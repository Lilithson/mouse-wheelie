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

package de.siphalor.mousewheelie.config;

import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.defaultextensions.pather.api.PathTracking;
import de.siphalor.tweed5.defaultextensions.pather.api.PathTrackingDataReader;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import de.siphalor.tweed5.serde.extension.api.TweedEntryReader;
import de.siphalor.tweed5.serde.extension.api.extension.ReadWriteExtensionSetupContext;
import de.siphalor.tweed5.serde.extension.api.extension.ReadWriteRelatedExtension;
import de.siphalor.tweed5.serde.extension.api.extension.ReaderMiddlewareContext;
import de.siphalor.tweed5.serde.extension.api.read.result.TweedReadResult;
import de.siphalor.tweed5.serde_api.api.TweedDataReadException;
import de.siphalor.tweed5.serde_api.api.TweedDataReader;
import de.siphalor.tweed5.serde_api.api.TweedDataToken;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MWConfigMigrationExtension implements TweedExtension, ReadWriteRelatedExtension {
	private static final String REFILL_RESTORE_SELECTED_SLOT_PATH = ".refill.restore-selected-slot";

	@Override
	public String getId() {
		return MouseWheelie.MOD_ID + ":config_migration";
	}

	@Override
	public void setupReadWriteExtension(ReadWriteExtensionSetupContext context) {
		PatchworkPartAccess<MigrationData> migrationDataAccess =
				context.registerReadWriteContextExtensionData(MigrationData.class);

		context.registerReaderMiddleware(new Middleware<>() {
			@Override
			public String id() {
				return getId();
			}

			@Override
			public TweedEntryReader<?, ?> process(TweedEntryReader<?, ?> inner, ReaderMiddlewareContext context) {
				TweedEntryReader<Object, ConfigEntry<Object>> castedInner =
						(TweedEntryReader<Object, ConfigEntry<Object>>) inner;
				return (TweedEntryReader<Object, ConfigEntry<Object>>) (reader, entry, readContext) -> {
					MigrationData migrationData = readContext.extensionsData().get(migrationDataAccess);

					TweedReadResult<Object> value;
					if (migrationData == null) {
						migrationData = new MigrationData();
						readContext.extensionsData().set(migrationDataAccess, migrationData);

						PathTracking pathTracking = PathTracking.create();
						TweedDataReader customReader = new PathTrackingDataReader(
								new DataReader(reader, pathTracking, migrationData),
								pathTracking
						);

						value = castedInner.read(customReader, entry, readContext);
					} else {
						value = castedInner.read(reader, entry, readContext);
					}

					if (value.hasValue() && value.value().getClass() == MWConfig.class) {
						applyMigrations((MWConfig) value.value(), migrationData);
					}
					return value;
				};
			}
		});
	}

	private void applyMigrations(MWConfig config, MigrationData migrationData) {
		if (migrationData.wasRefillRestoreSelectedSlot) {
			config.refill.alwaysKeepSelectedSlot = true;
		}
	}

	@RequiredArgsConstructor
	private static class DataReader implements TweedDataReader {
		private final TweedDataReader delegate;
		private final PathTracking pathTracking;
		private final MigrationData migrationData;

		@Override
		public TweedDataToken peekToken() throws TweedDataReadException {
			return delegate.peekToken();
		}

		@Override
		public TweedDataToken readToken() throws TweedDataReadException {
			TweedDataToken token = delegate.readToken();

			if (token.isMapEntryKey()) {
				String path = pathTracking.currentPath();
				if (path.equals(REFILL_RESTORE_SELECTED_SLOT_PATH)) {
					TweedDataToken value = delegate.peekToken();
					migrationData.setWasRefillRestoreSelectedSlot(
							value.canReadAsBoolean() && value.readAsBoolean());
				}
			}

			return token;
		}

		@Override
		public void close() throws Exception {
			delegate.close();
		}
	}

	@Data
	static class MigrationData {
		public boolean wasRefillRestoreSelectedSlot;
	}
}
