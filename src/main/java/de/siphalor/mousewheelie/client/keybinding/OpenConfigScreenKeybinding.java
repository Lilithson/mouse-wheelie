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

package de.siphalor.mousewheelie.client.keybinding;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.api.PriorityKeyBinding;
import de.siphalor.mousewheelie.client.MWClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class OpenConfigScreenKeybinding extends AmecsKeyBinding implements PriorityKeyBinding {
	public OpenConfigScreenKeybinding(Identifier id, InputUtil.Type type, int code, String category, KeyModifiers defaultModifiers) {
		super(id, type, code, category, defaultModifiers);
	}

	@Override
	public boolean onPressedPriority() {
		MinecraftClient minecraftClient = MinecraftClient.getInstance();
		if (minecraftClient.currentScreen == null || minecraftClient.currentScreen instanceof HandledScreen || minecraftClient.currentScreen instanceof TitleScreen) {
			//# if CONFIG == "TWEED_4"
			//- TweedRegistry.TAILORS.getOrEmpty(new Identifier("tweed4", "coat")).ifPresent(tailor -> {
			//- 	if (tailor instanceof CoatTailor) {
			//- 		ScreenTailorScreenFactory<?> screenFactory = ((CoatTailor) tailor).getScreenFactories().get(MouseWheelie.MOD_ID);
			//- 		if (screenFactory != null) {
			//- 			minecraftClient.setScreen(screenFactory.create(minecraftClient.currentScreen));
			//- 		}
			//- 	}
			//- });
			//# elif CONFIG == "TWEED_5"
			minecraftClient.setScreen(MWClient.createConfigScreen());
			//# end
			return true;
		}
		return false;
	}
}
