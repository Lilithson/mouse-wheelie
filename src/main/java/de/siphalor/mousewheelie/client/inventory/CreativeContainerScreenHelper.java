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

import de.siphalor.mousewheelie.client.network.ClickEventFactory;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.ItemStackUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class CreativeContainerScreenHelper<T extends CreativeModeInventoryScreen> extends ContainerScreenHelper<T> {
	public CreativeContainerScreenHelper(T screen, ClickEventFactory clickEventFactory) {
		super(screen, clickEventFactory);
	}

	@Override
	public void sendSingleItem(Slot slot) {
		if (slot.container instanceof Inventory) {
			super.sendSingleItem(slot);
		} else {
			int scope = getScope(slot);
			for (Slot testSlot : screen.getMenu().slots) {
				if (getScope(testSlot) != scope) {
					ItemStack itemStack = testSlot.getItem();
					if (ItemStackUtils.canCombine(slot.getItem(), itemStack) && itemStack.getCount() < itemStack.getMaxStackSize()) {
						InteractionManager.push(clickEventFactory.create(slot, 0, ClickType.PICKUP));
						InteractionManager.push(clickEventFactory.create(testSlot, 0, ClickType.PICKUP));
						return;
					}
				}
			}
			for (Slot testSlot : screen.getMenu().slots) {
				if (getScope(testSlot) != scope) {
					if (!testSlot.hasItem()) {
						InteractionManager.push(clickEventFactory.create(slot, 0, ClickType.PICKUP));
						InteractionManager.push(clickEventFactory.create(testSlot, 0, ClickType.PICKUP));
						return;
					}
				}
			}
		}
	}

	@Override
	public int getScope(Slot slot, boolean preferSmallerScopes) {
		if (screen.isInventoryOpen()) {
			return super.getScope(slot, preferSmallerScopes);
		}
		if (slot.container instanceof Inventory) {
			if (isHotbarSlot(slot)) {
				return 0;
			}
		}
		return INVALID_SCOPE;
	}

	@Override
	public void sendStack(Slot slot) {
		if (slot.container instanceof Inventory) {
			super.sendStack(slot);
		} else {
			int count = slot.getItem().getMaxStackSize();
			InteractionManager.push(clickEventFactory.create(slot, 0, ClickType.CLONE));
			for (Slot testSlot : screen.getMenu().slots) {
				ItemStack itemStack = testSlot.getItem();
				if (itemStack.isEmpty()) {
					InteractionManager.push(clickEventFactory.create(testSlot, 0, ClickType.PICKUP));
					return;
				} else if (ItemStackUtils.canCombine(itemStack, slot.getItem()) && itemStack.getCount() < itemStack.getMaxStackSize()) {
					count -= itemStack.getCount();
					InteractionManager.push(clickEventFactory.create(testSlot, 0, ClickType.PICKUP));
					if (count <= 0) return;
				}
			}
			InteractionManager.push(clickEventFactory.create(getDelSlot(slot.getItem()), 0, ClickType.PICKUP));
		}
	}

	@Override
	public void sendAllOfAKind(Slot referenceSlot) {
		if (referenceSlot.container instanceof Inventory) {
			super.sendAllOfAKind(referenceSlot);
		} else {
			sendStack(referenceSlot);
		}
	}

	@Override
	public void sendAllFrom(Slot referenceSlot) {
		if (referenceSlot.container instanceof Inventory) {
			super.sendAllFrom(referenceSlot);
		}
	}

	private Slot getDelSlot(ItemStack delStack) {
		for (Slot slot : screen.getMenu().slots) {
			if (slot.getItem().getItem() != delStack.getItem()) {
				return slot;
			}
		}
		return screen.getMenu().slots.get(0);
	}
}
