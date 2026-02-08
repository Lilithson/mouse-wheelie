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
import de.siphalor.mousewheelie.client.MWClient;
//- import de.siphalor.mousewheelie.client.compat.MWCompanionDataPackHelper;
//- import de.siphalor.mousewheelie.client.network.MWClientNetworking;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

//- import net.minecraft.client.Minecraft;
//- import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
@RequiredArgsConstructor
@CustomLog
public class ToolPicker {
	private final Inventory inventory;

	static int lastToolPickSlot = -1;

	public static synchronized void setLastToolPickSlot(int lastToolPickSlot) {
		ToolPicker.lastToolPickSlot = lastToolPickSlot;
	}

	public int findToolFor(BlockState blockState) {
		float bestBreakSpeed = 1.0F;
		int bestSpeedSlot = -1;
		//# if MC_VERSION_NUMBER >= 12108
		int invSize = (canPickFromInventory() ? inventory.getContainerSize() : 9);
		int selectedSlot = inventory.getSelectedSlot();
		//# else
		//- int invSize = (canPickFromInventory() ? inventory.items.size() : 9);
		//- int selectedSlot = inventory.selected;
		//# end
		for (int i = 1; i <= invSize; i++) {
			int index = (i + lastToolPickSlot) % invSize;
			if (index == selectedSlot) continue;
			//# if MC_VERSION_NUMBER >= 12108
			ItemStack stack = inventory.getItem(index);
			//# else
			//- ItemStack stack = inventory.items.get(index);
			//# end
			if (stack.isCorrectToolForDrops(blockState)) {
				return index;
			} else {
				float breakSpeed = stack.getDestroySpeed(blockState);
				if (breakSpeed > bestBreakSpeed) {
					bestSpeedSlot = index;
					bestBreakSpeed = breakSpeed;
				}
			}
		}
		if (bestSpeedSlot == -1) {
			//# if MC_VERSION_NUMBER >= 12108
			ItemStack stack = inventory.getItem(selectedSlot);
			//# else
			//- ItemStack stack = inventory.items.get(selectedSlot);
			//# end
			if (stack.isCorrectToolForDrops(blockState) || stack.getDestroySpeed(blockState) > 1.0F)
				return selectedSlot;
		}
		return bestSpeedSlot;
	}

	public boolean pickToolFor(BlockState blockState) {
		return pick(findToolFor(blockState));
	}

	public int findWeapon() {
		//# if MC_VERSION_NUMBER >= 12108
		int invSize = (canPickFromInventory() ? inventory.getContainerSize() : 9);
		//# else
		//- int invSize = (canPickFromInventory() ? inventory.items.size() : 9);
		//# end
		for (int i = 1; i <= invSize; i++) {
			int index = (i + lastToolPickSlot) % invSize;
			//# if MC_VERSION_NUMBER >= 12108
			if (index == inventory.getSelectedSlot()) continue;
			if (MWClient.isWeapon(inventory.getItem(index))) return index;
			//# else
			//- if (index == inventory.selected) continue;
			//- if (MWClient.isWeapon(inventory.items.get(index))) return index;
			//# end
		}
		return -1;
	}

	public boolean pickWeapon() {
		return pick(findWeapon());
	}

	private boolean canPickFromInventory() {
		//# if MC_VERSION_NUMBER >= 12104
		return MouseWheelie.config.toolPicking.pickFromInventory;
		//# else
		//- return MouseWheelie.config.toolPicking.pickFromInventory;
		//# end
	}

	private boolean pick(int index) {
		setLastToolPickSlot(index);

		//# if MC_VERSION_NUMBER >= 12108
		if (index != -1 && index != inventory.getSelectedSlot()) {
		//# else
		//- if (index != -1 && index != inventory.selected) {
		//# end
			if (Inventory.isHotbarSlot(index)) {
				//# if MC_VERSION_NUMBER >= 12108
				inventory.setSelectedSlot(index);
				//# else
				//- inventory.selected = index;
				//# end
			} else {
				new StackPicker(inventory.player)
						.pick(index, StackPicker.TargetMode.KEEP_SELECTED_HOTBAR_SLOT);
			}
			return true;
		}
		return false;
	}
}
