# Zero-Trust Enterprise Secret Guardian

[![Version](https://img.shields.io/badge/version-2026.8.0-blue)](https://plugins.jetbrains.com/plugin/30409)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue)](LICENSE)

Eliminate security risks associated with leaked sensitive data (API keys, database access, cloud certificates) during local application development.

This plugin creates a direct bridge between cloud vaults (Infisical, Doppler) and the JetBrains development environment, completely removing the need to store `.env` files on the developer's physical disk.

## Features

- **Vault Authentication:** Secure login to Doppler and Infisical API services using user tokens stored securely via JetBrains Credential Store.
- **Environment Picker:** Easily switch between environments (e.g., Dev, Staging, Prod) instantly through the dedicated Tool Window.
- **Dynamic Run Configuration Interception:** Before a Java-based run configuration (Application, JUnit, Gradle/Maven-launched JVM, …) starts, the plugin downloads the secrets and injects them as environment variables of the launched process, so the code never reads them from a file on disk.
- **Pre-Commit Scanner (Guardrail):** Integrates into the commit dialog. Just before a commit, it scans the files being committed and warns (letting you cancel) if they contain a value that is stored in the vault.
- **Log Masking:** Injected secrets are replaced with asterisks when they appear in the Run/Debug console output. Values shorter than 5 characters and common words (`true`, `dev`, …) are not masked.

## Security (Zero-Trust)
Retrieved secrets exist solely in RAM during the IDE session and are dropped when no run configuration is running any more; they are never written to disk. Only the vault access token is persisted, in the IDE Credential Store (Password Safe).

## Installation
- Install directly from JetBrains Marketplace.
- Open the **Zero-Trust Vault** Tool Window on the right panel.
- Enter your Infisical or Doppler Token and connect.
- Run your applications as usual; secrets will be injected automatically!

## Building from source

```bash
./gradlew buildPlugin   # plugin ZIP in build/distributions/
./gradlew detekt test   # static analysis + unit tests
```

Requires JDK 21.

## Free & open source

As of version 2026.8.0 this plugin is completely free — every feature is unlocked and no license is needed. The source code is available at https://github.com/JirakJ/zero-trust-secret-guardian under the Apache License 2.0.

## Contributing

Issues and pull requests are welcome — please open them on GitHub.

## License

Copyright 2026 Jakub Jirák. Licensed under the [Apache License, Version 2.0](LICENSE).
