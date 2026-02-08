package de.siphalor.mousewheelie.client.keybinding;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.mousewheelie.MouseWheelie;

import net.minecraft.client.KeyMapping;

public class MWBaseKeyMapping extends KeyMapping {
	//# if MC_VERSION_NUMBER >= 12109
	public MWBaseKeyMapping(String name, InputConstants.Key key, Category category) {
	//# else
	//- public MWBaseKeyMapping(String name, InputConstants.Key key, String category) {
	//# end
		this(name, key.getType(), key.getValue(), category);
	}

	//# if MC_VERSION_NUMBER >= 12109
	public MWBaseKeyMapping(String name, InputConstants.Type inputType, int inputCode, Category category) {
	//# else
	//- public MWBaseKeyMapping(String name, InputConstants.Type inputType, int inputCode, String category) {
	//# end
		super("key." + MouseWheelie.MOD_ID + "." + name, inputType, inputCode, category);
	}
}
