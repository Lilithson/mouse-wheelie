/*
 * Copyright 2020-2022 Siphalor
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
import lombok.RequiredArgsConstructor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
@RequiredArgsConstructor
public class ToolPicker {
	private final Inventory inventory;

	static int lastToolPickSlot = -1;

	public static synchronized void setLastToolPickSlot(int lastToolPickSlot) {
		ToolPicker.lastToolPickSlot = lastToolPickSlot;
	}

	public int findToolFor(BlockState blockState) {
		float bestBreakSpeed = 1.0F;
		int bestSpeedSlot = -1;
		int invSize = (MouseWheelie.config.toolPicking.pickFromInventory ? inventory.items.size() : 9);
		for (int i = 1; i <= invSize; i++) {
			int index = (i + lastToolPickSlot) % invSize;
			if (index == inventory.selected) continue;
			ItemStack stack = inventory.items.get(index);
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
			ItemStack stack = inventory.items.get(inventory.selected);
			if (stack.isCorrectToolForDrops(blockState) || stack.getDestroySpeed(blockState) > 1.0F)
				return inventory.selected;
		}
		return bestSpeedSlot;
	}

	public boolean pickToolFor(BlockState blockState) {
		return pick(findToolFor(blockState));
	}

	public int findWeapon() {
		int invSize = (MouseWheelie.config.toolPicking.pickFromInventory ? inventory.items.size() : 9);
		for (int i = 1; i <= invSize; i++) {
			int index = (i + lastToolPickSlot) % invSize;
			if (index == inventory.selected) continue;
			if (MWClient.isWeapon(inventory.items.get(index).getItem()))
				return index;
		}
		return -1;
	}

	public boolean pickWeapon() {
		return pick(findWeapon());
	}

	private boolean pick(int index) {
		setLastToolPickSlot(index);

		if (index != -1 && index != inventory.selected) {
			ServerboundPickItemPacket packet = new ServerboundPickItemPacket(index);
			Minecraft.getInstance().getConnection().send(packet);
			return true;
		}
		return false;
	}
}
