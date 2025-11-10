package de.siphalor.mousewheelie.client.mixin.gui.screen;

import de.siphalor.mousewheelie.client.util.ScrollAction;
import de.siphalor.mousewheelie.client.util.inject.IRecipeBookWidget;
import de.siphalor.mousewheelie.client.util.inject.ISpecialScrollableScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

//# if MC_VERSION_NUMBER >= 12103
@Mixin(AbstractRecipeBookScreen.class)
public class MixinAbstractRecipeBookScreen implements ISpecialScrollableScreen {
	@Shadow
	@Final
	private RecipeBookComponent<?> recipeBookComponent;

	@Override
	public ScrollAction mouseWheelie_onMouseScrolledSpecial(double mouseX, double mouseY, double scrollAmount) {
		return ((IRecipeBookWidget) recipeBookComponent).mouseWheelie_scrollRecipeBook(mouseX, mouseY, scrollAmount);
	}
}
//# end
