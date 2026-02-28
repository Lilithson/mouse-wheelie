package de.siphalor.mousewheelie.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

@Mixin(CreativeModeTabs.class)
public interface CreativeModeTabsAccessor {
	@Accessor
	static void setCACHED_PARAMETERS(CreativeModeTab.ItemDisplayParameters cachedParameters) {

	}
}
