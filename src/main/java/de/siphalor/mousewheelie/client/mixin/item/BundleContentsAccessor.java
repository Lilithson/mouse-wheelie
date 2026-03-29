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

package de.siphalor.mousewheelie.client.mixin.item;

import com.mojang.serialization.DataResult;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.item.ItemInstance;
//- import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;

//# if MC_VERSION_NUMBER >= 12103
@Mixin(BundleContents.class)
public interface BundleContentsAccessor {
	@Invoker
	//# if MC_VERSION_NUMBER >= 260100
	static DataResult<Fraction> callGetWeight(ItemInstance item) { return DataResult.success(Fraction.ONE); }
	//# else
	//- static Fraction callGetWeight(ItemStack stack) { return Fraction.ONE; }
	//# end
}
//# end
