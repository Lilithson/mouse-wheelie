package de.siphalor.mousewheelie.config;

import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.data.extension.api.TweedEntryReader;
import de.siphalor.tweed5.data.extension.api.TweedReadContext;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteExtensionSetupContext;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteRelatedExtension;
import de.siphalor.tweed5.dataapi.api.TweedDataReadException;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataToken;
import de.siphalor.tweed5.defaultextensions.pather.api.PatherExtension;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import java.util.Set;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MWConfigMigrationExtension implements TweedExtension, ReadWriteRelatedExtension {
	private static final String REFILL_RESTORE_SELECTED_SLOT_PATH = ".refill.restore-selected-slot";

	private final ConfigContainer<?> configContainer;

	@Override
	public String getId() {
		return MouseWheelie.MOD_ID + ":config_migration";
	}

	@Override
	public void setupReadWriteExtension(ReadWriteExtensionSetupContext context) {
		PatchworkPartAccess<MigrationData> migrationDataAccess =
				context.registerReadWriteContextExtensionData(MigrationData.class);

		PatherExtension patherExtension = configContainer.extension(PatherExtension.class)
				.orElseThrow(() -> new IllegalStateException("PatherExtension not found"));

		context.registerReaderMiddleware(new Middleware<>() {
			@Override
			public String id() {
				return getId();
			}

			@Override
			public Set<String> mustComeAfter() {
				return Set.of(PatherExtension.EXTENSION_ID);
			}

			@Override
			public TweedEntryReader<?, ?> process(TweedEntryReader<?, ?> inner) {
				TweedEntryReader<Object, ConfigEntry<Object>> castedInner =
						(TweedEntryReader<Object, ConfigEntry<Object>>) inner;
				return (TweedEntryReader<Object, ConfigEntry<Object>>) (reader, entry, readContext) -> {
					MigrationData migrationData = readContext.extensionsData().get(migrationDataAccess);

					Object value;
					if (migrationData == null) {
						migrationData = new MigrationData();
						readContext.extensionsData().set(migrationDataAccess, migrationData);

						TweedDataReader customReader = new DataReader(
								reader,
								patherExtension,
								readContext,
								migrationData
						);

						value = castedInner.read(customReader, entry, readContext);
					} else {
						value = castedInner.read(reader, entry, readContext);
					}

					if (entry.valueClass() == (Class) MWConfig.class) {
						applyMigrations((MWConfig) value, migrationData);
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
		private final PatherExtension patherExtension;
		private final TweedReadContext readContext;
		private final MigrationData migrationData;

		@Override
		public TweedDataToken peekToken() throws TweedDataReadException {
			return delegate.peekToken();
		}

		@Override
		public TweedDataToken readToken() throws TweedDataReadException {
			TweedDataToken token = delegate.readToken();

			if (token.isMapEntryKey()) {
				String path = patherExtension.getPath(readContext);
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
