# Zero-Trust Enterprise Secret Guardian

[![Version](https://img.shields.io/badge/version-2026.8.0-blue)](https://plugins.jetbrains.com/plugin/30409)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue)](LICENSE)

Eliminate security risks associated with leaked sensitive data (API keys, database access, cloud certificates) during local application development.

This plugin creates a direct bridge between cloud vaults (Infisical, Doppler) and the JetBrains development environment, completely removing the need to store `.env` files on the developer's physical disk.

## Features

- **Vault Authentication:** Secure login to Doppler and Infisical API services using user tokens stored securely via JetBrains Credential Store.
- **Environment Picker:** Easily switch between environments (e.g., Dev, Staging, Prod) instantly through the dedicated Tool Window.
- **Dynamic Run Configuration Interception:** The plugin intercepts the IDE's execution process. Before the application starts, it asynchronously downloads the secrets and injects them as environment variables directly into the process memory (JVM, Node.js, Python), ensuring the code never reads them from the disk.
- **Pre-Commit Scanner (Guardrail):** Integrates into the commit dialog. Just before a commit, it scans modified files and blocks the action if it detects strings matching known keys from the vault.
- **Log Masking:** All injected secrets are automatically censored (replaced with asterisks) if they appear in the integrated terminal or IDE application logs.

## Security (Zero-Trust)
Retrieved secrets exist solely in RAM during the IDE session. The plugin strictly adheres to zero-trust principles and does not store secrets in any local cache, registry, or disk files.

## Installation
- Install directly from JetBrains Marketplace.
- Open the **Zero-Trust Vault** Tool Window on the right panel.
- Enter your Infisical or Doppler Token and connect.
- Run your applications as usual; secrets will be injected automatically!

## Free & open source

As of version 2026.8.0 this plugin is completely free — every feature is unlocked and no license is needed. The source code is available at https://github.com/JirakJ/zero-trust-secret-guardian under the Apache License 2.0.

## Contributing

Issues and pull requests are welcome — please open them on GitHub.

## License

Copyright 2026 Jakub Jirák. Licensed under the [Apache License, Version 2.0](LICENSE).
