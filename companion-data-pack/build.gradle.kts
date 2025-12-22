import de.siphalor.mousewheelie.gradle.FormatReadmeForModSites
import org.gradle.kotlin.dsl.register

plugins {
	alias(libs.plugins.modrinth)
}

project.version = property("version") as String
val displayName = "Mouse Wheelie Companion Data Pack"

val buildGroup = "build"

val zip = tasks.register<Zip>("zip") {
	group = buildGroup
	description = "Zip the data pack"

	inputs.property("version", project.version)

	from(files("src"))

	filesMatching(listOf("pack.mcmeta", "**/version_info.mcfunction")) {
		expand("version" to project.version) {
			escapeBackslash = true
		}
	}

	destinationDirectory = layout.buildDirectory.dir("dist")
	archiveFileName = "$displayName v${project.version}.zip"
}

tasks.register("build") {
	group = buildGroup
	description = "Build the data pack"

	dependsOn(zip)
}

val formatReadmeForModSites = tasks.register<FormatReadmeForModSites>("formatReadmeForModSites") {
	input = layout.projectDirectory.file("README.md")
	output = layout.buildDirectory.dir("readme")
}

modrinth {
	project.findProperty("modrinth.token")?.let { token = it as String }
	projectId = "E9oUbZ2Q"

	syncBodyFrom = providers.fileContents(layout.buildDirectory.file("readme/modrinth.md")).asText
}

tasks.modrinth {
	enabled = false
}

tasks.modrinthSyncBody {
	dependsOn(formatReadmeForModSites)
}
