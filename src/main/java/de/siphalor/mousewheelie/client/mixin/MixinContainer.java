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

package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.inventory.SlotRefiller;
import de.siphalor.mousewheelie.client.util.inject.ISlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(AbstractContainerMenu.class)
public abstract class MixinContainer {
	@Shadow
	public abstract Slot getSlot(int index);

	@Inject(method = "initializeContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;set(Lnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void onSlotUpdate(int i, List<ItemStack> itemStacks, ItemStack cursorStack, CallbackInfo callbackInfo, int index) {
		//noinspection ConstantConditions
		if ((Object) this instanceof InventoryMenu && MouseWheelie.config.refill.enable && MouseWheelie.config.refill.other) {
			Inventory playerInventory = Minecraft.getInstance().player.getInventory();
			Slot targetSlot = getSlot(index);
			if (targetSlot.container != playerInventory) return;

			int indexInInv = ((ISlot) targetSlot).mouseWheelie_getIndexInInv();
			if (indexInInv == playerInventory.selected) {
				SlotRefiller.scheduleRefillChecked(InteractionHand.MAIN_HAND, playerInventory, playerInventory.getSelected(), itemStacks.get(index));
			} else if (indexInInv == 40) {
				SlotRefiller.scheduleRefillChecked(InteractionHand.OFF_HAND, playerInventory, playerInventory.getItem(40), itemStacks.get(index));
			}
		}
	}

	@Inject(method = "initializeContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;set(Lnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.AFTER))
	public void onSlotUpdated(int i, List<ItemStack> stacks, ItemStack cursorStack, CallbackInfo callbackInfo) {
		SlotRefiller.performRefill();
	}

}
