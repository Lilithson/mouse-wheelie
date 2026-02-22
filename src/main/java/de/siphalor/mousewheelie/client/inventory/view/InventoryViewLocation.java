package de.siphalor.mousewheelie.client.inventory.view;

import lombok.Value;

public interface InventoryViewLocation {
	int getInventorySlotId();

	@Value
	class Simple implements InventoryViewLocation {
		int inventorySlotId;
	}

	//# if MC_VERSION_NUMBER >= 12103
	@Value
	class Bundle implements InventoryViewLocation {
		int inventorySlotId;
		int indexInBundle;
	}
	//# end
}
