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

package de.siphalor.mousewheelie.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.coat.screen.ConfigScreen;
import de.siphalor.coat.util.EnumeratedMaterial;
import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.inventory.ToolPicker;
import de.siphalor.mousewheelie.client.inventory.sort.SortMode;
import de.siphalor.mousewheelie.client.keybinding.*;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.CreativeSearchOrder;
import de.siphalor.mousewheelie.client.util.ScrollAction;
import de.siphalor.mousewheelie.client.util.inject.IContainerScreen;
import de.siphalor.mousewheelie.client.util.inject.IScrollableRecipeBook;
import de.siphalor.mousewheelie.client.util.inject.ISpecialScrollableScreen;
import de.siphalor.tweed5.coat.bridge.api.ConfigScreenCreateParams;
import de.siphalor.tweed5.coat.bridge.api.TweedCoatBridgeExtension;
import de.siphalor.tweed5.coat.bridge.api.TweedCoatMappers;
import de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
//- import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.Locale;

import static de.siphalor.mousewheelie.MouseWheelie.createId;
import static de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension.presetValue;

@Environment(EnvType.CLIENT)
@SuppressWarnings("WeakerAccess")
public class MWClient implements ClientModInitializer {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	public static final String KEY_BINDING_CATEGORY = "key.categories." + MouseWheelie.MOD_ID;

	public static final KeyMapping OPEN_CONFIG_SCREEN = new OpenConfigScreenKeybinding(createId("open_config_screen"), InputConstants.Type.KEYSYM, -1, KEY_BINDING_CATEGORY, new KeyModifiers());
	public static final KeyMapping SORT_KEY_BINDING = new SortKeyBinding(createId("sort_inventory"), InputConstants.Type.MOUSE, 2, KEY_BINDING_CATEGORY, new KeyModifiers());
	public static final KeyMapping SCROLL_UP_KEY_BINDING = new ScrollKeyBinding(createId("scroll_up"), KEY_BINDING_CATEGORY, false);
	public static final KeyMapping SCROLL_DOWN_KEY_BINDING = new ScrollKeyBinding(createId("scroll_down"), KEY_BINDING_CATEGORY, true);
	public static final KeyMapping PICK_TOOL_KEY_BINDING = new PickToolKeyBinding(createId("pick_tool"), InputConstants.Type.KEYSYM, -1, KEY_BINDING_CATEGORY, new KeyModifiers());
	public static final ActionModifierKeybinding WHOLE_STACK_MODIFIER = new ActionModifierKeybinding(createId("whole_stack_modifier"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_SHIFT, KEY_BINDING_CATEGORY, new KeyModifiers());
	public static final ActionModifierKeybinding ALL_OF_KIND_MODIFIER = new ActionModifierKeybinding(createId("all_of_kind_modifier"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_CONTROL, KEY_BINDING_CATEGORY, new KeyModifiers());
	public static final ActionModifierKeybinding DROP_MODIFIER = new ActionModifierKeybinding(createId("drop_modifier"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, KEY_BINDING_CATEGORY, new KeyModifiers());
	public static final ActionModifierKeybinding DEPOSIT_MODIFIER = new ActionModifierKeybinding(createId("deposit_modifier"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_SPACE, KEY_BINDING_CATEGORY, new KeyModifiers());
	public static final ActionModifierKeybinding RESTOCK_MODIFIER = new ActionModifierKeybinding(createId("restock_modifier"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_SPACE, KEY_BINDING_CATEGORY, new KeyModifiers());

	public static int lastUpdatedSlot = -1;

	@Override
	public void onInitializeClient() {
		KeyBindingHelper.registerKeyBinding(OPEN_CONFIG_SCREEN);
		KeyBindingHelper.registerKeyBinding(SORT_KEY_BINDING);
		KeyBindingHelper.registerKeyBinding(SCROLL_UP_KEY_BINDING);
		KeyBindingHelper.registerKeyBinding(SCROLL_DOWN_KEY_BINDING);
		KeyBindingHelper.registerKeyBinding(PICK_TOOL_KEY_BINDING);

		KeyBindingHelper.registerKeyBinding(WHOLE_STACK_MODIFIER);
		KeyBindingHelper.registerKeyBinding(ALL_OF_KIND_MODIFIER);
		KeyBindingHelper.registerKeyBinding(DROP_MODIFIER);
		KeyBindingHelper.registerKeyBinding(DEPOSIT_MODIFIER);
		KeyBindingHelper.registerKeyBinding(RESTOCK_MODIFIER);

		//# if MC_VERSION_NUMBER < 12104
		//- ClientPickBlockGatherCallback.EVENT.register(MWClient::triggerPick);
		//# end

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			CreativeSearchOrder.refreshItemSearchPositionLookup();
			updateTickRate();
		});
	}

	public static boolean isTool(ItemStack stack) {
		//# if MC_VERSION_NUMBER >= 12103
		return stack.has(DataComponents.TOOL);
		//# else
		//- // TODO: reimplement Fapi tool tags
		//- return item instanceof TieredItem || item instanceof ShearsItem;
		//# end
	}

	public static boolean isWeapon(ItemStack stack) {
		//# if MC_VERSION_NUMBER >= 12103
		return stack.getItem() instanceof ProjectileWeaponItem
				|| stack.getItem() instanceof TridentItem
				|| stack.is(ItemTags.SHARP_WEAPON_ENCHANTABLE);
		//# else
		//- return item instanceof ProjectileWeaponItem || item instanceof TridentItem || item instanceof SwordItem;
		//# end
	}

	public static double getMouseX() {
		return CLIENT.mouseHandler.xpos() * (double) CLIENT.getWindow().getGuiScaledWidth() / (double) CLIENT.getWindow().getScreenWidth();
	}

	public static double getMouseY() {
		return CLIENT.mouseHandler.ypos() * (double) CLIENT.getWindow().getGuiScaledHeight() / (double) CLIENT.getWindow().getScreenHeight();
	}

	public static void onConfigChanged() {
		CreativeSearchOrder.refreshItemSearchPositionLookup();
		updateTickRate();
	}

	private static void updateTickRate() {
		if (isOnLocalServer()) {
			InteractionManager.setTickRate(MouseWheelie.config.general.integratedInteractionRate);
		} else {
			InteractionManager.setTickRate(MouseWheelie.config.general.interactionRate);
		}
	}

	public static boolean isOnLocalServer() {
		return CLIENT.getSingleplayerServer() != null;
	}

	public static boolean triggerScroll(double mouseX, double mouseY, double scrollY) {
		double scrollAmount = scrollY * CLIENT.options.mouseWheelSensitivity().get();
		ScrollAction result;
		if (CLIENT.screen instanceof ISpecialScrollableScreen) {
			result = ((ISpecialScrollableScreen) CLIENT.screen).mouseWheelie_onMouseScrolledSpecial(mouseX, mouseY, scrollAmount);
			if (result.cancelsCustomActions()) {
				return result.cancelsAllActions();
			}
		}
		if (CLIENT.screen instanceof IContainerScreen) {
			result = ((IContainerScreen) CLIENT.screen).mouseWheelie_onMouseScroll(mouseX, mouseY, scrollY);
			if (result.cancelsCustomActions()) {
				return result.cancelsAllActions();
			}
		}
		if (CLIENT.screen instanceof IScrollableRecipeBook) {
			result = ((IScrollableRecipeBook) CLIENT.screen).mouseWheelie_onMouseScrollRecipeBook(mouseX, mouseY, scrollY);
			if (result.cancelsCustomActions()) {
				return result.cancelsAllActions();
			}
		}
		return false;
	}

	//# if MC_VERSION_NUMBER >= 12104
	public static boolean triggerPick(Player player, HitResult hitResult) {
	//# else
	//- public static ItemStack onPick(Player player, HitResult hitResult) {
	//# end
		ItemStack stack = player.getMainHandItem();
		Item item = stack.getItem();
		//# if MC_VERSION_NUMBER < 12104
		//- int index = -1;
		//# end
		if (MouseWheelie.config.toolPicking.holdTool && (isTool(stack) || isWeapon(stack))) {
			ToolPicker toolPicker = new ToolPicker(player.getInventory());
			if (hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult) {
				BlockState blockState = player.level().getBlockState(((BlockHitResult) hitResult).getBlockPos());
				//# if MC_VERSION_NUMBER >= 12104
				if (toolPicker.pickToolFor(blockState)) {
					return true;
				}
				//# else
				//- index = toolPicker.findToolFor(blockState);
				//# end
			//# if MC_VERSION_NUMBER >= 12104
			} else if (toolPicker.pickWeapon()) {
				return true;
			//# else
			//- } else {
			//- 	index = toolPicker.findWeapon();
			//# end
			}
		}
		if (MouseWheelie.config.toolPicking.holdBlock && item instanceof BlockItem && hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult) {
			BlockState blockState = player.level().getBlockState(((BlockHitResult) hitResult).getBlockPos());
			if (blockState.getBlock() == ((BlockItem) item).getBlock()) {
				ToolPicker toolPicker = new ToolPicker(player.getInventory());
				//# if MC_VERSION_NUMBER >= 12104
				return toolPicker.pickToolFor(blockState);
				//# else
				//- index = toolPicker.findToolFor(blockState);
				//# end
			}
		}
		//# if MC_VERSION_NUMBER >= 12104
		return false;
		//# else
		//- return index == -1 || index == player.getInventory().selected ? ItemStack.EMPTY : player.getInventory().getItem(index);
		//# end
	}

	public static ConfigScreen createConfigScreen() {
		TweedCoatBridgeExtension coatBridge = MouseWheelie.configContainerHelper.configContainer().extension(TweedCoatBridgeExtension.class)
				.orElseThrow(() -> new IllegalStateException("Failed to get TweedCoatBridgeExtension"));

		Arrays.asList(
				TweedCoatMappers.booleanCheckboxMapper(),
				TweedCoatMappers.integerTextMapper(),
				TweedCoatMappers.enumCycleButtonMapper(),
				TweedCoatMappers.enumeratedMaterialCycleButtonMapper(SortMode.class, new EnumeratedMaterial<>() {
					@Override
					public SortMode[] values() {
						return SortMode.getAll().toArray(new SortMode[0]);
					}

					@Override
					public Component asText(SortMode sortMode) {
						return Component.translatable("mousewheelie.sortmode." + sortMode.name().toLowerCase(Locale.ROOT));
					}
				}),
				TweedCoatMappers.compoundCategoryMapper()
		).forEach(coatBridge::addMapper);

		MWConfig defaultValue = MouseWheelie.configContainerHelper.configContainer().rootEntry()
				.call(presetValue(PresetsExtension.DEFAULT_PRESET_NAME));

		return coatBridge.createConfigScreen(ConfigScreenCreateParams.<MWConfig>builder()
				.rootEntry(MouseWheelie.configContainerHelper.configContainer().rootEntry())
				.currentValue(MouseWheelie.config)
				.defaultValue(defaultValue)
				.title(Component.translatable("tweed4_tailor_screen.screen.mousewheelie"))
				.translationKeyPrefix("tweed4_tailor_screen.screen.mousewheelie")
				.saveHandler(value -> {
					MouseWheelie.config = value;
					MouseWheelie.configContainerHelper.writeConfigInConfigDirectory(value);
					MWClient.onConfigChanged();
				})
				.build());
	}
}
