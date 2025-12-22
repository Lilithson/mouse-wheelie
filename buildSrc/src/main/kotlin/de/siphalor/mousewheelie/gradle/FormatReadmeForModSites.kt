package de.siphalor.mousewheelie.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction


abstract class FormatReadmeForModSites : DefaultTask() {
	companion object {
		val KBD_REGEX = Regex("<kbd>(.*?)</kbd>")

		val MOD_SITE_EXCLUDE_REGEX = getExcludeRegex("mod_site")
		val MODRINTH_EXCLUDE_REGEX = getExcludeRegex("modrinth")
		val CURSEFORGE_EXCLUDE_REGEX = getExcludeRegex("curseforge")

		val MODRINTH_INCLUDE_REGEX = getIncludeRegex("modrinth")
		val CURSEFORGE_INCLUDE_REGEX = getIncludeRegex("curseforge")

		private fun getExcludeRegex(type: String): Regex {
			return Regex(
				"<!-- ${type}\\.exclude\\.start -->(.*?)<!-- ${type}\\.exclude\\.end -->\n*",
				setOf(RegexOption.DOT_MATCHES_ALL)
			)
		}

		private fun getIncludeRegex(type: String): Regex {
			return Regex(
				"<!-- ${type}\\.include:\\s*(.*?)\\s*-->",
				setOf(RegexOption.DOT_MATCHES_ALL)
			)
		}
	}

	@get:InputFile
	abstract val input: RegularFileProperty

	@get:OutputDirectory
	abstract val output: DirectoryProperty

	@TaskAction
	fun run() {
		val readmeContent = input.get().asFile.readText()

		val outputFile = output.get().asFile
		outputFile.mkdirs()

		outputFile.resolve("modrinth.md").writeText(formatModrinth(readmeContent))
		outputFile.resolve("curseforge.md").writeText(formatCurseforge(readmeContent))
	}

	private fun formatModrinth(content: String): String {
		return formatBase(content)
			.replace(CURSEFORGE_EXCLUDE_REGEX, "$1")
			.replace(MODRINTH_EXCLUDE_REGEX, "")
			.replace(CURSEFORGE_INCLUDE_REGEX, "")
			.replace(MODRINTH_INCLUDE_REGEX, "$1")
	}

	private fun formatCurseforge(content: String): String {
		return formatBase(content)
			.replace(CURSEFORGE_EXCLUDE_REGEX, "")
			.replace(MODRINTH_EXCLUDE_REGEX, "$1")
			.replace(MODRINTH_INCLUDE_REGEX, "")
			.replace(CURSEFORGE_INCLUDE_REGEX, "$1")
			.replace(KBD_REGEX) { result -> "`${result.groupValues[1]}`" }
	}

	private fun formatBase(content: String): String {
		return content.replace(getExcludeRegex("mod_site")) { "" }
	}
}
