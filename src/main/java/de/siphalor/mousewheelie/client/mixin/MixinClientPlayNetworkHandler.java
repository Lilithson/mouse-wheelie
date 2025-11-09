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
import de.siphalor.mousewheelie.client.MWClient;
import de.siphalor.mousewheelie.client.inventory.SlotRefiller;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.network.MWClientNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPacketListener.class)
public abstract class MixinClientPlayNetworkHandler extends ClientCommonPacketListenerImpl {
	protected MixinClientPlayNetworkHandler(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
		super(client, connection, connectionState);
	}


	/*@Inject(method = "onConfirmScreenAction", at = @At("RETURN"))
	public void onGuiActionConfirmed(ConfirmScreenActionS2CPacket packet, CallbackInfo callbackInfo) {
		InteractionManager.triggerSend(InteractionManager.TriggerType.GUI_CONFIRM);
	}*/

	@Inject(method = "handleSetCarriedItem", at = @At("HEAD"))
	public void onHeldItemChangeBegin(ClientboundSetCarriedItemPacket packet, CallbackInfo callbackInfo) {
		InteractionManager.triggerSend(InteractionManager.TriggerType.HELD_ITEM_CHANGE);
	}

	@Inject(method = "handleContainerSetSlot", at = @At("RETURN"))
	public void onGuiSlotUpdateBegin(ClientboundContainerSetSlotPacket packet, CallbackInfo callbackInfo) {
		MWClient.lastUpdatedSlot = packet.getSlot();
		InteractionManager.triggerSend(InteractionManager.TriggerType.CONTAINER_SLOT_UPDATE);
	}

	@Inject(method = "handleContainerSetSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/InventoryMenu;setItem(IILnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.BEFORE))
	public void onGuiSlotUpdateHotbar(ClientboundContainerSetSlotPacket packet, CallbackInfo callbackInfo) {
		if (MouseWheelie.config.refill.enable && MouseWheelie.config.refill.other) {
			//noinspection ConstantConditions
			Inventory inventory = minecraft.player.getInventory();
			if (packet.getSlot() - 36 == inventory.selected) { // MAIN_HAND
				SlotRefiller.scheduleRefillChecked(InteractionHand.MAIN_HAND, inventory, inventory.getItem(inventory.selected), packet.getItem());
			}
		}
	}

	@Inject(method = "handleContainerSetSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;setItem(IILnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.BEFORE))
	public void onGuiSlotUpdateOther(ClientboundContainerSetSlotPacket packet, CallbackInfo callbackInfo) {
		//noinspection ConstantConditions
		if (MouseWheelie.config.refill.enable && MouseWheelie.config.refill.other && minecraft.player.containerMenu == minecraft.player.inventoryMenu && packet.getSlot() == 45) {
			Inventory inventory = minecraft.player.getInventory();
			if (packet.getSlot() == 45) { // OFF_HAND
				SlotRefiller.scheduleRefillChecked(InteractionHand.OFF_HAND, inventory, inventory.offhand.get(0), packet.getItem());
			}
		}
	}

	@Inject(method = "handleContainerSetSlot", require = 2,
			at = {
				@At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/InventoryMenu;setItem(IILnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.AFTER),
				@At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;setItem(IILnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.AFTER),
			}
	)
	public void onGuiSlotUpdated(ClientboundContainerSetSlotPacket packet, CallbackInfo callbackInfo) {
		if (packet.getContainerId() == 0) {
			if (MWClientNetworking.areGuiUpdateRefillTriggersBlocked()) {
				MWClientNetworking.decrementGuiUpdateRefillTriggerBlocks();
				return;
			}

			SlotRefiller.performRefill();
		}
	}
}
