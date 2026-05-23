/*
 * Copyright 2025 Siphalor and contributors
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

package de.siphalor.mousewheelie.client.inventory.sort;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.serde.extension.api.TweedReadContext;
import de.siphalor.tweed5.serde.extension.api.TweedWriteContext;
import de.siphalor.tweed5.serde.extension.api.read.result.TweedReadIssue;
import de.siphalor.tweed5.serde.extension.api.read.result.TweedReadResult;
import de.siphalor.tweed5.serde.extension.api.readwrite.TweedEntryReaderWriter;
import de.siphalor.tweed5.serde_api.api.TweedDataReadException;
import de.siphalor.tweed5.serde_api.api.TweedDataReader;
import de.siphalor.tweed5.serde_api.api.TweedDataToken;
import de.siphalor.tweed5.serde_api.api.TweedDataVisitor;
import de.siphalor.tweed5.serde_api.api.TweedDataWriteException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SortModeReaderWriter implements TweedEntryReaderWriter<SortMode, @NotNull ConfigEntry<SortMode>> {
	@Override
	public TweedReadResult<SortMode> read(
			@NotNull TweedDataReader tweedDataReader,
			ConfigEntry<SortMode> sortModeConfigEntry,
			@NotNull TweedReadContext context
	) {
		try {
			TweedDataToken token = tweedDataReader.readToken();
			if (!token.canReadAsString()) {
				return TweedReadResult.error(TweedReadIssue.error(
						"Failed to understand token " + token + " as sort mode, expected string", context
				));
			}
			SortMode sortMode = SortMode.getByName(token.readAsString());
			if (sortMode == null) {
				return TweedReadResult.error(TweedReadIssue.error(
						"Unknown sort mode " + token.readAsString(), context
				));
			}
			return TweedReadResult.ok(sortMode);
		} catch (TweedDataReadException e) {
			return TweedReadResult.failed(TweedReadIssue.error(e, context));
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
