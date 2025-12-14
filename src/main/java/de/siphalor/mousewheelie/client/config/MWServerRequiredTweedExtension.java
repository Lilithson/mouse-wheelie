package de.siphalor.mousewheelie.client.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.tweed5.attributesextension.api.AttributesExtension;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.defaultextensions.validation.api.ConfigEntryValidator;
import de.siphalor.tweed5.defaultextensions.validation.api.ValidationProvidingExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssue;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssueLevel;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationResult;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.resources.language.I18n;
import org.jspecify.annotations.NullMarked;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Custom Tweed extension that adds an informational message
 * to config entries for features that require a server-side installation.
 */
@RequiredArgsConstructor
@CustomLog
public class MWServerRequiredTweedExtension implements TweedExtension, ValidationProvidingExtension {
	public static final String ID = MouseWheelie.MOD_ID + ":server_required";
	public static final String ATTRIBUTE = ID;

	private static final String MESSAGE_KEY = MouseWheelie.MOD_ID + ".config.server-required";

	private final ConfigContainer<?> container;
	private AttributesExtension attributesExtension;

	private String englishMessage;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void extensionsFinalized() {
		attributesExtension = container.extension(AttributesExtension.class)
				.orElseThrow(() -> new IllegalStateException("AttributesExtension not found"));

		try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader()
				.getResourceAsStream("assets/" + MouseWheelie.MOD_ID + "/lang/en_us.json")))) {
			Gson gson = new Gson();
			JsonObject object = gson.fromJson(reader, JsonObject.class);
			englishMessage = object.get(MESSAGE_KEY).getAsString();
		} catch (Exception e) {
			log.warn("Failed loading translation for server-required tweed extension", e);
			englishMessage = "Server-side installation required [Failed loading translation]";
		}
	}

	@NullMarked
	@Override
	public Middleware<ConfigEntryValidator> validationMiddleware() {
		return new Middleware<>() {
			@Override
			public String id() {
				return ID;
			}

			@Override
			public ConfigEntryValidator process(ConfigEntryValidator inner) {
				return new ConfigEntryValidator() {
					@Override
					public <T> ValidationResult<T> validate(ConfigEntry<T> configEntry, T value) {
						ValidationResult<T> innerResult = inner.validate(configEntry, value);
						if (attributesExtension.getAttributeValue(configEntry, ATTRIBUTE) == null) {
							return innerResult;
						} else if (value != Boolean.TRUE) {
							return innerResult;
						}

						List<ValidationIssue> issues = new ArrayList<>(innerResult.issues());
						issues.add(new ValidationIssue(englishMessage, ValidationIssueLevel.INFO));

						return ValidationResult.withIssues(innerResult.value(), issues);
					}

					@Override
					public <T> String description(ConfigEntry<T> configEntry) {
						String innerDescription = inner.description(configEntry);
						if (attributesExtension.getAttributeValue(configEntry, ATTRIBUTE) == null) {
							return innerDescription;
						}
						if (!innerDescription.isBlank()) {
							innerDescription += "\n\n";
						}
						if (I18n.exists(MESSAGE_KEY)) {
							return innerDescription + (I18n.get(MESSAGE_KEY));
						}
						return innerDescription + englishMessage;
					}
				};
			}
		};
	}
}
