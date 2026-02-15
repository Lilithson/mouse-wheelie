package de.siphalor.mousewheelie.client.inventory.view;

import lombok.Value;

import net.minecraft.world.item.ItemStack;

@Value
public class InventoryViewEntry {
	InventoryViewLocation location;
	ItemStack stack;
}
