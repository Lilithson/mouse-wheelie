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

//- import com.google.common.base.CaseFormat;
//- import de.siphalor.mousewheelie.client.MWClient;

import de.siphalor.mousewheelie.client.inventory.sort.SortMode;
import de.siphalor.mousewheelie.client.util.ItemStackUtils;
import de.siphalor.tweed5.coat.bridge.api.TweedCoatAttributes;
import de.siphalor.tweed5.coat.bridge.api.TweedCoatBridgeExtension;
import de.siphalor.tweed5.commentloaderextension.api.CommentLoaderExtension;
import de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension;
import de.siphalor.tweed5.fabric.helper.api.DefaultTweedMinecraftWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.CompoundWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.TweedExtension;
import de.siphalor.tweed5.weaver.pojoext.attributes.api.Attribute;
import de.siphalor.tweed5.weaver.pojoext.presets.api.Preset;
import de.siphalor.tweed5.weaver.pojoext.validation.api.Validator;
import de.siphalor.tweed5.weaver.pojoext.validation.api.validators.WeavableNumberRangeValidator;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@SuppressWarnings({"WeakerAccess", "unused"})
//# if CONFIG == "TWEED_5"
@DefaultTweedMinecraftWeaving
@TweedExtension(CommentLoaderExtension.class)
@TweedExtension(TweedCoatBridgeExtension.class)
@CompoundWeaving(namingFormat = "kebab_case")
@Attribute(key = TweedCoatAttributes.BACKGROUND_TEXTURE, values = "textures/block/green_concrete_powder.png")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
//# else
//- @ATweedConfig(environment = ConfigEnvironment.CLIENT, scope = ConfigScope.SMALLEST, tailors = {"tweed4:lang_json_descriptions", "tweed4:coat"}, casing = CaseFormat.LOWER_HYPHEN)
//- @AConfigBackground("textures/block/green_concrete_powder.png")
//# end
public class MWConfig {
	@Preset(PresetsExtension.DEFAULT_PRESET_NAME)
	public static final MWConfig DEFAULT = new MWConfig(new General(), new Scrolling(), new Sort(), new Refill(), new ToolPicking());

	//# if CONFIG == "TWEED_5"
	public General general;
	public Scrolling scrolling;
	public Sort sort;
	public Refill refill;
	public ToolPicking toolPicking;
	//# elif CONFIG == "TWEED_4"
	//- public static General general = new General();
	//- public static Scrolling scrolling = new Scrolling();
	//- public static Sort sort = new Sort();
	//- public static Refill refill = new Refill();
	//- public static ToolPicking toolPicking = new ToolPicking();
	//# end

	//# if CONFIG == "TWEED_5"
	@CompoundWeaving
	@Attribute(key = TweedCoatAttributes.BACKGROUND_TEXTURE, values = "textures/block/acacia_log.png")
	//# elif CONFIG == "TWEED_4"
	//- @AConfigBackground("textures/block/acacia_log.png")
	//# end
	public static class General {
		//# if CONFIG == "TWEED_5"
		@Validator(value = WeavableNumberRangeValidator.class, config = "1=..")
		//# elif CONFIG == "TWEED_4"
		//- @AConfigEntry(
		//- 		constraints = @AConfigConstraint(value = RangeConstraint.class, param = "1..")
		//- )
		//# end
		public int interactionRate = 10;
		//# if CONFIG == "TWEED_5"
		@Validator(value = WeavableNumberRangeValidator.class, config = "1=..")
		//# elif CONFIG == "TWEED_4"
		//- @AConfigEntry(
		//- 		constraints = @AConfigConstraint(value = RangeConstraint.class, param = "1..")
		//- )
		//# end
		public int integratedInteractionRate = 1;

		//# if MC_VERSION_NUMBER < 11904
		//- @AConfigEntry(environment = ConfigEnvironment.UNIVERSAL)
		//- public boolean enableQuickArmorSwapping = true;
		//# end

		public boolean enableDropModifier = true;

		public boolean enableQuickCraft = true;

		public ItemStackUtils.NbtMatchMode itemKindsNbtMatchMode = ItemStackUtils.NbtMatchMode.SOME;

		public enum HotbarScoping {HARD, SOFT, NONE}

		public HotbarScoping hotbarScoping = HotbarScoping.SOFT;

		public boolean betterFastDragging = false;

		public boolean enableBundleDragging = true;

		//# if CONFIG == "TWEED_4"
		//- @AConfigListener("interaction-rate")
		//- public void onReloadInteractionRate() {
		//- 	if (!MWClient.isOnLocalServer()) {
		//- 		InteractionManager.setTickRate(interactionRate);
		//- 	}
		//- }

		//- @AConfigListener("integrated-interaction-rate")
		//- public void onReloadIntegratedInteractionRate() {
		//- 	if (MWClient.isOnLocalServer()) {
		//- 		InteractionManager.setTickRate(integratedInteractionRate);
		//- 	}
		//- }
		//# end
	}

	//# if CONFIG == "TWEED_5"
	@CompoundWeaving
	@Attribute(key = TweedCoatAttributes.BACKGROUND_TEXTURE, values = "textures/block/dark_prismarine.png")
	//# elif CONFIG == "TWEED_4"
	//- @AConfigBackground("textures/block/dark_prismarine.png")
	//# end
	public static class Scrolling {
		public boolean enable = true;
		public boolean invert = false;
		public boolean directionalScrolling = true;
		public boolean scrollCreativeMenuItems = true;
		public boolean scrollCreativeMenuTabs = true;
	}

	//# if CONFIG == "TWEED_5"
	@CompoundWeaving
	@Attribute(key = TweedCoatAttributes.BACKGROUND_TEXTURE, values = "textures/block/barrel_top.png")
	//# elif CONFIG == "TWEED_4"
	//- @AConfigBackground("textures/block/barrel_top.png")
	//# end
	public static class Sort {
		public SortMode primarySort = SortMode.CREATIVE;
		public SortMode shiftSort = SortMode.QUANTITY;
		public SortMode controlSort = SortMode.ALPHABET;
		public boolean serverAcceleratedSorting = true;

		//# if CONFIG == "TWEED_4"
		//- @AConfigEntry(scope = ConfigScope.SMALLEST)
		//# end
		public boolean optimizeCreativeSearchSort = true;

		//# if CONFIG == "TWEED_4"
		//- @AConfigListener("optimize-creative-search-sort")
		//- public void onReloadOptimizeCreativeSearchSort() {
		//- 	CreativeSearchOrder.refreshItemSearchPositionLookup();
		//- }
		//# end
	}

	//# if CONFIG == "TWEED_5"
	@CompoundWeaving
	@Attribute(key = TweedCoatAttributes.BACKGROUND_TEXTURE, values = "textures/block/horn_coral_block.png")
	//# elif CONFIG == "TWEED_4"
	//- @AConfigBackground("textures/block/horn_coral_block.png")
	//# end
	public static class Refill {
		public boolean enable = true;

		public boolean playSound = true;

		public boolean offHand = true;
		public boolean restoreSelectedSlot = false;

		public boolean itemChanges = true;

		public boolean eat = true;
		public boolean drop = true;
		public boolean use = true;
		public boolean other = true;

		public Rules rules = new Rules();

		//# if CONFIG == "TWEED_5"
		@CompoundWeaving
		@Attribute(key = TweedCoatAttributes.BACKGROUND_TEXTURE, values = "textures/block/yellow_terracotta.png")
		//# elif CONFIG == "TWEED_4"
		//- @AConfigBackground("textures/block/yellow_terracotta.png")
		//# end
		public static class Rules {
			public boolean anyBlock = false;
			public boolean itemgroup = false;
			public boolean itemHierarchy = false;
			public boolean blockHierarchy = false;
			public boolean food = false;
			public boolean equalItems = true;
			public boolean equalStacks = true;
		}
	}

	//# if CONFIG == "TWEED_5"
	@CompoundWeaving
	@Attribute(key = TweedCoatAttributes.BACKGROUND_TEXTURE, values = "textures/block/coarse_dirt.png")
	//# elif CONFIG == "TWEED_4"
	//- @AConfigBackground("textures/block/coarse_dirt.png")
	//# end
	public static class ToolPicking {
		public boolean holdTool = true;
		public boolean holdBlock = false;
		public boolean pickFromInventory = true;
	}

	//# if CONFIG == "TWEED_4"
	//- @AConfigFixer
	//- public <V extends DataValue<V, L, O>, L extends DataList<V, L, O>, O extends DataObject<V, L, O>>
	//- void fixConfig(O dataObject, O rootObject) {
	//- 	if (dataObject.has("general") && dataObject.get("general").isObject()) {
	//- 		O general = dataObject.get("general").asObject();

	//- 		moveConfigEntry(dataObject, general, "enable-item-scrolling", "scrolling");
	//- 		moveConfigEntry(dataObject, general, "scroll-factor", "scrolling");
	//- 		moveConfigEntry(dataObject, general, "directional-scrolling", "scrolling");

	//- 		if (dataObject.has("scrolling") && dataObject.get("scrolling").isObject()) {
	//- 			O scrolling = dataObject.get("scrolling").asObject();

	//- 			if (scrolling.has("scroll-creative-menu") && scrolling.get("scroll-creative-menu").isBoolean()) {
	//- 				scrolling.set("scroll-creative-menu-items", !scrolling.get("scroll-creative-menu").asBoolean());
	//- 				scrolling.remove("scroll-creative-menu");
	//- 			}
	//- 			if (scrolling.has("scroll-factor") && scrolling.get("scroll-factor").isNumber()) {
	//- 				scrolling.set("invert", scrolling.get("scroll-factor").asFloat() < 0);
	//- 				scrolling.remove("scroll-factor");
	//- 			}
	//- 		}

	//- 		moveConfigEntry(dataObject, general, "hold-tool-pick", "tool-picking", "hold-tool");
	//- 		moveConfigEntry(dataObject, general, "hold-block-tool-pick", "tool-picking", "hold-block");

	//- 		moveConfigEntry(dataObject, general, "enable-alt-dropping", "general", "enable-drop-modifier");

	//- 		general.remove("hotbar-scope");
	//- 	}
	//- }

	//- @AConfigFixer("sort")
	//- public <V extends DataValue<V, L, O>, L extends DataList<V, L, O>, O extends DataObject<V, L, O>>
	//- void fixSortModes(O sort, O mainConfig) {
	//- 	if (!sort.has("optimize-creative-search-sort")) {
	//- 		if (sort.getString("primary-sort", "").equalsIgnoreCase("raw_id")) {
	//- 			sort.set("primary-sort", "creative");
	//- 		}
	//- 		if (sort.getString("shift-sort", "").equalsIgnoreCase("raw_id")) {
	//- 			sort.set("shift-sort", "creative");
	//- 		}
	//- 		if (sort.getString("control-sort", "").equalsIgnoreCase("raw_id")) {
	//- 			sort.set("control-sort", "creative");
	//- 		}
	//- 	}
	//- }

	//- @SuppressWarnings("SameParameterValue")
	//- private <V extends DataValue<V, L, O>, L extends DataList<V, L, O>, O extends DataObject<V, L, O>>
	//- void moveConfigEntry(O root, O origin, String name, String destCat) {
	//- 	moveConfigEntry(root, origin, name, destCat, name);
	//- }

	//- private <V extends DataValue<V, L, O>, L extends DataList<V, L, O>, O extends DataObject<V, L, O>>
	//- void moveConfigEntry(O root, O origin, String name, String destCat, String newName) {
	//- 	if (origin.has(name)) {
	//- 		O dest;
	//- 		if (root.has(destCat) && root.get(destCat).isObject()) {
	//- 			dest = root.get(destCat).asObject();
	//- 		} else {
	//- 			dest = root.addObject(destCat);
	//- 		}
	//- 		dest.set(newName, origin.get(name));
	//- 		origin.remove(name);
	//- 	}
	//- }
	//# end
}
