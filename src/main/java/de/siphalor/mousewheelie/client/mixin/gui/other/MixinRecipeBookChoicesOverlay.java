package de.siphalor.mousewheelie.client.mixin.gui.other;

import de.siphalor.mousewheelie.MouseWheelie;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(OverlayRecipeComponent.class)
public class MixinRecipeBookChoicesOverlay {
	@ModifyVariable(
			method = "mouseClicked",
			at = @At("HEAD"),
			argsOnly = true
	)
	//# if MC_VERSION_NUMBER >= 12109
	public MouseButtonEvent modifyMouseClickedEvent(MouseButtonEvent event) {
		if (event.button() == 1 && MouseWheelie.config.general.enableQuickCraft) {
			return new MouseButtonEvent(event.x(), event.y(), new MouseButtonInfo(0, event.modifiers()));
		}
		return event;
	}
	//# else
	//- public int modifyMouseClickedButton(int button) {
	//- 	if (button == 1 && MouseWheelie.config.general.enableQuickCraft) {
	//- 		return 0;
	//- 	}
	//- 	return button;
	//- }
	//# end
}
