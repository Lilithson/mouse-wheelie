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

package de.siphalor.mousewheelie.client.keybinding;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.api.PriorityKeyBinding;
import de.siphalor.mousewheelie.client.inventory.ToolPicker;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class PickToolKeyBinding extends AmecsKeyBinding implements PriorityKeyBinding {
	//# if MC_VERSION_NUMBER >= 12109
	public PickToolKeyBinding(ResourceLocation id, InputConstants.Type type, int code, Category category, KeyModifiers defaultModifiers) {
		super(id, type, code, category, defaultModifiers);
	}
	//# else
	//- public PickToolKeyBinding(ResourceLocation id, InputConstants.Type type, int code, String category, KeyModifiers defaultModifiers) {
	//- 	super(id, type, code, category, defaultModifiers);
	//- }
	//# end

	@Override
	public boolean onPressedPriority() {
		if (Minecraft.getInstance().screen != null) return false;
		Player playerEntity = Minecraft.getInstance().player;
		if (playerEntity != null) {
			HitResult hitResult = playerEntity.pick(4.5D, 0.0F, false);
			if (hitResult.getType() == HitResult.Type.BLOCK) {
				return new ToolPicker(playerEntity.getInventory()).pickToolFor(playerEntity.level().getBlockState(((BlockHitResult) hitResult).getBlockPos()));
			} else {
				return new ToolPicker(playerEntity.getInventory()).pickWeapon();
			}
		}
		return false;
	}
}
