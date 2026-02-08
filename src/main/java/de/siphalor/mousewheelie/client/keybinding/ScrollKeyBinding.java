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
import de.siphalor.amecs.inputs.mouse.api.AmecsMouseInputs;
import de.siphalor.amecs.priority_key_mappings.api.AmecsPriorityKeyMapping;
import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.MWClient;

public class ScrollKeyBinding extends MWBaseKeyMapping implements AmecsPriorityKeyMapping {
	private final boolean scrollDown;

	//# if MC_VERSION_NUMBER >= 12109
	public ScrollKeyBinding(String name, Category category, boolean scrollDown) {
	//# else
	//- public ScrollKeyBinding(String name, String category, boolean scrollDown) {
	//# end
		super(
				name,
				InputConstants.Type.MOUSE,
				scrollDown ? AmecsMouseInputs.SCROLL_DOWN : AmecsMouseInputs.SCROLL_UP,
				category
		);
		this.scrollDown = scrollDown;
	}

	@Override
	public boolean onPressedPriority() {
		return MWClient.triggerScroll(MWClient.getMouseX(), MWClient.getMouseY(), scrollDown == MouseWheelie.config.scrolling.invert ? -1D : 1D);
	}
}
