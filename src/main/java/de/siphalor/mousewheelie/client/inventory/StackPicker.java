package de.siphalor.mousewheelie.client.inventory;

import de.siphalor.mousewheelie.client.network.InteractionManager;
import lombok.RequiredArgsConstructor;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;

@RequiredArgsConstructor
public class StackPicker {
	private static final int HOTBAR_START_SLOT = InventoryMenu.USE_ROW_SLOT_START;

	private final Player player;

	public void pick(int slot, TargetMode targetMode) {
		swapWithSlot(slot);
		swapWithHotbar(targetMode);
		swapWithSlot(slot);
	}

	private void swapWithHotbar(TargetMode targetMode) {
		if (targetMode == TargetMode.OFFHAND) {
			swapWithSlot(InventoryMenu.SHIELD_SLOT);
			return;
		} else if (targetMode == TargetMode.PREFER_EMPTY_HOTBAR_SLOTS) {
			//# if MC_VERSION_NUMBER >= 12108
			int selectedSlot = player.getInventory().getSelectedSlot();
			//# else
			//- int selectedSlot = player.getInventory().selected;
			//# end
			for (int offset = 0; offset < 9; offset++) {
				int hotbarSlot = (selectedSlot + offset) % 9;
				if (player.getInventory().getItem(hotbarSlot).isEmpty()) {
					swapWithSlot(HOTBAR_START_SLOT + hotbarSlot);
					if (hotbarSlot != selectedSlot) {
						//# if MC_VERSION_NUMBER >= 12108
						player.getInventory().setSelectedSlot(hotbarSlot);
						//# else
						//- player.getInventory().selected = hotbarSlot;
						//# end
						Minecraft.getInstance().getConnection().send(new ServerboundSetCarriedItemPacket(hotbarSlot));
					}
					return;
				}
			}
		}

		//# if MC_VERSION_NUMBER >= 12108
		swapWithSlot(HOTBAR_START_SLOT + player.getInventory().getSelectedSlot());
		//# else
		//- swapWithSlot(HOTBAR_START_SLOT + player.getInventory().selected);
		//# end
	}

	private void swapWithSlot(int slot) {
		InteractionManager.push(new InteractionManager.ClickEvent(
				player.inventoryMenu.containerId,
				slot,
				player.getInventory().getItem(slot).isEmpty() ? 0 : 1,
				ClickType.PICKUP
		));
	}

	public enum TargetMode {
		PREFER_EMPTY_HOTBAR_SLOTS,
		KEEP_SELECTED_HOTBAR_SLOT,
		OFFHAND,
	}
}
