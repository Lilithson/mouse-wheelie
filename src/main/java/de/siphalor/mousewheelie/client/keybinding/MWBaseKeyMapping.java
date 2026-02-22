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

package de.siphalor.mousewheelie.client.keybinding;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.mousewheelie.MouseWheelie;

import net.minecraft.client.KeyMapping;

public class MWBaseKeyMapping extends KeyMapping {
	//# if MC_VERSION_NUMBER >= 12109
	public MWBaseKeyMapping(String name, InputConstants.Key key, Category category) {
	//# else
	//- public MWBaseKeyMapping(String name, InputConstants.Key key, String category) {
	//# end
		this(name, key.getType(), key.getValue(), category);
	}

	//# if MC_VERSION_NUMBER >= 12109
	public MWBaseKeyMapping(String name, InputConstants.Type inputType, int inputCode, Category category) {
	//# else
	//- public MWBaseKeyMapping(String name, InputConstants.Type inputType, int inputCode, String category) {
	//# end
		super("key." + MouseWheelie.MOD_ID + "." + name, inputType, inputCode, category);
	}
}
