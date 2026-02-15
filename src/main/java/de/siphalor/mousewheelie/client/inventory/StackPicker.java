package de.siphalor.mousewheelie.client.inventory;

import de.siphalor.mousewheelie.client.inventory.view.InventoryViewLocation;
import de.siphalor.mousewheelie.client.mixin.item.BundleContentsAccessor;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.commons.lang3.math.Fraction;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;

@RequiredArgsConstructor
public class StackPicker {
	private static final int HOTBAR_CONATINER_START_SLOT = InventoryMenu.USE_ROW_SLOT_START;
	private static final int MAIN_INVENTORY_START_SLOT = InventoryMenu.INV_SLOT_START;

	private final Player player;

	public boolean pick(InventoryViewLocation from, Options options) {
		if (from instanceof InventoryViewLocation.Bundle) {
			return pickFromBundleLocation((InventoryViewLocation.Bundle) from, options);
		} else {
			pickFromInventorySlot(from.getInventorySlotId(), options.getTargetMode());
			return true;
		}
	}

	public boolean pickFromBundleLocation(InventoryViewLocation.Bundle bundleLocation, Options options) {
		int targetContainerSlot = resolveTargetContainerSlot(
				options.getTargetMode() == TargetMode.OFFHAND
						? TargetMode.OFFHAND
						: TargetMode.PREFER_EMPTY_HOTBAR_SLOTS
		);
		ItemStack backfillStack = player.inventoryMenu.getSlot(targetContainerSlot).getItem();

		BundleContents bundleContents = player.getInventory().getItem(bundleLocation.getInventorySlotId())
				.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
		ItemStack refillStack = bundleContents.getItemUnsafe(bundleLocation.getIndexInBundle());

		boolean backfillToBundle = !backfillStack.isEmpty()
				&& options.isBackfillToBundleAllowed()
				&& isBackfillToBundlePossible(bundleContents, refillStack, backfillStack);
		int backfillSlot = backfillToBundle ? -1 : findFreeMainInventorySlot();

		if (!backfillStack.isEmpty() && !backfillToBundle && backfillSlot < 0) {
			return false;
		}

		takeFromBundle(bundleLocation, bundleContents);
		swapWithTargetContainerSlot(targetContainerSlot);

		if (backfillToBundle) {
			InteractionManager.push(new InteractionManager.ClickEvent(
					player.inventoryMenu.containerId,
					inventorySlotToContainerSlot(backfillSlot),
					1,
					ClickType.PICKUP
				));
		} else if (backfillSlot >= 0) {
			swapWithInventorySlot(backfillSlot);
		}
		return true;
	}

	private boolean isBackfillToBundlePossible(
			BundleContents bundleContents,
			ItemStack excludeStack,
			ItemStack backfillStack
	) {
		if (!BundleContents.canItemBeInBundle(backfillStack)) {
			return false;
		}

		Fraction excludeWeight = BundleContentsAccessor.callGetWeight(excludeStack);
		Fraction backfillWeight = BundleContentsAccessor.callGetWeight(backfillStack);
		return bundleContents.weight().subtract(excludeWeight).add(backfillWeight).compareTo(Fraction.ONE) > 0;
	}

	private void takeFromBundle(InventoryViewLocation.Bundle bundleLocation, BundleContents bundleContents) {
		if (bundleContents.getSelectedItem() != bundleLocation.getIndexInBundle()) {
			InteractionManager.push(new InteractionManager.PacketEvent(new ServerboundSelectBundleItemPacket(
					inventorySlotToContainerSlot(bundleLocation.getInventorySlotId()),
					bundleLocation.getIndexInBundle()
			)));
		}
		InteractionManager.pushClickEvent(
				player.inventoryMenu.containerId,
				inventorySlotToContainerSlot(bundleLocation.getInventorySlotId()),
				1,
				ClickType.PICKUP
		);
	}

	public void pickFromInventorySlot(int inventorySlot, TargetMode targetMode) {
		if (Inventory.isHotbarSlot(inventorySlot)) {
			selectHotbarSlot(inventorySlot);
			return;
		}

		swapWithInventorySlot(inventorySlot);
		int targetContainerSlot = resolveTargetContainerSlot(targetMode);
		swapWithTargetContainerSlot(targetContainerSlot);
		if (player.inventoryMenu.getSlot(targetContainerSlot).hasItem()) {
			swapWithInventorySlot(inventorySlot);
		}
	}

	private int resolveTargetContainerSlot(TargetMode targetMode) {
		if (targetMode == TargetMode.OFFHAND) {
			return InventoryMenu.SHIELD_SLOT;
		} else if (targetMode == TargetMode.PREFER_EMPTY_HOTBAR_SLOTS) {
			int nextFreeHotbarSlot = findNextFreeHotbarInventorySlot();
			if (nextFreeHotbarSlot >= 0) {
				return HOTBAR_CONATINER_START_SLOT + nextFreeHotbarSlot;
			}
		}

		return getSelectedHotbarSlot();
	}

	private int findFreeMainInventorySlot() {
		for (int i = Inventory.SELECTION_SIZE; i < Inventory.INVENTORY_SIZE; i++) {
			if (player.getInventory().getItem(i).isEmpty()) {
				return i;
			}
		}
		return -1;
	}

	private int findNextFreeHotbarInventorySlot() {
		//# if MC_VERSION_NUMBER >= 12108
		int selectedSlot = player.getInventory().getSelectedSlot();
		//# else
		//- int selectedSlot = player.getInventory().selected;
		//# end
		for (int targetSlot = selectedSlot; targetSlot < 9; targetSlot++) {
			if (player.getInventory().getItem(targetSlot).isEmpty()) {
				return targetSlot;
			}
		}
		for (int targetSlot = selectedSlot - 1; targetSlot >= 0; targetSlot--) {
			if (player.getInventory().getItem(targetSlot).isEmpty()) {
				return targetSlot;
			}
		}
		return -1;
	}

	private void swapWithTargetContainerSlot(int targetContainerSlot) {
		if (InventoryMenu.isHotbarSlot(targetContainerSlot)) {
			int hotbarSlot = targetContainerSlot - HOTBAR_CONATINER_START_SLOT;
			if (hotbarSlot != getSelectedHotbarSlot()) {
				selectHotbarSlot(hotbarSlot);
			}
			swapWithInventorySlot(targetContainerSlot);
		} else {
			swapWithInventorySlot(targetContainerSlot);
		}
	}

	private void selectHotbarSlot(int hotbarSlot) {
		player.getInventory().setSelectedSlot(hotbarSlot);
		Minecraft.getInstance().getConnection().send(new ServerboundSetCarriedItemPacket(hotbarSlot));
	}

	private void swapWithInventorySlot(int slot) {
		InteractionManager.push(new InteractionManager.ClickEvent(
				player.inventoryMenu.containerId,
				inventorySlotToContainerSlot(slot),
				player.getInventory().getItem(slot).isEmpty() ? 0 : 1,
				ClickType.PICKUP
		));
	}

	private int getSelectedHotbarSlot() {
		//# if MC_VERSION_NUMBER >= 12108
		return player.getInventory().getSelectedSlot();
		//# else
		//- return player.getInventory().selected;
		//# end
	}

	private static int inventorySlotToContainerSlot(int inventorySlot) {
		if (Inventory.isHotbarSlot(inventorySlot)) {
			return inventorySlot + HOTBAR_CONATINER_START_SLOT;
		} else {
			return inventorySlot - Inventory.SELECTION_SIZE + MAIN_INVENTORY_START_SLOT;
		}
	}

	@Value
	public static class Options {
		TargetMode targetMode;
		boolean backfillToBundleAllowed;
	}

	public enum TargetMode {
		PREFER_EMPTY_HOTBAR_SLOTS,
		KEEP_SELECTED_HOTBAR_SLOT,
		OFFHAND,
	}
}
