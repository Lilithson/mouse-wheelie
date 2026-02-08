import de.siphalor.jcyo.gradle.JcyoTask
import de.siphalor.mousewheelie.gradle.FormatReadmeForModSites
import java.util.*

plugins {
	alias(libs.plugins.loom)
	java
	`maven-publish`
	alias(libs.plugins.licenser)
	alias(libs.plugins.jcyo)
	alias(libs.plugins.modPublisher)
	alias(libs.plugins.modrinth)
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

license {
	rule(file("LICENSE_HEADER"))
	include("**/*.java")
}

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
		parchment(variantOf(mcLibs.parchment) {
			artifactType("zip")
		})
	})
	modImplementation(libs.fabric.loader)

	for (mod in listOf(
		"fabric-api-base",
		"fabric-events-interaction-v0",
		"fabric-item-api-v1",
		"fabric-item-group-api-v1",
		"fabric-lifecycle-events-v1",
		"fabric-key-binding-api-v1",
		"fabric-message-api-v1",
		"fabric-networking-api-v1",
		"fabric-resource-loader-v0",
		"fabric-registry-sync-v0",
		"fabric-screen-api-v1",
	)) {
		modImplementation(fabricApi.module(mod, mcLibs.versions.fabric.api.get()))
	}

	modImplementation(mcLibs.modmenu)

	include(mcLibs.bundles.config)
	modApi(mcLibs.bundles.config) {
		exclude(group = "net.fabricmc.fabric-api")
	}

	include(mcLibs.amecs.key.mapping.descriptions)
	modImplementation(mcLibs.amecs.key.mapping.descriptions) {
		exclude(group = "net.fabricmc.fabric-api")
	}
	include(mcLibs.amecs.mouse.inputs)
	modImplementation(mcLibs.amecs.mouse.inputs) {
		exclude(group = "net.fabricmc.fabric-api")
	}
	include(mcLibs.amecs.priority.key.mappings)
	modImplementation(mcLibs.amecs.priority.key.mappings) {
		exclude(group = "net.fabricmc.fabric-api")
	}
}

tasks.processResources {
    inputs.property("version", project.version)
	inputs.property("extraClientMixins", mcProps["mixins.extra.client"])

	filesMatching("fabric.mod.json") {
		expand("version" to project.version)
	}
	filesMatching("mousewheelie.mixins.json") {
		val extraClientMixins = mcProps.getProperty("mixins.extra.client")?.split(",")?.map { it.trim() } ?: listOf()
		expand("extraClientMixins" to
				if (extraClientMixins.isEmpty()) ""
				else "," + extraClientMixins.joinToString(",") { "\"$it\"" }
		)
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
	importOrder = listOf(
		"",
		"net.minecraft",
		"\\#",
	)
}

tasks.compileJava {
	dependsOn(jcyo)
}

publishing {
	publications {
		create<MavenPublication>("mod") {
			artifactId = archivesBaseName
			version = shortVersion

			from(components["java"])

			pom {
				name.set("Mouse Wheelie")
				description.set("A \"small\" client-side Minecraft mod providing various mouse wheel related utilities.")
				url.set("https://github.com/Siphalor/mouse-wheelie")

				scm {
					url.set("https://github.com/Siphalor/mouse-wheelie")
				}
			}
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

publisher {
	apiKeys {
		project.findProperty("modrinth.token")?.let { modrinth(it as String) }
		project.findProperty("curseforge.token")?.let { curseforge(it as String) }
		project.findProperty("github.token")?.let { github(it as String) }
	}

	curseID = "317514"
	modrinthID = "u5Ic2U1u"

	artifact.set(tasks.remapJar)

	projectVersion = project.version as String
	versionType = project.property("version.type") as String
	loaders = listOf("fabric")
	curseEnvironment = "client"

	gameVersions = (mcProps["mc.version.supported"] as String).split(", ")

	displayName = "[${mcProps["mc.version.title"]}] $shortVersion"
	changelog.set(providers.exec {
		commandLine("git", "log", "-1", "--format=format:##%x20%s%n%n%b%nRelease%x20by%x20%an", "--grep", "Version")
	}.standardOutput.asText.map { it.trim() })

	curseDepends {
		required("fabric-api")
	}
	modrinthDepends {
		required("fabric-api")
	}

	github {
		repo("Siphalor/mouse-wheelie")
		tag(shortVersion)
		displayName(shortVersion)
		createTag(true)
		createRelease(true)
	}
}

val formatReadmeForModSites = tasks.register<FormatReadmeForModSites>("formatReadmeForModSites") {
	input = layout.projectDirectory.file("README.md")
	output = layout.buildDirectory.dir("readme")
}

modrinth {
	project.findProperty("modrinth.token")?.let { token = it as String }
	projectId = "u5Ic2U1u"

	syncBodyFrom = providers.fileContents(layout.buildDirectory.file("readme/modrinth.md")).asText
}

tasks.modrinth {
	enabled = false
}

tasks.modrinthSyncBody {
	dependsOn(formatReadmeForModSites)
}
