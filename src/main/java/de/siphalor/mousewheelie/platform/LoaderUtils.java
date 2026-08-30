/*
 * Copyright 2026 Siphalor and contributors
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

package de.siphalor.mousewheelie.platform;

public final class LoaderUtils {
	private LoaderUtils() {}

	public static boolean isClientEnvironment() {
		Boolean fabricResult = detectFabricClientEnvironment();
		if (fabricResult != null) {
			return fabricResult;
		}

		Boolean neoForgeResult = detectNeoForgeClientEnvironment();
		if (neoForgeResult != null) {
			return neoForgeResult;
		}

		return false;
	}

	private static Boolean detectFabricClientEnvironment() {
		try {
			Class<?> fabricLoaderClass = Class.forName("net.fabricmc.loader.api.FabricLoader");
			Class<?> envTypeClass = Class.forName("net.fabricmc.api.EnvType");
			Object fabricLoader = fabricLoaderClass.getMethod("getInstance").invoke(null);
			Object environmentType = fabricLoaderClass.getMethod("getEnvironmentType").invoke(fabricLoader);
			Object clientEnv = Enum.valueOf((Class<Enum>) envTypeClass.asSubclass(Enum.class), "CLIENT");
			return environmentType == clientEnv;
		} catch (ReflectiveOperationException ignored) {
			return null;
		}
	}

	private static Boolean detectNeoForgeClientEnvironment() {
		try {
			Class<?> fmLEnvironmentClass = Class.forName("net.neoforged.fml.loading.FMLEnvironment");
			Object dist = fmLEnvironmentClass.getField("dist").get(null);
			return "CLIENT".equals(String.valueOf(dist));
		} catch (ReflectiveOperationException ignored) {
			return null;
		}
	}
}
