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

package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.inventory.SlotRefiller;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public abstract class MixinMinecraftClient {
	@Shadow
	public LocalPlayer player;

	@Unique
	private ItemStack mainHandStack;
	@Unique
	private ItemStack offHandStack;

	@Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/InteractionHand;values()[Lnet/minecraft/world/InteractionHand;"))
	public void onItemUse(CallbackInfo callbackInfo) {
		if (MouseWheelie.config.refill.enable && MouseWheelie.config.refill.use) {
			mainHandStack = player.getMainHandItem();
			mainHandStack = mainHandStack.isEmpty() ? null : mainHandStack.copy();
			offHandStack = player.getOffhandItem();
			offHandStack = offHandStack.isEmpty() ? null : offHandStack.copy();
		}
	}

	@Inject(method = "startUseItem", at = @At("RETURN"))
	public void onItemUsed(CallbackInfo callbackInfo) {
		boolean refillScheduled = false;
		if (mainHandStack != null) {
			refillScheduled = SlotRefiller.scheduleRefillChecked(InteractionHand.MAIN_HAND, player.getInventory(), mainHandStack, player.getMainHandItem());
		}
		if (!refillScheduled && offHandStack != null) {
			SlotRefiller.scheduleRefillChecked(InteractionHand.OFF_HAND, player.getInventory(), offHandStack, player.getOffhandItem());
		}
		SlotRefiller.performRefill();
		mainHandStack = null;
		offHandStack = null;
	}
}
