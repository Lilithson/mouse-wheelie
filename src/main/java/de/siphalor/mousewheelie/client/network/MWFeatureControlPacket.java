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

package de.siphalor.mousewheelie.client.network;

import de.siphalor.mousewheelie.MWFeature;
import io.netty.buffer.ByteBuf;
import lombok.CustomLog;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import lombok.Value;
//- import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.Identifier;

import java.util.*;

import static de.siphalor.mousewheelie.MouseWheelie.createId;

@Value
@CustomLog
public class MWFeatureControlPacket
		//# if MC_VERSION_NUMBER >= 12006
		implements CustomPacketPayload
		//# end
{
	//# if MC_VERSION_NUMBER >= 12111
	public static final Identifier PAYLOAD_ID = createId("feature_control");
	//# else
	//- public static final ResourceLocation PAYLOAD_ID = createId("feature_control");
	//# end

	//# if MC_VERSION_NUMBER >= 12006
	public static final Type<MWFeatureControlPacket> TYPE = new Type<>(PAYLOAD_ID);
	public static final StreamCodec<ByteBuf, MWFeatureControlPacket> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public MWFeatureControlPacket decode(ByteBuf buf) {
			return read(new FriendlyByteBuf(buf));
		}

		@Override
		public void encode(ByteBuf buf, MWFeatureControlPacket packet) {
			throw new UnsupportedOperationException();
		}
	};

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
	//# end

	public static MWFeatureControlPacket read(FriendlyByteBuf byteBuf) {
		if (!byteBuf.isReadable()) {
			return new MWFeatureControlPacket(EnumSet.noneOf(MWFeature.class));
		}
		try {
			int size = byteBuf.readVarInt();
			EnumSet<MWFeature> enabledFeatures = EnumSet.noneOf(MWFeature.class);
			boolean anyEnabled = false;
			EnumSet<MWFeature> disabledFeatures = EnumSet.noneOf(MWFeature.class);
			for (int i = 0; i < size; i++) {
				String name = byteBuf.readUtf(32).toLowerCase(Locale.ROOT);
				if (name.startsWith("!")) {
					MWFeature.of(name.substring(1)).ifPresent(disabledFeatures::add);
				} else {
					MWFeature.of(name).ifPresent(enabledFeatures::add);
					anyEnabled = true;
				}
			}
			if (anyEnabled) {
				return new MWFeatureControlPacket(enabledFeatures);
			} else {
				return new MWFeatureControlPacket(EnumSet.complementOf(disabledFeatures));
			}
		} catch (Exception e) {
			log.warn("Got invalid feature control package from server; turning off all Mouse Wheelie features");
			return new MWFeatureControlPacket(EnumSet.noneOf(MWFeature.class));
		}
	}

	EnumSet<MWFeature> features;

}
