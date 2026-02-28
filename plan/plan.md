# Zero-Trust Enterprise Secret Guardian - Future Development Plan

Tento dokument obsahuje seznam chybějících funkčností a vylepšení pro budoucí iterace (včetně "Nice to have" pro Enterprise vrstvu), které posunou plugin do absolutní B2B "Production-Ready" podoby. Aktuálně je splněno jádro Fáze 1, 2 a 3 (autentizace, dynamická injekce, pre-commit scany, live testování), a navíc i podpora Custom URL a Retry logiky z Fáze 4. Níže jsou sepsány plány pro Fázi 4 a 5 a pokročilá vylepšení.

## 1. Pokročilá uživatelská nastavení a On-Premise podpora (Phase 3/4)
- [x] **Custom Vault URLs (On-Premise):** (Implementováno ve v2026.2.4)
- **Workspace/Project Auto-Discovery:** Místo manuálního vypisování ID projektu by měl plugin po vložení platného tokenu zavolat API pro získání seznamu projektů, ke kterým má uživatel přístup, a naplnit jimi ComboBox v Tool Window.

## 2. Pokročilá Run Configuration a Ošetření paměti (Phase 4)
- **Komplexní Injection logika (Support jiných jazyků než Java/Kotlin):** Současný `updateJavaParameters` v `RunConfigurationExtension` spolehlivě funguje na JVM procesech. Pro Node.js, Python, Go nebo Docker je potřeba prozkoumat další Extension Points (např. implementovat `ProgramRunner` nebo specifické `RunConfigurationExtension` pro Python/JS, pokud obecné `params.env` nezafunguje ve všech run configuration typech).
- [x] **Asynchronní fetching před spuštěním:** (Implementováno ve v2026.2.5 pomocí ProgressManager v rámci `updateJavaParameters`).
- [x] **Plnohodnotný In-Memory Cache LifeCycle:** Tajemství v `ZeroTrustSecretCache` by se mělo spolehlivě pročistit po ukončení běhu aplikace, aby v paměti IDE neležela déle, než je nezbytně nutné (posílení Zero-Trust modelu). (Implementováno `ZeroTrustExecutionListener` ve v2026.2.5).

## 3. Ošetření síťových výpadků a Error Handling (Phase 4)
- [x] Přidat robustnější "Retry" mechaniku do `OkHttp` volání. (Implementováno ve v2026.2.4)
- [x] Implementovat fallback okno, které se zobrazí uživateli při spouštění aplikace, pokud je Vault nedostupný: *"Nelze načíst tajemství z Vaultu. Chcete pokračovat bez nich, zkusit znovu, nebo zrušit spuštění?"* (Implementováno ve v2026.2.5)

## 4. UI/UX Polishing
- [x] V Tool Window přidat animovaný spinner (načítání) při testování připojení a stahování tajemství. (Implementováno ve v2026.2.7)
- [x] Zlepšit maskování logů: `ConsoleInputFilterProvider` je naimplementován funkčně, ale pokud je tajemství obsaženo v JSON objektu na výstupu (např. `{ "token": "SUPER_SECRET" }`), maskování celého řetězce funguje jen primitivně na přímou shodu textu. Je nutné otestovat edge-cases (vyvarovat se maskování příliš krátkých klíčů). (Vylepšeno ve v2026.2.6 - ignorace krátkých klíčů a common values).

## 5. Enterprise Licencování a Monetizace (Phase 5)
- [x] Implementovat napojení na JetBrains Licensing API pro ověření předplatného a zapnutí/vypnutí prémiových funkcí. (Implementován Mock LicenseManager pro Fázi 5).
- [x] Oddělit Freemium funkčnosti (např. omezení počtu získaných proměnných nebo zamezení pre-commit scannování ve Free verzi). (Pre-commit scanner je nyní exkluzivní pro Enterprise verze).

## 6. Telemetrie a Error Reporting
- Přidat anonymizovaný sběr telemetrických dat (Sentry / JetBrains Analytics), pokud uživatel souhlasí. Obzvláště důležité pro hlášení výjimek při selhání injekce prostředí, abyste mohli plugin iterativně zlepšovat.
