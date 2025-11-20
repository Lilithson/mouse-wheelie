package de.siphalor.mousewheelie.client.mixin;

import net.minecraft.client.Minecraft;
//- import net.minecraft.client.color.item.ItemColors;
import org.spongepowered.asm.mixin.Mixin;
//- import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftClientAccessor {
	//# if MC_VERSION_NUMBER >= 12006 && MC_VERSION_NUMBER < 12104
	//- @Accessor
	//- ItemColors getItemColors();
	//# end
}
