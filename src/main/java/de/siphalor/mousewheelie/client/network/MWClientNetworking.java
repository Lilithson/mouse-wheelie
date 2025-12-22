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

package de.siphalor.mousewheelie.client.network;

import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.compat.MWCompanionDataPackHelper;
import de.siphalor.mousewheelie.common.network.MWNetworking;
import de.siphalor.mousewheelie.common.network.PickFromInventoryPacket;
import de.siphalor.mousewheelie.common.network.ReorderInventoryPacket;
import lombok.CustomLog;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
//- import net.minecraft.network.FriendlyByteBuf;
//- import net.minecraft.network.protocol.game.ServerboundPickItemPacket;

import java.util.concurrent.CompletableFuture;

@CustomLog
public class MWClientNetworking extends MWNetworking {

	private static int blockNextGuiUpdateRefillTriggers;

	public static void setup() {
		//# if MC_VERSION_NUMBER >= 12006
		PayloadTypeRegistry.configurationS2C().register(MWFeatureControlPacket.TYPE, MWFeatureControlPacket.STREAM_CODEC);
		ClientConfigurationNetworking.registerGlobalReceiver(
				MWFeatureControlPacket.TYPE,
				(packet, context) -> onFeatureControlPacket(packet)
		);
		PayloadTypeRegistry.playS2C().register(MWFeatureControlPacket.TYPE, MWFeatureControlPacket.STREAM_CODEC);
		ClientPlayNetworking.registerGlobalReceiver(
				MWFeatureControlPacket.TYPE,
				(packet, context) -> onFeatureControlPacket(packet)
		);
		//# elif MC_VERSION_NUMBER >= 12002
		//- ClientConfigurationNetworking.registerGlobalReceiver(MWFeatureControlPacket.PAYLOAD_ID, (client, handler, buf, responseSender) -> {
		//- 	onFeatureControlPacket(MWFeatureControlPacket.read(buf));
		//- });
		//- ClientPlayNetworking.registerGlobalReceiver(MWFeatureControlPacket.PAYLOAD_ID, (minecraft, handler, buf, responseSender) -> {
		//- 	onFeatureControlPacket(MWFeatureControlPacket.read(buf));
		//- });
		//# end
		ClientLoginNetworking.registerGlobalReceiver(MWFeatureControlPacket.PAYLOAD_ID, (minecraft, listener, buf, consumer) -> {
			onFeatureControlPacket(MWFeatureControlPacket.read(buf));
			return CompletableFuture.completedFuture(null);
		});
	}

	private static void onFeatureControlPacket(MWFeatureControlPacket packet) {
		MouseWheelie.setFeaturesForSession(packet.getFeatures());
	}

	public static boolean canSendReorderPacket() {
		//# if MC_VERSION_NUMBER >= 12006
		return ClientPlayNetworking.canSend(ReorderInventoryPacket.TYPE);
		//# else
		//- return ClientPlayNetworking.canSend(REORDER_INVENTORY_C2S_PACKET);
		//# end
	}

	public static boolean canSendPickFromInventoryPacket() {
		//# if MC_VERSION_NUMBER >= 12104
		return ClientPlayNetworking.canSend(PickFromInventoryPacket.TYPE);
		//# else
		//- return true;
		//# end
	}

	public static void send(ReorderInventoryPacket packet) {
		//# if MC_VERSION_NUMBER >= 12006
		ClientPlayNetworking.send(packet);
		//# else
		//- FriendlyByteBuf buffer = createBuffer();
		//- packet.write(buffer);
		//- ClientPlayNetworking.send(REORDER_INVENTORY_C2S_PACKET, buffer);
		//# end
	}

	public static void pickFromInventory(int slot) {
		//# if MC_VERSION_NUMBER >= 12104
		if (!canSendPickFromInventoryPacket()) {
			if (MWCompanionDataPackHelper.canPickFromInventory()) {
				MWCompanionDataPackHelper.pickFromInventorySlot(slot - 9);
			} else {
				log.warn("Trying to send pick from inventory packet, but the server doesn't support it");
			}
			return;
		}
		InteractionManager.push(new InteractionManager.PacketEvent(
				new ServerboundCustomPayloadPacket(new PickFromInventoryPacket(slot)),
				InteractionManager.HELD_ITEM_CHANGE_WAITER
		));
		//# else
		//- InteractionManager.push(new InteractionManager.PacketEvent(
		//- 		new ServerboundPickItemPacket(slot),
		//- 		triggerType -> triggerType == InteractionManager.TriggerType.HELD_ITEM_CHANGE
		//- ));
		//# end
	}

	public static synchronized void blockNextGuiUpdateRefillTriggers(int amount) {
		blockNextGuiUpdateRefillTriggers += amount;
	}

	public static synchronized boolean areGuiUpdateRefillTriggersBlocked() {
		return blockNextGuiUpdateRefillTriggers > 0;
	}

	public static synchronized void decrementGuiUpdateRefillTriggerBlocks() {
		blockNextGuiUpdateRefillTriggers--;
	}
}
