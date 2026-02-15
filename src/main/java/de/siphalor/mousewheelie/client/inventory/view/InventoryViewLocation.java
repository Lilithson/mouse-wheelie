package de.siphalor.mousewheelie.client.inventory.view;

import lombok.Value;

public interface InventoryViewLocation {
	int getInventorySlotId();

	@Value
	class Simple implements InventoryViewLocation {
		int inventorySlotId;
	}

	@Value
	class Bundle implements InventoryViewLocation {
		int inventorySlotId;
		int indexInBundle;
	}
}
