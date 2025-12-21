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

package de.siphalor.mousewheelie;

import de.siphalor.mousewheelie.client.MWClient;
import de.siphalor.mousewheelie.client.inventory.sort.SortMode;
import de.siphalor.mousewheelie.common.network.MWLogicalServerNetworking;
import de.siphalor.mousewheelie.common.network.MWNetworking;
import de.siphalor.mousewheelie.client.config.MWServerRequiredTweedExtension;
import de.siphalor.tweed5.coat.bridge.api.TweedCoatBridgeExtension;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.data.hjson.HjsonCommentType;
import de.siphalor.tweed5.data.hjson.HjsonSerde;
import de.siphalor.tweed5.data.hjson.HjsonWriter;
import de.siphalor.tweed5.fabric.helper.api.FabricConfigCommentLoader;
import de.siphalor.tweed5.fabric.helper.api.FabricConfigContainerHelper;
import de.siphalor.tweed5.weaver.pojo.api.TweedPojoWeaver;
import lombok.CustomLog;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
//- import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
//- import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Set;
//- import net.minecraft.world.InteractionHand;
//- import net.minecraft.world.InteractionResultHolder;
//- import net.minecraft.world.entity.player.Player;
//- import net.minecraft.world.item.ItemStack;
//- import net.minecraft.world.level.Level;

@CustomLog
public class MouseWheelie implements ModInitializer {
	public static final String MOD_ID = "mousewheelie";
	public static final String MOD_NAME = "Mouse Wheelie";

	public static MWConfig globalConfig;
	public static MWConfig config;
	public static EnumSet<MWFeature> enabledFeatures = EnumSet.allOf(MWFeature.class);

	public static FabricConfigContainerHelper<MWConfig> configContainerHelper;

	public static Logger createLogger(Class<?> clazz) {
		return LoggerFactory.getLogger(MOD_NAME + "/" + clazz.getSimpleName());
	}

	//# if MC_VERSION_NUMBER >= 12111
	public static Identifier createId(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
	//# else
	//- public static ResourceLocation createId(String path) {
	//- 	//# if MC_VERSION_NUMBER >= 12100
	//- 	return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	//- 	//# else
	//- 	return new ResourceLocation(MOD_ID, path);
	//- 	//# end
	//- }
	//# end

	@Override
	public void onInitialize() {
		initializeConfig();
		loadConfig();

		//# if MC_VERSION_NUMBER < 11904
		//- UseItemCallback.EVENT.register(this::onPlayerUseItem);
		//# end

		MWNetworking.setup();
		MWLogicalServerNetworking.setup();
	}

	private void initializeConfig() {
		TweedPojoWeaver<MWConfig> weaver = TweedPojoWeaver.forClass(MWConfig.class);
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			weaver.withExtension(TweedCoatBridgeExtension.class).withExtension(MWServerRequiredTweedExtension.class);
		}

		ConfigContainer<MWConfig> configContainer = weaver.weave();
		FabricConfigCommentLoader.builder()
				.configContainer(configContainer)
				.modId(MOD_ID)
				.prefix("mousewheelie.config")
				.suffix(".description")
				.build()
				.loadCommentsFromLanguageFile("en_us");
		configContainer.initialize();
		configContainerHelper = FabricConfigContainerHelper.create(
				configContainer,
				new HjsonSerde(new HjsonWriter.Options().multilineCommentType(HjsonCommentType.SLASHES)),
				MOD_ID
		);
	}

	private void loadConfig() {
		globalConfig = configContainerHelper.loadAndUpdateInConfigDirectory();
		config = globalConfig;
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			MWClient.onConfigChanged();
		}
		log.info("Loaded {} config", MOD_NAME);
	}

	public static void updateConfig(MWConfig config) {
		globalConfig = config;
		setFeaturesForSession(enabledFeatures);
	}

	public static void setFeaturesForSession(Set<MWFeature> features) {
		log.info("Reducing enabled {} features to {}", MOD_NAME, features);

		config = configContainerHelper.configContainer().rootEntry().deepCopy(globalConfig);

		if (!features.contains(MWFeature.SORT)) {
			config.sort.primarySort = SortMode.NONE;
			config.sort.shiftSort = SortMode.NONE;
			config.sort.controlSort = SortMode.NONE;
		}
		if (config.general.enableQuickCraft) {
			config.general.enableQuickCraft = features.contains(MWFeature.QUICK_CRAFT);
		}
		if (config.refill.enable) {
			config.refill.enable = features.contains(MWFeature.REFILL);
		}
		if (config.toolPicking.pickFromInventory) {
			config.toolPicking.pickFromInventory = features.contains(MWFeature.TOOL_PICK_INVENTORY);
		}
	}

	public static void endFeatureSession() {
		log.info("{} feature session ended", MOD_NAME);

		enabledFeatures = EnumSet.allOf(MWFeature.class);
		config = globalConfig;
	}

	//# if MC_VERSION_NUMBER < 11904
	//- private InteractionResultHolder<ItemStack> onPlayerUseItem(Player player, Level world, InteractionHand hand) {
	//- 	ItemStack stack = player.getItemInHand(hand);
	//- 	if (MWConfig.general.enableQuickArmorSwapping && !world.isClient()) {
	//- 		EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(stack);
	//- 		if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
	//- 			ItemStack equipmentStack = player.getEquippedStack(equipmentSlot);
	//- 			int index = 5 + (3 - equipmentSlot.getEntitySlotId());
	//- 			if (!equipmentStack.isEmpty() && player.playerScreenHandler.getSlot(index).canTakeItems(player)) {
	//- 				player.setStackInHand(hand, equipmentStack);
	//- 				player.equipStack(equipmentSlot, stack);
	//- 				return TypedActionResult.consume(equipmentStack);
	//- 			}
	//- 		}
	//- 	}
	//- 	return InteractionResultHolder.pass(stack);
	//- }
	//# end
}
