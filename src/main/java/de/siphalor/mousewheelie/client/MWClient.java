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
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.Locale;

import static de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension.presetValue;

@Environment(EnvType.CLIENT)
@SuppressWarnings("WeakerAccess")
public class MWClient implements ClientModInitializer {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	public static final String KEY_BINDING_CATEGORY = "key.categories." + MouseWheelie.MOD_ID;

	public static final KeyMapping OPEN_CONFIG_SCREEN = new OpenConfigScreenKeybinding(new ResourceLocation(MouseWheelie.MOD_ID, "open_config_screen"), InputConstants.Type.KEYSYM, -1, KEY_BINDING_CATEGORY, new KeyModifiers());
	public static final KeyMapping SORT_KEY_BINDING = new SortKeyBinding(new ResourceLocation(MouseWheelie.MOD_ID, "sort_inventory"), InputConstants.Type.MOUSE, 2, KEY_BINDING_CATEGORY, new KeyModifiers());
	public static final KeyMapping SCROLL_UP_KEY_BINDING = new ScrollKeyBinding(new ResourceLocation(MouseWheelie.MOD_ID, "scroll_up"), KEY_BINDING_CATEGORY, false);
	public static final KeyMapping SCROLL_DOWN_KEY_BINDING = new ScrollKeyBinding(new ResourceLocation(MouseWheelie.MOD_ID, "scroll_down"), KEY_BINDING_CATEGORY, true);
	public static final KeyMapping PICK_TOOL_KEY_BINDING = new PickToolKeyBinding(new ResourceLocation(MouseWheelie.MOD_ID, "pick_tool"), InputConstants.Type.KEYSYM, -1, KEY_BINDING_CATEGORY, new KeyModifiers());
	public static final ActionModifierKeybinding WHOLE_STACK_MODIFIER = new ActionModifierKeybinding(new ResourceLocation(MouseWheelie.MOD_ID, "whole_stack_modifier"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_SHIFT, KEY_BINDING_CATEGORY, new KeyModifiers());
	public static final ActionModifierKeybinding ALL_OF_KIND_MODIFIER = new ActionModifierKeybinding(new ResourceLocation(MouseWheelie.MOD_ID, "all_of_kind_modifier"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_CONTROL, KEY_BINDING_CATEGORY, new KeyModifiers());
	public static final ActionModifierKeybinding DROP_MODIFIER = new ActionModifierKeybinding(new ResourceLocation(MouseWheelie.MOD_ID, "drop_modifier"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, KEY_BINDING_CATEGORY, new KeyModifiers());
	public static final ActionModifierKeybinding DEPOSIT_MODIFIER = new ActionModifierKeybinding(new ResourceLocation(MouseWheelie.MOD_ID, "deposit_modifier"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_SPACE, KEY_BINDING_CATEGORY, new KeyModifiers());
	public static final ActionModifierKeybinding RESTOCK_MODIFIER = new ActionModifierKeybinding(new ResourceLocation(MouseWheelie.MOD_ID, "restock_modifier"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_SPACE, KEY_BINDING_CATEGORY, new KeyModifiers());

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

		ClientPickBlockGatherCallback.EVENT.register((player, result) -> {
			Item item = player.getMainHandItem().getItem();
			int index = -1;
			if (MouseWheelie.config.toolPicking.holdTool && (isTool(item) || isWeapon(item))) {
				ToolPicker toolPicker = new ToolPicker(player.getInventory());
				if (result.getType() == HitResult.Type.BLOCK && result instanceof BlockHitResult) {
					index = toolPicker.findToolFor(player.level().getBlockState(((BlockHitResult) result).getBlockPos()));
				} else {
					index = toolPicker.findWeapon();
				}
			}
			if (MouseWheelie.config.toolPicking.holdBlock && item instanceof BlockItem && result.getType() == HitResult.Type.BLOCK && result instanceof BlockHitResult) {
				BlockState blockState = player.level().getBlockState(((BlockHitResult) result).getBlockPos());
				if (blockState.getBlock() == ((BlockItem) item).getBlock()) {
					ToolPicker toolPicker = new ToolPicker(player.getInventory());
					index = toolPicker.findToolFor(blockState);
				}
			}
			return index == -1 || index == player.getInventory().selected ? ItemStack.EMPTY : player.getInventory().getItem(index);
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			CreativeSearchOrder.refreshItemSearchPositionLookup();
			updateTickRate();
		});
	}

	public static boolean isTool(Item item) {
		// TODO: reimplement Fapi tool tags
		return item instanceof TieredItem || item instanceof ShearsItem;
	}

	public static boolean isWeapon(Item item) {
		return item instanceof ProjectileWeaponItem || item instanceof TridentItem || item instanceof SwordItem;
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
				})
				.build());
	}
}
