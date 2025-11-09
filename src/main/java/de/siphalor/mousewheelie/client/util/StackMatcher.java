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

package de.siphalor.mousewheelie.client.util;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.component.DataComponentPatch;
//- import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class StackMatcher {
	//# if MC_VERSION_NUMBER >= 12006
	private final @NotNull Item item;
	private final @Nullable DataComponentPatch componentPatch;

	public static StackMatcher withoutCustomData(@NotNull ItemStack stack) {
		return new StackMatcher(stack.getItem(), DataComponentPatch.EMPTY);
	}

	public static StackMatcher of(@NotNull ItemStack stack) {
		return new StackMatcher(stack.getItem(), stack.getComponentsPatch());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StackMatcher other) {
			return item == other.item && Objects.equals(componentPatch, other.componentPatch);
		}
		if (obj instanceof ItemStack stack) {
			return item == stack.getItem() && Objects.equals(componentPatch, stack.getComponentsPatch());
		}
		if (obj instanceof Item objItem) {
			return item == objItem;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(item, componentPatch);
	}
	//# else
	//- private final @NotNull Item item;
	//- private final @Nullable CompoundTag nbt;

	//- public static StackMatcher withoutCustomData(@NotNull ItemStack stack) {
	//- 	return new StackMatcher(stack.getItem(), null);
	//- }

	//- public static StackMatcher of(@NotNull ItemStack stack) {
	//- 	return new StackMatcher(stack.getItem(), stack.getTag());
	//- }

	//- @Override
	//- public boolean equals(Object obj) {
	//- 	if (obj instanceof StackMatcher other) {
	//- 		return item == other.item && Objects.equals(nbt, other.nbt);
	//- 	}
	//- 	if (obj instanceof ItemStack stack) {
	//- 		return item == stack.getItem() && Objects.equals(nbt, stack.getTag());
	//- 	}
	//- 	if (obj instanceof Item objItem) {
	//- 		return item == objItem;
	//- 	}
	//- 	return false;
	//- }

	//- @Override
	//- public int hashCode() {
	//- 	return Objects.hash(item, nbt);
	//- }
	//# end
}
