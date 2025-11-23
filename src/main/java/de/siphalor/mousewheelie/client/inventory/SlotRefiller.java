/*
 * Copyright 2020 Siphalor and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.siphalor.mousewheelie.client.inventory;

import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.network.MWClientNetworking;
import de.siphalor.mousewheelie.client.util.ItemStackUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
//- import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
//- import net.minecraft.world.item.enchantment.EnchantmentHelper;
//- import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class SlotRefiller {
	/**
	 * Indicates the maximum time in milliseconds a refill is expected to take.
	 * If a refill has been started with no recorded end, it is treated as done after this time.
	 */
	private static final long MAX_REFILL_MILLIS = 5000;
	private static final InteractionManager.InteractionEvent REFILL_END_EVENT = () -> {
		endRefill();
		return InteractionManager.DUMMY_WAITER;
	};

	private static Inventory playerInventory;
	private static ItemStack stack;
	private static long refillStartTime = System.currentTimeMillis() - MAX_REFILL_MILLIS;

	private static final ConcurrentLinkedDeque<Rule> rules = new ConcurrentLinkedDeque<>();
	private static InteractionHand refillHand = null;

	private SlotRefiller() {}

	/**
	 * Schedules a refill if a refill scenario is encountered.
	 * @param hand the hand to potentially refill
	 * @param inventory the player inventory
	 * @param oldStack the old stack in the hand
	 * @param newStack the new stack in the hand
	 * @return whether a refill has been scheduled
	 */
	public static boolean scheduleRefillChecked(InteractionHand hand, Inventory inventory, ItemStack oldStack, ItemStack newStack) {
		if (Minecraft.getInstance().screen != null) {
			return false;
		}

		if (!oldStack.isEmpty() && (newStack.isEmpty() || (MouseWheelie.config.refill.itemChanges && oldStack.getItem() != newStack.getItem()))) {
			scheduleRefillUnchecked(hand, inventory, oldStack.copy());
			return true;
		}
		return false;
	}

	/**
	 * Unconditionally schedules a refill.
	 * @param hand the hand to refill
	 * @param inventory the player inventory
	 * @param referenceStack the stack to decide the refilling by
	 */
	public static void scheduleRefillUnchecked(InteractionHand hand, Inventory inventory, ItemStack referenceStack) {
		refillHand = hand;
		setupRefill(inventory, referenceStack);
	}

	public static boolean performRefill() {
		if (refillHand == null) return false;

		InteractionHand hand = refillHand;
		refillHand = null;
		if (hand == InteractionHand.OFF_HAND && !MouseWheelie.config.refill.offHand) {
			return false;
		}
		refill(hand);

		return true;
	}


	public static void setupRefill(Inventory playerInventory, ItemStack stack) {
		SlotRefiller.playerInventory = playerInventory;
		SlotRefiller.stack = stack;
	}

	/**
	 * @deprecated Use {@link #refill(InteractionHand)} instead.
	 */
	@Deprecated
	public static boolean refill() {
		return refill(InteractionHand.MAIN_HAND);
	}

	@SuppressWarnings("UnusedReturnValue")
	public static boolean refill(InteractionHand hand) {
		if (isRefillInProgress()) {
			return false;
		}
		//# if MC_VERSION_NUMBER >= 12101
		if (!stack.getOrDefault(
				EnchantmentEffectComponents.TRIDENT_RETURN_ACCELERATION,
				Collections.emptyList()
		).isEmpty()) {
			return false;
		}
		//# else
		//- if (stack.getItem() == Items.TRIDENT && EnchantmentHelper.getLoyalty(stack) > 0) {
		//- 	return false;
		//- }
		//# end

		Iterator<Rule> iterator = rules.descendingIterator();
		while (iterator.hasNext()) {
			Rule rule = iterator.next();
			if (!rule.matches(stack)) {
				continue;
			}

			int slot = rule.findMatchingStack(playerInventory, stack);

			if (slot != -1) {
				startRefill();

				refillFromSlot(hand, slot);
				return true;
			}
		}
		return false;
	}

	private static void startRefill() {
		refillStartTime = System.currentTimeMillis();

		if (MouseWheelie.config.refill.playSound) {
			scheduleRefillSound();
		}
	}

	private static void endRefill() {
		refillStartTime = System.currentTimeMillis() - MAX_REFILL_MILLIS;
	}

	private static boolean isRefillInProgress() {
		return System.currentTimeMillis() - refillStartTime < MAX_REFILL_MILLIS;
	}

	private static void scheduleRefillSound() {
		InteractionManager.delay(SlotRefiller::playRefillSound, Duration.of(200, ChronoUnit.MILLIS));
	}

	private static void playRefillSound() {
		//# if MC_VERSION_NUMBER >= 12102
		Minecraft.getInstance().schedule(() ->
		//# else
		//- Minecraft.getInstance().tell(() ->
		//# end
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_PICKUP, 0.2F, 1F))
		);
	}

	private static void refillFromSlot(InteractionHand hand, int slot) {
		//# if MC_VERSION_NUMBER >= 12108
		if (slot == playerInventory.getSelectedSlot()) {
		//# else
		//- if (slot == playerInventory.selected) {
		//# end
			return;
		}

		if (Inventory.isHotbarSlot(slot)) {
			refillFromHotbar(hand, slot);
		} else {
			refillFromInventory(hand, slot);
		}

		InteractionManager.push(REFILL_END_EVENT);
	}

	private static void refillFromHotbar(InteractionHand hand, int hotbarSlot) {
		if (MouseWheelie.config.refill.restoreSelectedSlot) {
			//# if MC_VERSION_NUMBER >= 12108
			ItemStack offhandStack = playerInventory.player.getOffhandItem();
			//# else
			//- ItemStack offhandStack = playerInventory.offhand.get(0);
			//# end
			if (hand == InteractionHand.MAIN_HAND && !offhandStack.isEmpty()) {
				InteractionManager.push(InteractionManager.SWAP_WITH_OFFHAND_EVENT);
			}
			InteractionManager.push(new InteractionManager.PacketEvent(
					new ServerboundSetCarriedItemPacket(hotbarSlot),
					InteractionManager.Waiter.equal(InteractionManager.TriggerType.HELD_ITEM_CHANGE)
			));
			InteractionManager.push(InteractionManager.SWAP_WITH_OFFHAND_EVENT);
			InteractionManager.push(new InteractionManager.PacketEvent(
					//# if MC_VERSION_NUMBER >= 12108
					new ServerboundSetCarriedItemPacket(playerInventory.getSelectedSlot()),
					//# else
					//- new ServerboundSetCarriedItemPacket(playerInventory.selected),
					//# end
					InteractionManager.TICK_WAITER
			));
			if (hand == InteractionHand.MAIN_HAND) {
				InteractionManager.push(InteractionManager.SWAP_WITH_OFFHAND_EVENT);
			}
		} else {
			if (hand == InteractionHand.OFF_HAND) {
				InteractionManager.push(InteractionManager.SWAP_WITH_OFFHAND_EVENT);
			}
			//# if MC_VERSION_NUMBER >= 12108
			playerInventory.setSelectedSlot(hotbarSlot);
			//# else
			//- playerInventory.selected = hotbarSlot;
			//# end
			InteractionManager.push(new InteractionManager.PacketEvent(new ServerboundSetCarriedItemPacket(hotbarSlot), InteractionManager.TICK_WAITER));
			if (hand == InteractionHand.OFF_HAND) {
				InteractionManager.push(InteractionManager.SWAP_WITH_OFFHAND_EVENT);
			}
		}
	}

	private static void refillFromInventory(InteractionHand hand, int inventorySlot) {
		if (hand == InteractionHand.OFF_HAND) {
			//# if MC_VERSION_NUMBER >= 12108
			ItemStack mainHandStack = playerInventory.getSelectedItem();
			//# else
			//- ItemStack mainHandStack = playerInventory.getSelected();
			//# end
			InteractionManager.push(InteractionManager.SWAP_WITH_OFFHAND_EVENT);

			MWClientNetworking.pickFromInventory(inventorySlot);

			InteractionManager.push(InteractionManager.SWAP_WITH_OFFHAND_EVENT);
			// Sometimes the swapping visually duplicates the stack on the client,
			// so we're manually fixing the visuals here
			InteractionManager.push(() -> {
				//# if MC_VERSION_NUMBER >= 12108
				playerInventory.setItem(playerInventory.getSelectedSlot(), mainHandStack);
				//# else
				//- playerInventory.setItem(playerInventory.selected, mainHandStack);
				//# end
				return InteractionManager.DUMMY_WAITER;
			});
		} else {
			MWClientNetworking.pickFromInventory(inventorySlot);
		}
	}

	static {
		rules.add(new BlockRule());
		rules.add(new ItemGroupRule());
		rules.add(new ItemHierarchyRule());
		rules.add(new BlockHierarchyRule());
		rules.add(new FoodRule());
		rules.add(new EqualItemRule());
		rules.add(new EqualStackRule());
	}

	public abstract static class Rule {
		/**
		 * Creates a new rule.
		 * Automatically registers this rule to the list of rules.
		 */
		protected Rule() {
			rules.add(this);
		}

		/**
		 * Checks if the rule is valid for the given stack.
		 *
		 * @param oldStack The stack to check.
		 * @return Whether the rule applies to the given stack.
		 */
		abstract boolean matches(ItemStack oldStack);

		/**
		 * Find a matching slot for the given base stack in the player inventory.
		 *
		 * @param playerInventory The player inventory to search in.
		 * @param oldStack        The base stack to search for.
		 * @return The slot index of the matching stack or -1 if no match was found.
		 */
		abstract int findMatchingStack(Inventory playerInventory, ItemStack oldStack);

		/***
		 * Utility function that iterates over all slots of the player inventory and returns the first slot that matches the given predicate.
		 * @param playerInventory The player inventory to search in.
		 * @param predicate       The predicate to check for.
		 * @return The slot index of the matching stack or -1 if no match was found.
		 */
		protected int iterateInventory(Inventory playerInventory, Predicate<ItemStack> predicate) {
			//# if MC_VERSION_NUMBER >= 12108
			int invSize = playerInventory.getContainerSize();
			//# else
			//- int invSize = playerInventory.items.size();
			//# end
			for (int i = 0; i < invSize; i++) {
				//# if MC_VERSION_NUMBER >= 12108
				if (predicate.test(playerInventory.getItem(i))) return i;
				//# else
				//- if (predicate.test(playerInventory.items.get(i))) return i;
				//# end
			}
			return -1;
		}
	}

	public static class BlockRule extends Rule {
		@Override
		boolean matches(ItemStack oldStack) {
			return MouseWheelie.config.refill.rules.anyBlock && oldStack.getItem() instanceof BlockItem;
		}

		@Override
		int findMatchingStack(Inventory playerInventory, ItemStack oldStack) {
			return iterateInventory(playerInventory, stack -> stack.getItem() instanceof BlockItem);
		}
	}

	public static class ItemGroupRule extends Rule {

		private static boolean containsBroad(CreativeModeTab group, ItemStack stack) {
			return group.contains(stack) || group.contains(stack.getItem().getDefaultInstance());
		}

		@Override
		boolean matches(ItemStack oldStack) {
			if (!MouseWheelie.config.refill.rules.itemgroup) {
				return false;
			}
			for (CreativeModeTab group : CreativeModeTabs.allTabs()) {
				if (containsBroad(group, oldStack)) {
					return true;
				}
			}
			return false;
		}

		@Override
		int findMatchingStack(Inventory playerInventory, ItemStack oldStack) {
			List<CreativeModeTab> checkGroups = new ArrayList<>();
			for (CreativeModeTab group : CreativeModeTabs.allTabs()) {
				if (containsBroad(group, oldStack)) {
					checkGroups.add(group);
				}
			}
			if (checkGroups.isEmpty()) {
				return -1;
			}
			return iterateInventory(playerInventory, stack -> {
				for (CreativeModeTab group : checkGroups) {
					if (containsBroad(group, stack)) {
						return true;
					}
				}
				return false;
			});
		}
	}

	public static class ItemHierarchyRule extends Rule {
		@Override
		boolean matches(ItemStack oldStack) {
			return MouseWheelie.config.refill.rules.itemHierarchy && oldStack.getItem().getClass() != Item.class && !(oldStack.getItem() instanceof BlockItem);
		}

		@Override
		int findMatchingStack(Inventory playerInventory, ItemStack oldStack) {
			return findBestThroughClassHierarchy(oldStack, playerInventory, Item::getClass, Item.class);
		}
	}

	public static class BlockHierarchyRule extends Rule {
		@Override
		boolean matches(ItemStack oldStack) {
			return MouseWheelie.config.refill.rules.blockHierarchy && oldStack.getItem() instanceof BlockItem;
		}

		@Override
		int findMatchingStack(Inventory playerInventory, ItemStack oldStack) {
			return findBestThroughClassHierarchy(oldStack, playerInventory, item -> {
				if (item instanceof BlockItem) {
					return ((BlockItem) item).getBlock().getClass();
				} else {
					return null;
				}
			}, Block.class);
		}
	}

	private static int findBestThroughClassHierarchy(ItemStack baseStack, Inventory inventory, Function<Item, Class<?>> getClass, Class<?> baseClass) {
		int currentRank = 0;
		Collection<Class<?>> classes = new ArrayList<>(10);
		Class<?> clazz = getClass.apply(baseStack.getItem());
		while (clazz != baseClass) {
			classes.add(clazz);
			clazz = clazz.getSuperclass();
		}
		int classesSize = classes.size();
		if (classesSize == 0)
			return -1;

		int index = -1;

		//# if MC_VERSION_NUMBER >= 12108
		int invSize = inventory.getContainerSize();
		//# else
		//- int invSize = inventory.items.size();
		//# end
		outer:
		for (int i = 0; i < invSize; i++) {
			//# if MC_VERSION_NUMBER >= 12108
			clazz = getClass.apply(inventory.getItem(i).getItem());
			//# else
			//- clazz = getClass.apply(inventory.items.get(i).getItem());
			//# end
			if (clazz == null) {
				continue;
			}
			while (clazz != baseClass) {
				int classRank = classesSize;
				for (Iterator<Class<?>> iterator = classes.iterator(); iterator.hasNext(); classRank--) {
					if (classRank <= 0) break;
					if (classRank <= currentRank) continue outer;
					if (Objects.equals(clazz, iterator.next())) {
						if (classRank >= classesSize) return i;
						currentRank = classRank;
						index = i;
						continue outer;
					}
				}
				clazz = clazz.getSuperclass();
			}
		}
		return index;
	}

	public static class FoodRule extends Rule {
		@Override
		boolean matches(ItemStack oldStack) {
			return MouseWheelie.config.refill.rules.food && isEdible(oldStack);
		}

		@Override
		int findMatchingStack(Inventory playerInventory, ItemStack oldStack) {
			return iterateInventory(playerInventory, FoodRule::isEdible);
		}

		private static boolean isEdible(ItemStack stack) {
			//# if MC_VERSION_NUMBER >= 12006
			return stack.has(DataComponents.FOOD);
			//# else
			//- return stack.isEdible();
			//# end
		}
	}

	public static class EqualItemRule extends Rule {
		@Override
		boolean matches(ItemStack oldStack) {
			return MouseWheelie.config.refill.rules.equalItems;
		}

		@Override
		int findMatchingStack(Inventory playerInventory, ItemStack oldStack) {
			Item item = oldStack.getItem();
			return iterateInventory(playerInventory, stack -> stack.getItem() == item);
		}
	}

	public static class EqualStackRule extends Rule {
		@Override
		boolean matches(ItemStack oldStack) {
			return MouseWheelie.config.refill.rules.equalStacks;
		}

		@Override
		int findMatchingStack(Inventory playerInventory, ItemStack oldStack) {
			return iterateInventory(playerInventory, stack -> ItemStackUtils.canCombine(stack, oldStack));
		}
	}
}
