package de.siphalor.mousewheelie.client.inventory.sort;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.data.extension.api.TweedEntryReadException;
import de.siphalor.tweed5.data.extension.api.TweedReadContext;
import de.siphalor.tweed5.data.extension.api.TweedWriteContext;
import de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriter;
import de.siphalor.tweed5.dataapi.api.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SortModeReaderWriter implements TweedEntryReaderWriter<SortMode, @NotNull ConfigEntry<SortMode>> {
	@Override
	public SortMode read(
			@NotNull TweedDataReader tweedDataReader,
			ConfigEntry<SortMode> sortModeConfigEntry,
			@NotNull TweedReadContext context
	) throws TweedEntryReadException {
		try {
			TweedDataToken token = tweedDataReader.readToken();
			if (!token.canReadAsString()) {
				throw new TweedEntryReadException("Failed to understand token " + token + " as sort mode, expected string", context);
			}
			SortMode sortMode = SortMode.getByName(token.readAsString());
			if (sortMode == null) {
				throw new TweedEntryReadException("Unknown sort mode " + token.readAsString(), context);
			}
			return sortMode;
		} catch (TweedDataReadException e) {
			throw new TweedEntryReadException("Failed to read sort mode", e, context);
		}
	}

	@Override
	public void write(
			@NotNull TweedDataVisitor tweedDataVisitor,
			@Nullable SortMode sortMode,
			@NotNull ConfigEntry<SortMode> sortModeConfigEntry,
			@NotNull TweedWriteContext context
	) throws TweedDataWriteException {
		if (sortMode == null) {
			tweedDataVisitor.visitNull();
		} else {
			tweedDataVisitor.visitString(sortMode.name());
		}
	}
}
