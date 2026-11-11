# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

## [2026.9.0] - 2026-11-11

### Fixed

- **Vault connection never worked:** request URLs, the `Authorization` header and several messages contained escaped `\$` placeholders instead of the actual values, so every Doppler/Infisical call hit a literal `$baseUrl/...` URL. Query parameters are now URL-encoded.
- HTTP calls now have timeouts and only retry server errors (5xx); authentication errors fail fast instead of being retried three times.
- The pre-commit scan no longer blocks the UI thread while fetching secrets, scans only text files and never echoes secret values in its warning.
- Console masking replaces longer secrets before shorter ones and is thread-safe.

### Removed

- Dead `ZeroTrustLogFilterProvider` (never registered, did nothing) and the unused `JavaScript` platform dependency.

## [2026.8.0] - 2026-11-10

### Changed

- The plugin is now completely free — all features unlocked, no license needed.
- Source code is now open source under Apache-2.0: https://github.com/JirakJ/zero-trust-secret-guardian

## [2026.2.10] - 2026-02-28
### Fixed
- **API Deprecation:** Resolved warning regarding `CredentialAttributes` constructor by using the modern `generateServiceName` API.

## [2026.2.9] - 2026-02-28
### Changed
- **All Features Free:** Removed product-descriptor and licensing restrictions; all features are now available to everyone at no cost.

## [2026.2.8] - 2026-02-28
### Added
- **Enterprise Pre-Commit:** The Pre-Commit Guardrail checks for secrets before allowing `git commit`.

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
