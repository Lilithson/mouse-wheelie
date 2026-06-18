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
//- import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
//- import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
//- import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
//- import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
//- import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;

@Environment(EnvType.CLIENT)
@Mixin(ClientPacketListener.class)
public abstract class MixinClientPlayNetworkHandler
	/*# if MC_VERSION_NUMBER >= 12002 */extends ClientCommonPacketListenerImpl/*# end */ {

	//# if MC_VERSION_NUMBER >= 12002
	protected MixinClientPlayNetworkHandler(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
		super(client, connection, connectionState);
	}
	//# else
	//- @Shadow @Final
	//- private Minecraft minecraft;
	//# end

	/*@Inject(method = "onConfirmScreenAction", at = @At("RETURN"))
	public void onGuiActionConfirmed(ConfirmScreenActionS2CPacket packet, CallbackInfo callbackInfo) {
		InteractionManager.triggerSend(InteractionManager.TriggerType.GUI_CONFIRM);
	}*/

	//# if MC_VERSION_NUMBER >= 12103
	@Inject(method = "handleSetHeldSlot", at = @At("HEAD"))
	public void onHeldItemChangeBegin(ClientboundSetHeldSlotPacket packet, CallbackInfo callbackInfo) {
		InteractionManager.triggerSend(InteractionManager.TriggerType.HELD_ITEM_CHANGE);
	}
	//# else
	//- @Inject(method = "handleSetCarriedItem", at = @At("HEAD"))
	//- public void onHeldItemChangeBegin(ClientboundSetCarriedItemPacket packet, CallbackInfo callbackInfo) {
	//- 	InteractionManager.triggerSend(InteractionManager.TriggerType.HELD_ITEM_CHANGE);
	//- }
	//# end

	@Inject(method = "handleContainerSetSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/InventoryMenu;setItem(IILnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.BEFORE))
	public void onGuiSlotUpdateHotbar(ClientboundContainerSetSlotPacket packet, CallbackInfo callbackInfo) {
		if (MouseWheelie.config.refill.enable && MouseWheelie.config.refill.other) {
			//noinspection ConstantConditions
			Inventory inventory = minecraft.player.getInventory();
			//# if MC_VERSION_NUMBER >= 12108
			if (packet.getSlot() - Inventory.INVENTORY_SIZE == inventory.getSelectedSlot()) {
			//# else
			//- if (packet.getSlot() - 36 == inventory.selected) {
			//# end
				SlotRefiller.scheduleRefillChecked(
						InteractionHand.MAIN_HAND,
						inventory,
						//# if MC_VERSION_NUMBER >= 12108
						inventory.getSelectedItem(),
						//# else
						//- inventory.getItem(inventory.selected),
						//# end
						packet.getItem()
				);
			//# if MC_VERSION_NUMBER >= 12103
			} else if (packet.getSlot() == InventoryMenu.SHIELD_SLOT) {
				SlotRefiller.scheduleRefillChecked(
						InteractionHand.OFF_HAND,
						inventory,
						//# if MC_VERSION_NUMBER >= 12108
						minecraft.player.getOffhandItem(),
						//# else
						//- inventory.offhand.get(0),
						//# end
						packet.getItem()
				);
			//# end
			}
		}
	}

	//# if MC_VERSION_NUMBER < 12103
	//- @Inject(method = "handleContainerSetSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;setItem(IILnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.BEFORE))
	//- public void onGuiSlotUpdateOther(ClientboundContainerSetSlotPacket packet, CallbackInfo callbackInfo) {
	//- 	//noinspection ConstantConditions
	//- 	if (MouseWheelie.config.refill.enable && MouseWheelie.config.refill.other && minecraft.player.containerMenu == minecraft.player.inventoryMenu && packet.getSlot() == 45) {
	//- 		Inventory inventory = minecraft.player.getInventory();
	//- 		if (packet.getSlot() == 45) {
	//- 			SlotRefiller.scheduleRefillChecked(
	//- 					InteractionHand.OFF_HAND,
	//- 					inventory,
	//- 					inventory.offhand.get(0),
	//- 					packet.getItem()
	//- 			);
	//- 		}
	//- 	}
	//- }
	//# end

	@Inject(method = "handleContainerSetSlot",
			require = /*# if MC_VERSION_NUMBER >= 12103 */ 1 /*# else *//*-  2  *//*# end */,
			at = {
				@At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/InventoryMenu;setItem(IILnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.AFTER),
				//# if MC_VERSION_NUMBER < 12103
				//- @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;setItem(IILnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.AFTER),
				//# end
			}
	)
	public void onGuiSlotUpdated(ClientboundContainerSetSlotPacket packet, CallbackInfo callbackInfo) {
		if (packet.getContainerId() == 0
				&& MWClientNetworking.shouldPlayerInventoryMenuUpdateBeProcessedForState(packet.getStateId())) {
			SlotRefiller.performRefill();
		} else {
			SlotRefiller.clearRefill();
		}
	}

	@Inject(method = "handleContainerSetSlot", at = @At("RETURN"))
	public void onGuiSlotUpdateEnd(ClientboundContainerSetSlotPacket packet, CallbackInfo callbackInfo) {
		MWClient.lastUpdatedSlot = packet.getSlot();
		InteractionManager.triggerSend(InteractionManager.TriggerType.CONTAINER_SLOT_UPDATE);
	}

	//# if MC_VERSION_NUMBER >= 12002
	// moved to MixinClientCommonNetworkHandler
	//# else
	//- @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
	//- public void onSend(Packet<?> packet, CallbackInfo callbackInfo) {
	//- 	if (packet instanceof ServerboundPlayerActionPacket) {
	//- 		if (((ServerboundPlayerActionPacket) packet).getAction() == ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
	//- 			MWClientNetworking.setPlayerInventoryMenuAlreadyProcessedStateId(
	//- 					minecraft.player.inventoryMenu.getStateId() + 2
	//- 			);
	//- 		}
	//- 	}
	//- }
	//# end
}
