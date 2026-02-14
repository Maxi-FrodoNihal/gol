plugins {
    kotlin("jvm") version "2.3.0"
    id("org.jetbrains.compose") version "1.10.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0"
}

group = "org.msc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.0")
    implementation(compose.desktop.currentOs)
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "org.msc.MainKt"
        jvmArgs += listOf(
            "--enable-native-access=ALL-UNNAMED"
        )

        nativeDistributions {
            // Beide OS-Targets
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.AppImage
            )

            packageName = "GameOfLife"
            packageVersion = "1.0.0"
            description = "Conway's Game of Life"
            vendor = "MSC"

            // Windows-spezifische Einstellungen
            windows {
                menuGroup = "Games"
                upgradeUuid = "BF9CDA6A-1391-46D5-9ED5-383D6E68CCEB"
            }

            // Linux-spezifische Einstellungen
            linux {
                menuGroup = "Games"
            }
        }
    }
}

// Create a fat JAR task manually
tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Creates a fat JAR with all dependencies"

    archiveBaseName.set("game-of-life")
    archiveVersion.set("1.0")

    manifest {
        attributes(
            "Main-Class" to "org.msc.MainKt"
        )
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    from(sourceSets.main.get().output)

    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}

tasks.build {
    dependsOn("fatJar")
}