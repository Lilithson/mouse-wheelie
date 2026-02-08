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
import de.siphalor.amecs.priority_key_mappings.api.AmecsPriorityKeyMapping;

public class ActionModifierKeybinding extends MWBaseKeyMapping implements AmecsPriorityKeyMapping {
	//# if MC_VERSION_NUMBER >= 12109
	public ActionModifierKeybinding(String name, InputConstants.Key key, Category category) {
	//# else
	//- public ActionModifierKeybinding(String name, InputConstants.Key key, String category) {
	//# end
		super(name, key, category);
	}

	@Override
	public boolean onPressedPriority() {
		setDown(true);
		return false;
	}

	@Override
	public boolean onReleasedPriority() {
		setDown(false);
		return false;
	}
}
