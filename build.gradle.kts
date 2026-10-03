import org.jreleaser.model.Active
import org.jreleaser.model.Stereotype
import java.util.*
import java.util.zip.ZipFile
import java.net.InetAddress
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/*
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.14.1/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    application
    eclipse
    idea

    id("com.gradleup.shadow") version "9.0.0-beta15"
    id("com.palantir.git-version") version "3.3.0"
    id("org.jreleaser") version "1.18.0"
}

group = "com.carolinarollergirls"

repositories {
    mavenCentral()
}

dependencies {
    implementation("commons-fileupload:commons-fileupload:1.6.0")
    implementation("commons-io:commons-io:2.21.0")

    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.apache.commons:commons-compress:1.28.0")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("org.apache.commons:commons-lang3:3.20.0")

    implementation("org.apache.poi:poi:4.1.2")
    implementation("org.apache.poi:poi-ooxml:4.1.2")
    implementation("org.apache.poi:poi-ooxml-schemas:4.1.2")

    implementation("org.apache.xmlbeans:xmlbeans:3.1.0")

    implementation("com.fasterxml.jackson.core:jackson-core:2.20.1")
    implementation("com.fasterxml.jackson.jr:jackson-jr-objects:2.20.1")

    implementation("org.eclipse.jetty:jetty-http:9.4.44.v20210927")
    implementation("org.eclipse.jetty:jetty-io:9.4.44.v20210927")
    implementation("org.eclipse.jetty:jetty-security:9.4.44.v20210927")
    implementation("org.eclipse.jetty:jetty-server:9.4.44.v20210927")
    implementation("org.eclipse.jetty:jetty-servlet:9.4.44.v20210927")
    implementation("org.eclipse.jetty:jetty-util:9.4.44.v20210927")
    implementation("org.eclipse.jetty.websocket:websocket-api:9.4.44.v20210927")
    implementation("org.eclipse.jetty.websocket:websocket-common:9.4.44.v20210927")
    implementation("org.eclipse.jetty.websocket:websocket-server:9.4.44.v20210927")
    implementation("org.eclipse.jetty.websocket:websocket-servlet:9.4.44.v20210927")

    implementation("io.prometheus:simpleclient:0.14.1")
    implementation("io.prometheus:simpleclient_common:0.14.1")
    implementation("io.prometheus:simpleclient_hotspot:0.14.1")
    implementation("io.prometheus:simpleclient_servlet:0.14.1")
    implementation("io.prometheus:simpleclient_servlet_common:0.14.1")

    compileOnly("javax.servlet:javax.servlet-api:3.1.0")

    testImplementation(libs.junit)
    testImplementation("org.hamcrest:hamcrest:2.2")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.run<JavaExec> {
    args(listOf("--nogui"))
}

val gitVersion: groovy.lang.Closure<String> by extra
version = gitVersion()

application {
    mainClass = "com.carolinarollergirls.scoreboard.Main"

    applicationName = "CRG Scoreboard"
    applicationDefaultJvmArgs = listOf(
        "-Done-jar.silent=true",
        "-Dorg.eclipse.jetty.server.LEVEL=WARN"
    )

    applicationDistribution.from(".") {
        include("README.md", "COPYING", "COPYING-GPL", "COPYING-AL", "LICENSES", "NOTICE", "start.html")
    }
    applicationDistribution.from("html") {
        include("**/*")
        exclude("game-data/**/*", "stream/**/*", "images/teamlogo/**/*", "images/sponsor_banner/**/*", "themes/custom/**/*", "**/.gitignore")
        into("html")
        includeEmptyDirs = true
    }
    // Bundle the shipped theme entry while keeping uploaded custom themes excluded.
    applicationDistribution.from("html/themes/custom") {
        include("Halloween.js")
        into("html/themes/custom")
    }
    applicationDistribution.from("config") {
        include("**/*")
        exclude("**/.gitignore", "autosave/**/*")
        into("config")
        includeEmptyDirs = true
    }
}

// Give the plain and executable JARs distinct paths so they cannot overwrite each other.
tasks.jar {
    archiveClassifier.set("plain")
}

tasks.shadowJar {
    archiveClassifier.set("")
    // Keep the first copy of ordinary resources/classes, including our Jetty MIME override.
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    mergeServiceFiles()
    filesMatching("META-INF/services/**") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    // Combine dependency license/notice texts instead of dropping all but the first.
    val legalResources = listOf("META-INF/LICENSE", "META-INF/LICENSE.txt", "META-INF/NOTICE", "META-INF/NOTICE.txt")
    legalResources.forEach { append(it) }
    filesMatching(legalResources) {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    dependsOn(tasks.distTar, tasks.distZip)
}

val verifyShadowJar by tasks.registering {
    val executableJar = tasks.shadowJar.flatMap { it.archiveFile }
    inputs.file(executableJar)
    doLast {
        ZipFile(executableJar.get().asFile).use { jar ->
            val duplicates = jar.entries().asSequence().groupingBy { it.name }.eachCount()
                .filterValues { it > 1 }.keys
            check(duplicates.isEmpty()) { "Duplicate executable JAR entries: $duplicates" }
        }
    }
}

tasks.check {
    dependsOn(verifyShadowJar)
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

val generateVersionProperties by tasks.registering {
    val generatedResources = layout.buildDirectory.dir("generated/resources")
    val outputDir = generatedResources.map { it.dir("com/carolinarollergirls/scoreboard/version") }
    val releaseVersion = providers.exec {
        commandLine("git", "describe", "--tags", "--always", "--dirty")
    }.standardOutput.asText.map { it.trim() }
    val releaseCommit = providers.exec {
        commandLine("git", "rev-parse", "HEAD")
    }.standardOutput.asText.map { it.trim() }

    inputs.property("release", releaseVersion)
    inputs.property("release.commit", releaseCommit)
    inputs.property("release.user", System.getProperty("user.name"))
    inputs.property("release.host", providers.provider {
        try {
            InetAddress.getLocalHost().hostName
        } catch (_: UnknownHostException) {
            "localhost"
        }
    })
    inputs.property("isRelease", providers.gradleProperty("isRelease").map { it.toBoolean() }.orElse(false))
    outputs.dir(generatedResources)
    // Like Ant, refresh the build timestamp for each invocation.
    outputs.upToDateWhen { false }

    doLast {
        val timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val propertiesFile = outputDir.get().file("release.properties").asFile
        propertiesFile.parentFile.mkdirs()
        val properties = Properties()
        val release = inputs.properties["release"].toString()
        properties["release"] = if (inputs.properties["isRelease"] == true) release else "$release-$timestamp"
        properties["release.commit"] = inputs.properties["release.commit"].toString()
        properties["release.user"] = inputs.properties["release.user"].toString()
        properties["release.host"] = inputs.properties["release.host"].toString()
        properties["release.time"] = timestamp
        propertiesFile.writer().use { properties.store(it, null) }
    }
}

// Keep the Ant source layout; Gradle does not require src/main/java.
sourceSets {
    main {
        java.setSrcDirs(listOf("src"))
        resources {
            setSrcDirs(listOf("src"))
            include("**/*.properties")
            srcDir(generateVersionProperties)
        }
    }
    test {
        java.setSrcDirs(listOf("tests"))
        resources {
            setSrcDirs(listOf("tests"))
            exclude("**/*.java")
        }
    }
}

tasks.classes {
    dependsOn(generateVersionProperties)
}

val installerResources by tasks.registering(Sync::class) {
    dependsOn(tasks.installDist)
    from(layout.buildDirectory.dir("install/CRG Scoreboard")) {
        exclude("bin/**", "lib/**")
    }
    into(layout.buildDirectory.dir("installer-resources"))
}

val packagingJdk = javaToolchains.launcherFor {
    languageVersion = JavaLanguageVersion.of(17)
}.map { it.metadata.installationPath.asFile.absolutePath }

val packagingPlatform = providers.provider {
    val os = System.getProperty("os.name").lowercase().let {
        when {
            it.startsWith("windows") -> "windows"
            it.startsWith("mac") -> "osx"
            it.startsWith("linux") -> "linux"
            else -> error("Unsupported packaging OS: $it")
        }
    }
    val arch = when (val arch = System.getProperty("os.arch")) {
        "amd64", "x86_64" -> "x86_64"
        "aarch64", "arm64" -> "aarch_64"
        else -> error("Unsupported packaging architecture: $arch")
    }
    "$os-$arch"
}

val installerVersion = providers.gradleProperty("installerVersion").orElse(
    providers.provider {
        Regex("\\d+\\.\\d+(?:\\.\\d+)?").find(project.version.toString())?.value
            ?: error("Set -PinstallerVersion to a numeric installer version")
    }
)

jreleaser {
    project {
        name = "CRG Scoreboard"
        description = "A browser-based scoreboard solution for Roller Derby."
        longDescription = """
            |The CRG ScoreBoard is a browser-based scoreboard solution
            |that also provides overlays for video production
            |and the ability to track full game data and export it to a WFTDA statsbook.
            """.trimMargin()

        versionPattern = "CUSTOM"

        inceptionYear = "2008"
        authors = listOf("Mr Temper", "The CRG developers")
        copyright = "2008-2012 Mr Temper, since 2012 The CRG developers"
        license = "Apache-2.0-or-GPL-3.0-or-later"

        links {
            vcsBrowser = "https://github.com/rollerderby/scoreboard"
            bugTracker = "https://github.com/rollerderby/scoreboard/issues"
            documentation = "https://github.com/rollerderby/scoreboard/wiki"
            homepage = "https://github.com/rollerderby/scoreboard"
            license = "https://github.com/rollerderby/scoreboard/blob/dev/COPYING"
        }
        stereotype = Stereotype.WEB
    }

    if (providers.gradleProperty("nativeRelease").isPresent) {
        files {
            listOf("dmg", "deb", "rpm", "exe").forEach { extension ->
                glob {
                    pattern = "build/native-release/*.$extension"
                }
            }
        }
    } else {
        assemble {
            jlink {
                create("scoreboard-runtime") {
                    active = Active.ALWAYS
                    exported = false
                    copyJars = false
                    executable = "scoreboard"
                    java {
                        mainClass = "com.carolinarollergirls.scoreboard.NativeMain"
                    }
                    targetJdk {
                        path.set(file(packagingJdk.get()))
                        platform = packagingPlatform.get()
                    }
                    mainJar { path.set(tasks.shadowJar.flatMap { it.archiveFile }) }
                    moduleNames = setOf("ALL-MODULE-PATH")
                }
            }
            jpackage {
                create("scoreboard") {
                    active = Active.ALWAYS
                    jlink = "scoreboard-runtime"
                    attachPlatform = true
                    mainJar { path.set(tasks.shadowJar.flatMap { it.archiveFile }) }
                    java {
                        mainClass = "com.carolinarollergirls.scoreboard.NativeMain"
                    }
                    applicationPackage {
                        appName = "CRG Scoreboard"
                        appVersion = installerVersion.map { value ->
                            val components = value.split(".").map { it.toInt() }.toMutableList()
                            if (packagingPlatform.get().startsWith("windows-") && components[0] >= 2000) {
                                components[0] -= 2000
                            }
                            components.joinToString(".")
                        }.get()
                        vendor = "The CRG developers"
                        licenseFile = "COPYING"
                    }
                    launcher {
                        arguments = listOf("--gui")
                        javaOptions = application.applicationDefaultJvmArgs.toList() +
                            "-Dscoreboard.installDir=\$APPDIR"
                    }
                    linux {
                        types = listOf("deb", "rpm")
                        packageName = "crg-scoreboard"
                    }
                    osx {
                        types = listOf("dmg")
                        packageIdentifier = "com.carolinarollergirls.scoreboard"
                    }
                    windows {
                        types = listOf("exe")
                        perUserInstall = true
                        dirChooser = true
                        menu = true
                        shortcut = true
                        upgradeUuid = "fd184a99-5bfa-4d0b-b2c5-c0960879777a"
                    }
                    fileSet {
                        input = layout.buildDirectory.dir("installer-resources").get().asFile.path
                    }
                }
            }
        }
    }

    release {
        github {
            tagName = "{{projectVersion}}"
        }
    }

    distributions {
        create("app") {
            active = Active.ALWAYS
            distributionType = org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR
            artifact {
                // Point to the shadow JAR instead of a ZIP distribution
                path.set(tasks.shadowJar.flatMap { it.archiveFile })
            }
        }
    }
}

tasks.startScripts {
    doLast {
        // Make the startup script change the working directory to the APP_HOME before launching the application.
        val unixScript = unixScript
        val unixScriptText = unixScript.readText()
        val modifiedUnixScript = unixScriptText.replace(
            "exec \"\$JAVACMD\" \"$@\"", "cd \"\$APP_HOME\" || exit 1\nexec \"\$JAVACMD\" \"$@\""
        )
        unixScript.writeText(modifiedUnixScript)

        val windowsScript = windowsScript
        val windowsScriptText = windowsScript.readText()
        val modifiedWindowsScript = windowsScriptText.replace(
            "\"%JAVA_EXE%\" %DEFAULT_JVM_OPTS%", "cd /d \"%APP_HOME%\"\n\"%JAVA_EXE%\" %DEFAULT_JVM_OPTS%"
        )
        windowsScript.writeText(modifiedWindowsScript)
    }
}

tasks.named("jreleaserAssemble") {
    inputs.file(layout.projectDirectory.file("build.gradle.kts"))
    inputs.property("installerVersion", installerVersion)
    dependsOn(tasks.shadowJar, installerResources)
}

tasks.named("jreleaserRelease") {
    dependsOn(tasks.shadowJar)
}
