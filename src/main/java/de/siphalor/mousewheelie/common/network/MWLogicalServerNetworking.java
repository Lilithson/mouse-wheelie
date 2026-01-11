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

package de.siphalor.mousewheelie.common.network;

import de.siphalor.mousewheelie.MouseWheelie;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.CustomLog;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

//- import net.minecraft.network.FriendlyByteBuf;
//- import net.minecraft.network.protocol.PacketUtils;
//- import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
//- import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Class that handles functionality on the logical server side.
 */
@CustomLog
public class MWLogicalServerNetworking extends MWNetworking {

	private MWLogicalServerNetworking() {}

	public static void setup() {
		//# if MC_VERSION_NUMBER >= 12006
		ServerPlayNetworking.registerGlobalReceiver(
				ReorderInventoryPacket.TYPE,
				(payload, context) -> onReorderInventoryPacket(payload, context.server(), context.player())
		);
		//# if MC_VERSION_NUMBER >= 12104
		ServerPlayNetworking.registerGlobalReceiver(
				PickFromInventoryPacket.TYPE,
				(payload, context) -> onPickFromInventoryPacket(payload, context.player(), context.responseSender())
		);
		//# end
		//# else
		//- ServerPlayNetworking.registerGlobalReceiver(REORDER_INVENTORY_C2S_PACKET, MWLogicalServerNetworking::onReorderInventoryPacket);
		//# end
	}

	//# if MC_VERSION_NUMBER >= 12006
	//# else
	//- private static void onReorderInventoryPacket(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
		//- onReorderInventoryPacket(ReorderInventoryPacket.read(buf), server, player);
	//- }
	//# end

	private static void onReorderInventoryPacket(ReorderInventoryPacket packet, MinecraftServer server, ServerPlayer player) {
		if (packet == null) {
			log.warn("Failed to read reorder inventory packet from player {}!", player);
			return;
		}

		if (player.containerMenu == null) {
			log.warn("Player {} tried to reorder inventory without having an open container!", player);
			return;
		}

		if (packet.getSyncId() == player.inventoryMenu.containerId) {
			server.execute(() -> reorder(player, player.inventoryMenu, packet.getSlotMappings()));
		} else if (packet.getSyncId() == player.containerMenu.containerId) {
			server.execute(() -> reorder(player, player.containerMenu, packet.getSlotMappings()));
		}
	}

	private static void reorder(Player player, AbstractContainerMenu screenHandler, int[] slotMapping) {
		if (!checkReorder(player, screenHandler, slotMapping)) {
			log.warn("Reorder inventory packet from player {} contains invalid data, ignoring!", player);
			return;
		}

		List<ItemStack> stacks = screenHandler.slots.stream().map(Slot::getItem).collect(Collectors.toList());

		for (int i = 0; i < slotMapping.length; i += 2) {
			int originSlotId = slotMapping[i];
			int destSlotId = slotMapping[i + 1];

			screenHandler.slots.get(destSlotId).setByPlayer(stacks.get(originSlotId));
		}
	}

	private static boolean checkReorder(Player player, AbstractContainerMenu screenHandler, int[] slotMappings) {
		if (slotMappings.length < 4) {
			log.warn("Reorder inventory packet contains too few slots!");
			return false;
		}

		IntSet requestedSlots = new IntAVLTreeSet();
		Container targetInv;

		Slot firstSlot = screenHandler.slots.get(slotMappings[0]);
		targetInv = firstSlot.container;

		for (int i = 0; i < slotMappings.length; i += 2) {
			int originSlotId = slotMappings[i];
			int destSlotId = slotMappings[i + 1];

			if (!checkReorderSlot(screenHandler, originSlotId, targetInv)) {
				return false;
			}
			if (!requestedSlots.add(originSlotId)) {
				log.warn("Reorder inventory packet contains duplicate origin slot {}!", originSlotId);
				return false;
			}

			if (!checkReorderSlot(screenHandler, destSlotId, targetInv)) {
				return false;
			}

			if (originSlotId == destSlotId) {
				continue;
			}

			Slot originSlot = screenHandler.getSlot(originSlotId);
			if (!originSlot.mayPickup(player)) {
				log.warn("Player {} tried to reorder slot {}, but that slot doesn't allow taking items!", player, originSlotId);
				return false;
			}
			Slot destSlot = screenHandler.getSlot(destSlotId);
			if (!destSlot.mayPlace(originSlot.getItem())) {
				log.warn("Player {} tried to reorder slot {}, but that slot doesn't allow inserting the origin stack!", player, destSlotId);
				return false;
			}
		}

		for (int i = 1; i < slotMappings.length; i += 2) {
			int destSlotId = slotMappings[i];
			if (!requestedSlots.remove(destSlotId)) {
				log.warn("Reorder inventory packet contains duplicate destination slot or slot without origin: {}!", i);
				return false;
			}
		}
		if (!requestedSlots.isEmpty()) {
			log.error("Invalid state during checking reorder packet, please report this to the {} bug tracker. Requested slots: {}", MouseWheelie.MOD_NAME, requestedSlots);
			return false;
		}
		return true;
	}

	private static boolean checkReorderSlot(AbstractContainerMenu screenHandler, int slotId, Container targetInv) {
		Slot slot = screenHandler.getSlot(slotId);
		if (slot == null) {
			log.warn("Reorder inventory packet contains invalid slot id!");
			return false;
		}

		if (targetInv != slot.container) {
			log.warn("Reorder inventory packet contains slots from different inventories, first: {}, now: {}!", targetInv, slot.container);
			return false;
		}
		return true;
	}

	//# if MC_VERSION_NUMBER >= 12104
	private static void onPickFromInventoryPacket(PickFromInventoryPacket packet, Player player, PacketSender sender) {
		Inventory inventory = player.getInventory();
		inventory.pickSlot(packet.slot());
		//# if MC_VERSION_NUMBER >= 12108
		int selected = inventory.getSelectedSlot();
		//# else
		//- int selected = inventory.selected;
		//# end
		sender.sendPacket(inventory.createInventoryUpdatePacket(selected));
		sender.sendPacket(inventory.createInventoryUpdatePacket(packet.slot()));
		sender.sendPacket(new ClientboundSetHeldSlotPacket(selected));
	}
	//# end
}
