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
