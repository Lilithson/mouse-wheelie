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

package de.siphalor.mousewheelie.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
//- import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static de.siphalor.mousewheelie.MouseWheelie.createId;

//# if MC_VERSION_NUMBER >= 12104
/**
 * Replicates the old {@code ServerboundPickItemPacket} that no longer exists in that form since Minecraft 1.21.4.
 */
public record PickFromInventoryPacket(int slot) implements CustomPacketPayload {
	//# if MC_VERSION_NUMBER >= 12111
	public static final Identifier PAYLOAD_ID = createId("pick_from_inventory");
	//# else
	//- public static final ResourceLocation PAYLOAD_ID = createId("pick_from_inventory");
	//# end
	public static final Type<PickFromInventoryPacket> TYPE = new Type<>(PAYLOAD_ID);
	public static final StreamCodec<ByteBuf, PickFromInventoryPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT,
			PickFromInventoryPacket::slot,
			PickFromInventoryPacket::new
	);

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
//# end
