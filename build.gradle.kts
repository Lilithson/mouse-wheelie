import de.siphalor.jcyo.gradle.JcyoTask
import java.util.*

plugins {
	alias(libs.plugins.loom)
	java
	`maven-publish`
	alias(libs.plugins.jcyo)
}

val minecraftVersionDescriptor = project.properties["minecraft.version.descriptor"] as String
val mcProps = Properties().apply {
	val propFile = project.layout.settingsDirectory.file("gradle/mc-${minecraftVersionDescriptor}/gradle.properties")
	load(propFile.asFile.inputStream())
}

group = "de.siphalor.${project.name}"
val archivesBaseName = "${project.name}-mc${minecraftVersionDescriptor}"
val shortVersion = "${properties["version"]}"
version = "${shortVersion}+mc${mcLibs.versions.minecraft.get()}"

repositories {
	maven {
		name = "shedaniel"
		url = uri("https://maven.shedaniel.me/")
	}
	maven {
		name = "TerraformersMC"
		url = uri("https://maven.terraformersmc.com/releases")
	}
	maven {
		name = "Nucleoid"
		url = uri("https://maven.nucleoid.xyz")
		mavenContent {
			includeGroupAndSubgroups("eu.pb4")
		}
	}
	maven {
		name = "ParchmentMC"
		url = uri("https://maven.parchmentmc.org")
		mavenContent {
			includeGroup("org.parchmentmc.data")
		}
	}
	maven {
		name = "Siphalor"
		url = uri("https://maven.siphalor.de/")
		mavenContent {
			includeGroupAndSubgroups("de.siphalor")
		}
	}
	maven {
		name = "Jitpack"
		url = uri("https://jitpack.io")
		mavenContent {
			includeGroupAndSubgroups("com.github")
		}
	}
	mavenLocal()
}

// Workaround for https://github.com/gradle/gradle/issues/10195
configurations {
	include {
		isTransitive = true
		dependencies.filter { !it.name.contains("bom") }.forEach {
			(it as ModuleDependency).isTransitive = false
		}
	}
}

dependencies {
	annotationProcessor(libs.lombok)
	compileOnly(libs.lombok)

	minecraft(mcLibs.minecraft)
	mappings(loom.layered {
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-${mcLibs.versions.minecraft.get()}:${mcLibs.versions.parchment.get()}@zip")
	})
	"modImplementation"(libs.fabric.loader)

	for (mod in listOf(
		"fabric-api-base",
		"fabric-events-interaction-v0",
		"fabric-item-api-v1",
		"fabric-item-group-api-v1",
		"fabric-lifecycle-events-v1",
		"fabric-key-binding-api-v1",
		"fabric-networking-api-v1",
		"fabric-resource-loader-v0",
		"fabric-registry-sync-v0",
		"fabric-screen-api-v1",
	)) {
		"modImplementation"(fabricApi.module(mod, mcLibs.versions.fabric.api.get()))
	}

	modApi(mcLibs.modmenu)

	include(mcLibs.bundles.config)
	modApi(mcLibs.bundles.config)

	include(mcLibs.amecs.api)
	modImplementation(mcLibs.amecs.api)
}

tasks.processResources {
    inputs.property("version", project.version)

	from(sourceSets.main.get().resources.srcDirs) {
		include("fabric.mod.json")
		expand("version" to project.version)
		duplicatesStrategy = DuplicatesStrategy.INCLUDE
	}
}

java {
	sourceCompatibility = JavaVersion.toVersion(mcLibs.versions.java.get())
	targetCompatibility = JavaVersion.toVersion(mcLibs.versions.java.get())
	withSourcesJar()
}

val jcyoVars = mcProps.stringPropertyNames()
	.filter { it.startsWith("preprocessor.") }
	.map { it to mcProps[it] }
	.associate { (key, value) -> key.substring("preprocessor.".length) to value.toString() }
val jcyo = tasks.register<JcyoTask>("jcyo") {
	inputDirectory = file("src/main/java")
	variables = jcyoVars
}

tasks.compileJava {
	dependsOn(jcyo)
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = archivesBaseName
			version = shortVersion

			from(components["java"])
		}
	}

	repositories {
		if (project.hasProperty("siphalor.maven.user")) {
			maven {
				name = "Siphalor"
				url = uri("https://maven.siphalor.de/upload.php")
				credentials {
					username = project.property("siphalor.maven.user") as String
					password = project.property("siphalor.maven.password") as String
				}
			}
		}
	}
}

/*
static def getChangelog() {
	return 'git log -1 --format=format:##%x20%s%n%n%b%nRelease%x20by%x20%an --grep Version'.execute().text.trim()
}

tasks.register('uploadToModSites') {
	dependsOn build
	group = "upload"
}

if (project.hasProperty("curseforgeToken")) {
	curseforge {
		apiKey project.curseforgeToken
		project {
			id = "317514"
			releaseType = project.mod_release
			changelogType = "markdown"
			changelog = project.getChangelog()
			addGameVersion("Fabric")
			String mcVersions = project.hasProperty("curseforge_mc_versions") ? project.property("curseforge_mc_versions") : project.mod_mc_versions
			for (version in (mcVersions).split(";")) {
				addGameVersion(version)
			}
			relations {
				embeddedLibrary "fabric-api"
				embeddedLibrary "amecs"
				embeddedLibrary "tweed-api"
				optionalDependency "modmenu"
			}
			mainArtifact(remapJar) {
				displayName = "[${project.mod_mc_version_specifier}] ${project.mod_version}"
			}
		}
	}
	uploadToModSites.finalizedBy(tasks.curseforge)
}

modrinth {
	if (project.hasProperty("modrinthToken")) {
		token = project.modrinthToken
		uploadToModSites.finalizedBy(tasks.modrinth)
	}

	projectId = "u5Ic2U1u"
	versionName = "[$project.mod_mc_version_specifier] $project.mod_version"
	versionType = project.mod_release
	changelog = project.getChangelog()
	uploadFile = remapJar
	gameVersions.set(project.mod_mc_versions.split(";") as List<String>)
	loaders.set(["fabric"])
}
tasks.modrinth.group = "upload"

if (project.hasProperty("githubToken")) {
	githubRelease {
		token project.githubToken
		targetCommitish = "unstable"
		releaseName = "Version $project.mod_version for $project.mod_mc_version_specifier"
		body = project.getChangelog()
		releaseAssets remapJar.getArchiveFile()
		prerelease = mod_release != "release"
		overwrite = true
	}
	uploadToModSites.finalizedBy(tasks.githubRelease)
}
*/
