package de.siphalor.mousewheelie.client.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.siphalor.mousewheelie.client.MWClient;

public class MWModMenuEntryPoint implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> MWClient.createConfigScreen();
	}
}
