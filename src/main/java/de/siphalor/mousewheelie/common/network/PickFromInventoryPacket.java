package de.siphalor.mousewheelie.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static de.siphalor.mousewheelie.MouseWheelie.createId;

//# if MC_VERSION_NUMBER >= 12104
/**
 * Replicates the old {@code ServerboundPickItemPacket} that no longer exists in that form since Minecraft 1.21.4.
 */
public record PickFromInventoryPacket(int slot) implements CustomPacketPayload {
	public static final ResourceLocation PAYLOAD_ID = createId("pick_from_inventory");
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
