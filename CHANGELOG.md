# Changelog

All notable changes to this project will be documented in this file.

## [2026.2.8] - 2026-02-28
### Added
- **Licensing Core:** Introduced `LicenseManager` as the foundation for the Freemium / Enterprise model.
- **Enterprise Pre-Commit:** The Pre-Commit Guardrail (checking for secrets before allowing `git commit`) is now correctly flagged as an exclusive Enterprise feature.

## [2026.2.7] - 2026-02-28
### Added
- **UI Enhancements:** Added an `AsyncProcessIcon` (loading spinner) to the Tool Window to provide immediate visual feedback while testing connection and downloading secrets.

## [2026.2.6] - 2026-02-28
### Added
- **Log Masking Improvements:** Tweaked the log masking logic to ignore short values (under 5 characters) and common logging strings (e.g. true, false, dev, numbers) to avoid accidental masking of regular log lines.

## [2026.2.5] - 2026-02-28
### Added
- **Synchronous Progress & Fallback UI:** Implemented modal progress indicator during execution startup. If connection to vault fails, users are now prompted to retry, cancel, or continue without secrets.
- **Cache Lifecycle Management:** Secrets loaded in RAM are now safely flushed after the running process terminates (via `ExecutionListener`), enforcing stricter zero-trust compliance.

## [2026.2.4] - 2026-02-28
### Added
- **On-Premise Support:** Added a "Custom Base URL" field to support enterprise instances of Infisical and Doppler hosted on private networks.
- **Resilience:** Implemented a robust network retry mechanism (up to 3 retries) for OkHttp clients to handle transient connection drops.

## [2026.2.3] - 2026-02-27
### Changed
- **Package Refactoring:** Updated base package to `com.jakubjirak` and corrected support email to `support@jakubjirak.com` across all configuration files.
- **Real Vault Integration:** Replaced mock vault clients with live `OkHttp` network calls to Infisical and Doppler APIs.
- **UI Improvements:** Added live connection testing with visual status indicators in the Tool Window.

## [2026.2.1] - 2026-02-27
### Added
- **Vault Integration:** Added support for Infisical and Doppler cloud vaults.
- **Environment Picker Tool Window:** New UI to manage vault provider, project, environment, and access token.
- **In-Memory Secret Injection:** Intercepts Java run configurations to inject fetched secrets directly into environment variables without writing to disk.
- **Pre-Commit Scanner:** Added a guardrail to prevent committing known vault secrets into version control.
- **Log Masking:** Automatically censors injected secrets from appearing in the IDE's console output.
