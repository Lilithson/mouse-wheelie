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

package de.siphalor.mousewheelie;

import java.util.Locale;
import java.util.Optional;

public enum MWFeature {
	SCROLL,
	QUICK_CRAFT,
	SORT,
	REFILL,
	TOOL_PICK_INVENTORY,
	;

	public static Optional<MWFeature> of(String name) {
		try {
			return Optional.of(valueOf(name.toUpperCase(Locale.ROOT)));
		} catch (IllegalArgumentException ignored) {
			return Optional.empty();
		}
	}
}
