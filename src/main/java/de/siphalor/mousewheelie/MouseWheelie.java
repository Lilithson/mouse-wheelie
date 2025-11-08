/*
 * Copyright 2020-2022 Siphalor
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
import de.siphalor.mousewheelie.common.network.MWLogicalServerNetworking;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.data.hjson.HjsonCommentType;
import de.siphalor.tweed5.data.hjson.HjsonSerde;
import de.siphalor.tweed5.data.hjson.HjsonWriter;
import de.siphalor.tweed5.fabric.helper.api.FabricConfigCommentLoader;
import de.siphalor.tweed5.fabric.helper.api.FabricConfigContainerHelper;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.TweedPojoWeaverBootstrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class MouseWheelie implements ModInitializer {
	public static final String MOD_ID = "mousewheelie";
	public static final String MOD_NAME = "Mouse Wheelie";

	//# if CONFIG == "TWEED_5"
	public static MWConfig config;
	public static FabricConfigContainerHelper<MWConfig> configContainerHelper;
	//# end

	@Override
	public void onInitialize() {
		//# if CONFIG == "TWEED_5"
		initializeConfig();
		loadConfig();
		//# end

		UseItemCallback.EVENT.register(this::onPlayerUseItem);

		MWLogicalServerNetworking.setup();
	}

	//# if CONFIG == "TWEED_5"
	private void loadConfig() {
		config = configContainerHelper.loadAndUpdateInConfigDirectory();
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			MWClient.onConfigChanged();
		}
	}

	private void initializeConfig() {
		ConfigContainer<MWConfig> configContainer = TweedPojoWeaverBootstrapper.create(MWConfig.class).weave();
		FabricConfigCommentLoader.builder()
				.configContainer(configContainer)
				.modId(MOD_ID)
				.prefix("tweed4_tailor_screen.screen.mousewheelie")
				.suffix(".description")
				.build()
				.loadCommentsFromLanguageFile("en.us");
		configContainer.initialize();
		configContainerHelper = FabricConfigContainerHelper.create(
				configContainer,
				new HjsonSerde(new HjsonWriter.Options().multilineCommentType(HjsonCommentType.SLASHES)),
				MOD_ID
		);
	}
	//# end

	private TypedActionResult<ItemStack> onPlayerUseItem(PlayerEntity player, World world, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		//# if MC_VERSION_NUMBER < 11904
		//- if (MWConfig.general.enableQuickArmorSwapping && !world.isClient()) {
		//- 	EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(stack);
		//- 	if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
		//- 		ItemStack equipmentStack = player.getEquippedStack(equipmentSlot);
		//- 		int index = 5 + (3 - equipmentSlot.getEntitySlotId());
		//- 		if (!equipmentStack.isEmpty() && player.playerScreenHandler.getSlot(index).canTakeItems(player)) {
		//- 			player.setStackInHand(hand, equipmentStack);
		//- 			player.equipStack(equipmentSlot, stack);
		//- 			return TypedActionResult.consume(equipmentStack);
		//- 		}
		//- 	}
		//- }
		//# end
		return TypedActionResult.pass(stack);
	}
}
