package de.siphalor.mousewheelie.client.inventory;

import de.siphalor.mousewheelie.client.network.ClickEventFactory;
import de.siphalor.mousewheelie.client.util.inject.ISlot;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class PlayerInventoryFocusedContainerScreenHelper<T extends AbstractContainerScreen<?>> extends ContainerScreenHelper<T> {
	protected PlayerInventoryFocusedContainerScreenHelper(T screen, ClickEventFactory clickEventFactory) {
		super(screen, clickEventFactory);
	}

	@Override
	public int getScope(Slot slot, boolean preferSmallerScopes) {
		// In player inventory-focused screens, Vanilla scopes the hotbar separately from the main inventory
		if (slot.container instanceof Inventory) {
			if (isHotbarSlot(slot)) {
				return 0;
			} else {
				int idInContainer = ((ISlot) slot).mouseWheelie_getIndexInInv();
				if (idInContainer < 36) {
					// main inventory
					return 1;
				} else if (idInContainer < 40) {
					// armor slots
					return 2;
				} else {
					// offhand + potentially other stuff
					return -1;
				}
			}
		} else {
			return 3;
		}
	}
}
