import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jreleaser.model.Active
import org.jreleaser.model.Stereotype
import java.util.*

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
    implementation(libs.guava)

    implementation("commons-fileupload:commons-fileupload:1.4")
    implementation("commons-io:commons-io:2.11.0")

    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.apache.commons:commons-compress:1.21")
    implementation("org.apache.commons:commons-math3:3.6.1")

    implementation("org.apache.poi:poi:4.1.2")
    implementation("org.apache.poi:poi-ooxml:4.1.2")
    implementation("org.apache.poi:poi-ooxml-schemas:4.1.2")

    implementation("org.apache.xmlbeans:xmlbeans:3.1.0")

    implementation("com.fasterxml.jackson.core:jackson-core:2.13.1")
    implementation("com.fasterxml.jackson.jr:jackson-jr-objects:2.13.1")

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
    testImplementation("net.bytebuddy:byte-buddy:1.12.6")
    testImplementation("net.bytebuddy:byte-buddy-agent:1.12.6")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.mockito:mockito-core:4.2.0")
    testImplementation("org.objenesis:objenesis:3.2")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
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

    applicationDistribution.from("html") {
        include("**/*")
        exclude("game-data/**/*", "**/.gitignore")
        into("html")
        includeEmptyDirs = true
    }
    applicationDistribution.from("config") {
        include("**/*")
        exclude("**/.gitignore", "autosave/**/*")
        into("config")
        includeEmptyDirs = true
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    mergeServiceFiles()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

val generateVersionProperties by tasks.registering {
    val generatedResources = layout.buildDirectory.dir("generated/resources")
    val projectVersion = project.version
    val outputDir = generatedResources.map { it.dir("com/carolinarollergirls/scoreboard/version") }

    inputs.property("version", projectVersion)
    outputs.dir(generatedResources)

    doLast {
        val propertiesFile = outputDir.get().file("release.properties").asFile
        propertiesFile.parentFile.mkdirs()
        propertiesFile.writer().use { writer ->
            val properties = Properties()
            properties["release"] = inputs.properties["version"].toString()
            properties.store(writer, null)
        }
    }
}

sourceSets {
    main {
        resources {
            srcDir(generateVersionProperties)
        }
    }
}

tasks.classes {
    dependsOn(generateVersionProperties)
}

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
