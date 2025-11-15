/*
 * Copyright 2020 Siphalor and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.siphalor.mousewheelie.client.mixin.gui.other;

import de.siphalor.mousewheelie.client.util.inject.ISpecialClickableButtonWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractWidget.class)
public abstract class MixinAbstractButtonWidget {
	@Shadow
	public abstract boolean isMouseOver(double x, double y);

	@Inject(
			method = "mouseClicked",
			//# if MC_VERSION_NUMBER >= 12109
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/AbstractWidget;isValidClickButton(Lnet/minecraft/client/input/MouseButtonInfo;)Z"),
			//# else
			//- at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/AbstractWidget;isValidClickButton(I)Z"),
			//# end
			cancellable = true
	)
	//# if MC_VERSION_NUMBER >= 12109
	public void mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
		int button = event.button();
		double x = event.x();
		double y = event.y();
	//# else
	//- public void mouseClicked(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
	//# end
		if (this.isMouseOver(x, y)) {
			if (this instanceof ISpecialClickableButtonWidget) {
				if (((ISpecialClickableButtonWidget) this).mouseWheelie_mouseClickedSpecial(button)) {
					cir.setReturnValue(true);
				}
			}
		}
	}
}
