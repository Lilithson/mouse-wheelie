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
import de.siphalor.mousewheelie.client.MWClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;

public class OpenConfigScreenKeybinding extends AmecsKeyBinding implements PriorityKeyBinding {
	//# if MC_VERSION_NUMBER >= 12109
	public OpenConfigScreenKeybinding(ResourceLocation id, InputConstants.Type type, int code, Category category, KeyModifiers defaultModifiers) {
		super(id, type, code, category, defaultModifiers);
	}
	//# else
	//- public OpenConfigScreenKeybinding(ResourceLocation id, InputConstants.Type type, int code, String category, KeyModifiers defaultModifiers) {
	//- 	super(id, type, code, category, defaultModifiers);
	//- }
	//# end

	@Override
	public boolean onPressedPriority() {
		Minecraft minecraftClient = Minecraft.getInstance();
		if (minecraftClient.screen == null || minecraftClient.screen instanceof AbstractContainerScreen || minecraftClient.screen instanceof TitleScreen) {
			minecraftClient.setScreen(MWClient.createConfigScreen());
			return true;
		}
		return false;
	}
}
