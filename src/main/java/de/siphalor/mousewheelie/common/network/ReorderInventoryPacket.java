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
import io.netty.buffer.ByteBuf;
import lombok.CustomLog;
import lombok.Value;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value
@CustomLog
public class ReorderInventoryPacket
		//# if MC_VERSION_NUMBER >= 12006
		implements CustomPacketPayload
		//# end
{
	//# if MC_VERSION_NUMBER >= 12006
	public static final ResourceLocation PAYLOAD_ID = new ResourceLocation(MouseWheelie.MOD_ID, "reorder_inventory");
	public static final Type<ReorderInventoryPacket> TYPE = new Type<>(PAYLOAD_ID);
	private static final int MAX_SLOTS = 2048;
	private static final StreamCodec<ByteBuf, int[]> INT_ARRAY_STREAM_CODEC = new StreamCodec<>() {
		@Override
		public void encode(ByteBuf byteBuf, int[] array) {
			ByteBufCodecs.writeCount(byteBuf, array.length, MAX_SLOTS);
			for (int value : array) {
				VarInt.write(byteBuf, value);
			}
		}

		@Override
		public int @NotNull [] decode(ByteBuf byteBuf) {
			int length = ByteBufCodecs.readCount(byteBuf, MAX_SLOTS);
			int[] array = new int[length];
			for (int i = 0; i < length; i++) {
				array[i] = VarInt.read(byteBuf);
			}
			return array;
		}
	};

	public static final StreamCodec<ByteBuf, ReorderInventoryPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT,
			ReorderInventoryPacket::getSyncId,
			INT_ARRAY_STREAM_CODEC,
			ReorderInventoryPacket::getSlotMappings,
			ReorderInventoryPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
	//# end

	int syncId;
	int[] slotMappings;

	public void write(@NotNull FriendlyByteBuf buf) {
		buf.writeVarInt(syncId);
		buf.writeVarIntArray(slotMappings);
	}

	public static @Nullable ReorderInventoryPacket read(FriendlyByteBuf buf) {
		int syncId = buf.readVarInt();
		int[] reorderedIndices = buf.readVarIntArray();

		if (reorderedIndices.length % 2 != 0) {
			log.warn("Received reorder inventory packet with invalid data!");
			return null;
		}

		return new ReorderInventoryPacket(syncId, reorderedIndices);
	}
}
