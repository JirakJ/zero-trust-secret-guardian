plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.jakubjirak"

val pluginVersion: String by project
version = pluginVersion

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    // Use an HTTP client for API requests (e.g., Ktor or OkHttp)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation(kotlin("test"))
}

intellij {
    val platformVersion: String by project
    version.set(platformVersion)
    type.set("IU")
    
    // Plugins required by this plugin
    plugins.set(listOf("java", "JavaScript"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("242")
        untilBuild.set("")
        
        pluginDescription.set("""
            <p><strong>Zero-Trust Enterprise Secret Guardian</strong></p>
            <p>Eliminate security risks associated with leaked sensitive data (API keys, database access, cloud certificates) during local application development.</p>
            <ul>
                <li>✓ Authenticate to Infisical and Doppler cloud vaults.</li>
                <li>✓ Environment Picker to instantly switch between Dev, Staging, Prod.</li>
                <li>✓ Dynamic Run Configuration Interception to inject secrets in memory without writing to disk.</li>
                <li>✓ Pre-Commit Scanner Guardrail to block committing known vault secrets.</li>
                <li>✓ Log Masking to censor injected secrets in terminal and IDE application logs.</li>
            </ul>
        """.trimIndent())
        
        changeNotes.set("""
            <h3>2026.2.1</h3>
            <ul>
                <li>🚀 Initial release of Zero-Trust Enterprise Secret Guardian.</li>
                <li>✓ Infisical and Doppler integration.</li>
                <li>✓ In-memory secret injection for local run configurations.</li>
                <li>✓ Pre-commit scanning.</li>
                <li>✓ Log masking.</li>
            </ul>
        """.trimIndent())
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PLUGIN_TOKEN"))
    }
    
    buildSearchableOptions {
        enabled = false
    }

    test {
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain(17)
}
