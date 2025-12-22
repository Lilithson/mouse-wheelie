package de.siphalor.mousewheelie.client.compat;

import de.siphalor.mousewheelie.client.network.InteractionManager;
import lombok.CustomLog;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;

//# if MC_VERSION_NUMBER >= 12104
@CustomLog
public class MWCompanionDataPackHelper {
	public static final String DATA_PACK_ID = "mousewheelie_companion";
	public static final String DATA_PACK_NAME = "Mouse Wheelie Companion";

	public static final String VERSION_INFO_TRIGGER = DATA_PACK_ID + "_version_info";
	public static final String PICK_FROM_INVENTORY_TRIGGER = DATA_PACK_ID + "_pick_from_inventory";

	private static final int COMMAND_SUGGESTIONS_PACKET_ID = -302533433;

	private static boolean available;

	public static void updateAvailability() {
		log.info("Checking for availability of {} data pack", DATA_PACK_NAME);

		available = false;

		//noinspection DataFlowIssue
		Minecraft.getInstance().getConnection().send(new ServerboundCommandSuggestionPacket(
				COMMAND_SUGGESTIONS_PACKET_ID,
				"trigger " + DATA_PACK_ID
		));
	}

	public static boolean handleAvailabilityTriggerCommandSuggestions(
			ClientboundCommandSuggestionsPacket packet
	) {
		if (packet.id() != COMMAND_SUGGESTIONS_PACKET_ID) {
			return false;
		}

		available = packet.suggestions().stream()
				.anyMatch(suggestion -> suggestion.text().equals(VERSION_INFO_TRIGGER));

		if (available) {
			log.info("{} data pack is present on server", DATA_PACK_NAME);
		} else {
			log.info("{} data pack is not present on the server, consider installing it for the full Mouse Wheelie feature set", DATA_PACK_NAME);
		}
		return true;
	}

	public static boolean canPickFromInventory() {
		return available;
	}

	public static void pickFromInventorySlot(int slot) {
		InteractionManager.push(() -> InteractionManager.HELD_ITEM_CHANGE_WAITER);
		//noinspection DataFlowIssue
		Minecraft.getInstance().getConnection().send(new ServerboundChatCommandPacket(
				"trigger " + PICK_FROM_INVENTORY_TRIGGER + " set " + slot
		));
	}
}
//# end
