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

//- import de.siphalor.mousewheelie.MouseWheelie;
//- import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
//- import net.minecraft.network.FriendlyByteBuf;
//- import net.minecraft.resources.ResourceLocation;

//- import static de.siphalor.mousewheelie.MouseWheelie.createId;

public class MWNetworking {
	protected MWNetworking() {}

	public static void setup() {
		//# if MC_VERSION_NUMBER >= 12006
		PayloadTypeRegistry.playC2S().register(ReorderInventoryPacket.TYPE, ReorderInventoryPacket.STREAM_CODEC);
		//# end
	}
	//# if MC_VERSION_NUMBER < 12006
	//- protected static final ResourceLocation REORDER_INVENTORY_C2S_PACKET = createId("reorder_inventory_c2s");

	//- public static FriendlyByteBuf createBuffer() {
	//- 	return new FriendlyByteBuf(Unpooled.buffer());
	//- }
	//# end
}
