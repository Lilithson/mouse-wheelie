/*
 * Copyright 2026 Siphalor and contributors
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

package de.siphalor.mousewheelie.platform.neoforge;

import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.MWClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(MouseWheelie.MOD_ID)
public class MouseWheelieNeoForge {
	public MouseWheelieNeoForge(IEventBus modEventBus, ModContainer modContainer) {
		MouseWheelie.initialize();
		if (modContainer.getDist() == Dist.CLIENT) {
			modEventBus.addListener(this::onClientSetup);
		}
	}

	private void onClientSetup(FMLClientSetupEvent event) {
		MWClient.initializeClient();
	}
}
