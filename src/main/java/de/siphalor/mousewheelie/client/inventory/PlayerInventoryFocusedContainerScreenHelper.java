/*
 * Copyright 2025 Siphalor and contributors
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
import de.siphalor.mousewheelie.client.util.inject.ISlot;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class PlayerInventoryFocusedContainerScreenHelper<T extends AbstractContainerScreen<?>> extends ContainerScreenHelper<T> {
	protected PlayerInventoryFocusedContainerScreenHelper(T screen, ClickEventFactory clickEventFactory) {
		super(screen, clickEventFactory);
	}

	@Override
	public int getScope(Slot slot, boolean preferSmallerScopes) {
		// In player inventory-focused screens, Vanilla scopes the hotbar separately from the main inventory
		if (slot.container instanceof Inventory) {
			if (isHotbarSlot(slot)) {
				return 0;
			} else {
				int idInContainer = ((ISlot) slot).mouseWheelie_getIndexInInv();
				if (idInContainer < 36) {
					// main inventory
					return 1;
				} else if (idInContainer < 40) {
					// armor slots
					return 2;
				} else {
					// offhand + potentially other stuff
					return -1;
				}
			}
		} else {
			return 3;
		}
	}
}
