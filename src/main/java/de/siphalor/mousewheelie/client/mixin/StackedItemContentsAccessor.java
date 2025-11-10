package de.siphalor.mousewheelie.client.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//# if MC_VERSION_NUMBER >= 12103
@Mixin(StackedItemContents.class)
public interface StackedItemContentsAccessor {
	@Accessor
	StackedContents<Holder<Item>> getRaw();
}
//# end
