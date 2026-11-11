plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.10.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
}

group = "com.jakubjirak"
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(providers.gradleProperty("javaVersion").get().toInt())
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create(
            providers.gradleProperty("platformType"),
            providers.gradleProperty("platformVersion"),
        )
        bundledPlugin("com.intellij.java")
        pluginVerifier()
        zipSigner()
    }

    // HTTP client for the vault REST APIs. Kotlin stdlib, coroutines and Gson come from the IDE.
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(kotlin("test"))
}

// Exclude IDE-provided libraries from the plugin distribution
configurations {
    runtimeClasspath {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.jetbrains", module = "annotations")
    }
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = provider { null }
        }

        changeNotes = """
            <h3>2026.8.0</h3>
            <ul>
                <li>The plugin is now completely free — all features unlocked, no license needed.</li>
                <li>Source code is now open source under Apache-2.0: https://github.com/JirakJ/zero-trust-secret-guardian</li>
            </ul>
            <h3>2026.2.1</h3>
            <ul>
                <li>Initial release of Zero-Trust Enterprise Secret Guardian.</li>
                <li>Infisical and Doppler integration.</li>
                <li>In-memory secret injection for local run configurations.</li>
                <li>Pre-commit scanning.</li>
                <li>Log masking.</li>
            </ul>
        """.trimIndent()
    }

    pluginVerification {
        ides {
            create("IC", "2024.2") { useInstaller = false }
            create("IC", "2024.3") { useInstaller = false }
            create("IC", "2025.1.7") { useInstaller = false }
            create("IC", "2025.3.4") { useInstaller = false }
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PLUGIN_TOKEN")
    }
}

tasks {
    test {
        useJUnitPlatform()
    }

    wrapper {
        gradleVersion = "8.13"
    }
}

detekt {
    config.setFrom(files("detekt.yml"))
    buildUponDefaultConfig = true
}
