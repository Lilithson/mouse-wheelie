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

package de.siphalor.mousewheelie.client.mixin.gui.screen;

//- import de.siphalor.mousewheelie.client.util.ScrollAction;
//- import de.siphalor.mousewheelie.client.util.inject.IRecipeBookWidget;
//- import de.siphalor.mousewheelie.client.util.inject.IScrollableRecipeBook;
//- import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
//- import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
//- import net.minecraft.client.gui.screens.recipebook.AbstractFurnaceRecipeBookComponent;
//- import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
//- import net.minecraft.network.chat.Component;
//- import net.minecraft.world.entity.player.Inventory;
//- import net.minecraft.world.inventory.AbstractFurnaceMenu;
//- import org.spongepowered.asm.mixin.Final;
//- import org.spongepowered.asm.mixin.Mixin;
//- import org.spongepowered.asm.mixin.Shadow;

//# if MC_VERSION_NUMBER < 12103
//- @Mixin(AbstractFurnaceScreen.class)
//- public abstract class MixinAbstractFurnaceScreen extends AbstractContainerScreen<AbstractFurnaceMenu>
//- 		implements IScrollableRecipeBook {
//- 	public MixinAbstractFurnaceScreen(AbstractFurnaceMenu container, Inventory playerInventory, Component name) {
//- 		super(container, playerInventory, name);
//- 	}

//- 	@Shadow
//- 	@Final
//- 	public AbstractFurnaceRecipeBookComponent recipeBookComponent;

//- 	@Override
//- 	public ScrollAction mouseWheelie_onMouseScrollRecipeBook(double mouseX, double mouseY, double scrollAmount) {
//- 		return ((IRecipeBookWidget) recipeBookComponent).mouseWheelie_scrollRecipeBook(mouseX, mouseY, scrollAmount);
//- 	}
//- }
//# end
