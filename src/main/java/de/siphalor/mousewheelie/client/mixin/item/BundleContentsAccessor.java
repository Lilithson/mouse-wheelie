package de.siphalor.mousewheelie.client.mixin.item;

import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;

//# if MC_VERSION_NUMBER >= 12103
@Mixin(BundleContents.class)
public interface BundleContentsAccessor {
	@Invoker
	static Fraction callGetWeight(ItemStack stack) {
		return Fraction.ONE;
	}
}
//# end
