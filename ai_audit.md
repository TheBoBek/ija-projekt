# AI Audit Log - Tým xmarina00

**Datum poslední aktualizace:** 16. 5. 2026

---

Prompty: TheBoBek

## 1. Přesun základu z úkolu 2

- **Nástroj:** ChatGPT Codex
- **Datum:** 1. 5. 2026
- **Prompt:**
  > "help me move my task 2 code to package ija and keep the old loader paths working"
- **Co udělal Codex:** Navrhl přesun tříd do balíčků `ija.common`, `ija.game` a `ija.observer` a nechal v `GameFactory` staré textové cesty `createGame(String[])` a `createGame(Path)`.
- **Co jsem upravil ručně:** Ručně jsem dořešil importy, napojení `Observable` a `GameObserver` a pohlídal, aby se kvůli tomu nerozbila starší část API používaná v `GameTest` a `AdditionalEdgeCasesTest`.
- **Ověření:** Kontrola signatur a průchod starého textového parseru v `GameFactory`.
- **Podíl AI:** Tady jsem z návrhu použil jen část, protože bylo potřeba zachovat staré chování z úkolu 2.

---

## 2. Rozšíření `TerrainType`

- **Nástroj:** ChatGPT Codex
- **Datum:** 2. 5. 2026
- **Prompt:**
  > "add city factory and hq to TerrainType and also helper methods for buildings and passable terrain"
- **Co udělal Codex:** Připravil rozšíření `TerrainType` o `CITY`, `FACTORY` a `HQ` a navrhl pomocné metody pro budovy a průchodnost.
- **Co jsem upravil ručně:** Hodnoty a význam jednotlivých helperů jsem ještě doladil podle `terrain.tsv`, hlavně kvůli tomu, aby se nepletla obrana, příjem a samotná průchodnost.
- **Ověření:** Porovnání s `terrain.tsv` a kontrola souvisejících případů v `AdditionalEdgeCasesTest`.
- **Podíl AI:** Codex pomohl hlavně s kostrou enumu, ale konkrétní význam polí a helperů jsem si hlídal ručně.

---

## 3. `Tile` a stav budov

- **Nástroj:** ChatGPT Codex
- **Datum:** 2. 5. 2026
- **Prompt:**
  > "make Tile for one map field with terrain owner and capture state only for buildings"
- **Co udělal Codex:** Navrhl třídu `Tile` s terénem, volitelným vlastníkem a capture stavem pro budovy.
- **Co jsem upravil ručně:** První verzi jsem nepřevzal celou, protože zbytečně míchala chování běžných polí a budov. Ručně jsem nechal ownera a capture points jen tam, kde dává smysl `CITY`, `FACTORY` nebo `HQ`.
- **Ověření:** Kontrola pravidel nad `Tile` a scénáře pro budovy v `BuildingServiceTest`.
- **Podíl AI:** Návrh přišel od Codexu, ale validace a hranice mezi obyčejným polem a budovou jsem si upravoval sám.

---

## 4. Testy pro `Tile` a terén

- **Nástroj:** ChatGPT Codex
- **Datum:** 2. 5. 2026
- **Prompt:**
  > "write edge case tests for tile owner building helpers and terrain values"
- **Co udělal Codex:** Vypsal víc edge-case scénářů pro ownera budov, helpery v `TerrainType` a práci s `Tile`.
- **Co jsem upravil ručně:** Nenechal jsem všechno. Vybral jsem jen testy, které fakt seděly na zadání a na naše API, zbytek by z logu dělal spíš šum.
- **Ověření:** Kontrola vybraných scénářů proti `AdditionalEdgeCasesTest`.
- **Podíl AI:** Codex byl užitečný hlavně na seznam možných edge casů, finální výběr testů byl na mně.

---

## 5. Kostra `JsonMapLoader`

- **Nástroj:** ChatGPT Codex
- **Datum:** 2. 5. 2026
- **Prompt:**
  > "help me load map from json with metadata grid buildings and units using gson"
- **Co udělal Codex:** Připravil kostru `JsonMapLoader`, rozdělení na metadata, grid, buildings a units a record `LoadedMapData`.
- **Co jsem upravil ručně:** Formát jsem si přizpůsobil tomu, co jsem chtěl opravdu držet v projektu, a nechal jsem `createGameFromJson(...)` bokem od starých textových cest v `GameFactory`.
- **Ověření:** Kontrola `JsonMapLoader`, `GameFactory` a ukázkových JSON scénářů v `AdditionalEdgeCasesTest`.
- **Podíl AI:** Codex pomohl hlavně s kostrou loaderu a s rozdělením práce do menších validačních kroků.

---

## 6. Validace JSON mapy

- **Nástroj:** ChatGPT Codex
- **Datum:** 2. 5. 2026
- **Prompt:**
  > "check validation for json metadata grid duplicate buildings and owner values"
- **Co udělal Codex:** Navrhl validace pro metadata, obdélníkový grid, duplicitní budovy, chybějící building entry a owner hodnoty.
- **Co jsem upravil ručně:** Ten první návrh byl moc obecný, takže jsem nechal jen to, co sedělo na náš JSON formát: mismatch rozměrů, neznámý token, duplicity, missing building entry a nepodporovaný owner.
- **Ověření:** Průchod negativních JSON scénářů v `AdditionalEdgeCasesTest` a kontrola podmínek přímo v `JsonMapLoader`.
- **Podíl AI:** Tady mi Codex pomohl spíš jako návrhovač variant validací, finální výběr pravidel jsem si zúžil ručně.

---

## 7. Načítání jednotek ze scénáře

- **Nástroj:** ChatGPT Codex
- **Datum:** 2. 5. 2026
- **Prompt:**
  > "parse scenario units from json and connect them to GameFactory"
- **Co udělal Codex:** Navrhl parser jednotek v `JsonMapLoader`, kontroly ownera, typu, duplicitní pozice a neprůchozího pole a pak napojení přes `GameFactory`.
- **Co jsem upravil ručně:** Ručně jsem nechal jednoduché ownery `P1` a `P2`, použil `UnitType.fromName(...)` a napojil vytvoření jednotek přes existující `game.createUnit(...)`.
- **Ověření:** Kontrola scénářů pro špatný owner, neplatný typ, duplicitu pozice a neprůchodné pole v `AdditionalEdgeCasesTest`.
- **Podíl AI:** Návrh přišel od Codexu, ale napojení na stávající `GameFactory` a `Game` jsem dělal ručně.

---

## 8. Testy pro JSON loader

- **Nástroj:** ChatGPT Codex
- **Datum:** 2. 5. 2026
- **Prompt:**
  > "write tests for invalid json maps duplicate buildings and invalid units"
- **Co udělal Codex:** Připravil návrhy testů pro invalid metadata, duplicate buildings, invalid units a chování při špatných JSON scénářích.
- **Co jsem upravil ručně:** Testy jsem neskládal do nové samostatné třídy, ale nechal je tam, kde už seděly do zbytku projektu, hlavně v `AdditionalEdgeCasesTest`.
- **Ověření:** Kontrola, že navržené případy opravdu odpovídají validacím v `JsonMapLoader`.
- **Podíl AI:** Codex byl užitečný hlavně na seznam chybových scénářů, finální testy jsem dost přebral a zredukoval.

---

## 9. Rozšíření `UnitType`

- **Nástroj:** ChatGPT Codex
- **Datum:** 2. 5. 2026
- **Prompt:**
  > "extend UnitType with artillery combat data and fromName mapping"
- **Co udělal Codex:** Navrhl rozšíření `UnitType` o `ARTILLERY`, cenu, pohyb, attack range, capture flag a pomocné mapování textových názvů.
- **Co jsem upravil ručně:** Hodnoty jsem si porovnal s `units.tsv` a nechal jsem jen takové mapování názvů, které sedělo na JSON vstupy a testy. Nechtěl jsem z toho dělat zbytečně chytrý parser.
- **Ověření:** Porovnání s `units.tsv` a metadata scénáře v `AdditionalEdgeCasesTest`.
- **Podíl AI:** Codex pomohl s kostrou enumu, ale konkrétní hodnoty a mapování názvů jsem hlídal ručně.

---

## 10. Změny v `Unit`

- **Nástroj:** ChatGPT Codex
- **Datum:** 2. 5. 2026
- **Prompt:**
  > "add hp helpers and moved acted flags to Unit but keep old behavior"
- **Co udělal Codex:** Připravil mechanické doplnění metod jako `takeDamage`, `heal`, `isDestroyed` a turn flagů `moved/acted`.
- **Co jsem upravil ručně:** Dával jsem pozor, aby zůstalo použitelné původní `toString()` a aby se neporušila starší logika pohybu. Tady šlo dost o kompatibilitu se zbytkem kódu.
- **Ověření:** Kontrola `UnitTurnFlagsTest`, částí `AdditionalEdgeCasesTest` a staršího `GameTest`.
- **Podíl AI:** Tady šlo hlavně o mechanické doplnění metod, ale kompatibilitu jsem si hlídal sám.

---

## 11. `UnitDamageTable`

- **Nástroj:** ChatGPT Codex
- **Datum:** 2. 5. 2026
- **Prompt:**
  > "make UnitDamageTable from unit matchups and write tests for values"
- **Co udělal Codex:** Navrhl `UnitDamageTable`, základní uložení damage matchupů a první sadu testů pro očekávané hodnoty.
- **Co jsem upravil ručně:** Hodnoty jsem srovnal s `units-damage.tsv` a v `UnitDamageTableTest` jsem nechal hlavně kontroly reálných kombinací a pár null scénářů.
- **Ověření:** Porovnání s `units-damage.tsv` a průchod `UnitDamageTableTest`.
- **Podíl AI:** Codex mi pomohl hlavně s kostrou tabulky a se seznamem kombinací, kontrolu hodnot jsem dělal ručně.

---

## 12. Základ `CombatService`

- **Nástroj:** ChatGPT Codex
- **Datum:** 7. 5. 2026
- **Prompt:**
  > "make CombatResult and CombatService canAttack with integer damage formula"
- **Co udělal Codex:** Připravil `CombatResult`, základ `CombatService`, pravidla pro `canAttack(...)` a integer verzi damage vzorce.
- **Co jsem upravil ručně:** Pravidla jsem dorovnal podle zadání, hlavně owner check, mrtvé jednotky, melee vs artillery range a zákaz střelby pro dělostřelectvo po pohybu.
- **Ověření:** Kontrola proti zadání a první části `CombatServiceTest`.
- **Podíl AI:** Codex pomohl s kostrou a s rozdělením pravidel do menších metod, finální podmínky jsem doladil ručně.

---

## 13. Protiofenzíva a debug souboje

- **Nástroj:** ChatGPT Codex
- **Datum:** 7. 5. 2026
- **Prompt:**
  > "add counterattack and debug why my combat formula gives wrong numbers"
- **Co udělal Codex:** Navrhl protiofenzívu po přežití obránce a pomohl rozebrat, kde se může rozbít integer výpočet poškození.
- **Co jsem upravil ručně:** Několik soubojů jsem si přepočítal ručně podle vzorce a nenechal jsem všechno z prvního návrhu, protože jsem chtěl pohlídat, že obránce útočí už se sníženým HP.
- **Ověření:** Ruční přepočet několika damage hodnot a průchod scénářů v `CombatServiceTest`.
- **Podíl AI:** Tady jsem z návrhu nepřevzal všechno, protože jsem si chtěl sám pohlídat přesný výpočet.

---

## 14. `BuildingService` a capture/heal model

- **Nástroj:** ChatGPT Codex
- **Datum:** 7. 5. 2026
- **Prompt:**
  > "make BuildingService and CaptureResult for heal and capture rules"
- **Co udělal Codex:** Připravil `BuildingService`, record `CaptureResult` a návrh pravidel pro heal a capture na `CITY`, `FACTORY` a `HQ`.
- **Co jsem upravil ručně:** Musel jsem ručně rozhodnout, co ještě patří do služby a co už má zůstat v `Game`. Nechal jsem heal jen na vlastněných budovách a capture jen pro pěchotu na neutrálních nebo nepřátelských budovách.
- **Ověření:** Kontrola `BuildingService` proti zadání a souvisejících případů v `BuildingServiceTest`.
- **Podíl AI:** Návrh přišel od Codexu, ale hranice mezi `BuildingService` a `Game` jsem si upravil podle zbytku projektu.

---

## 15. Testy pro budovy

- **Nástroj:** ChatGPT Codex
- **Datum:** 7. 5. 2026
- **Prompt:**
  > "write tests for BuildingService heal capture ownership and game hooks"
- **Co udělal Codex:** Navrhl větší sadu testů pro ownership helpery, heal, capture a game hooky jako `healUnitOnCurrentTile(...)` a `captureBuilding(...)`.
- **Co jsem upravil ručně:** Výběr jsem dost proškrtal. Nechal jsem jen scénáře, které seděly na naše API a opravdu něco hlídaly, místo toho aby jen opakovaly implementaci.
- **Ověření:** Průchod `BuildingServiceTest` a kontrola, že scénáře sedí na reálné metody v `Game`.
- **Podíl AI:** Codex byl užitečný hlavně na návrh scénářů, finální podoba testů byla dost ruční.

---

## 16. Integrace útoku do `Game`

- **Nástroj:** ChatGPT Codex
- **Datum:** 7. 5. 2026
- **Prompt:**
  > "add canAttack and attack into Game and keep observer event after unit removal"
- **Co udělal Codex:** Navrhl integraci `CombatService` do `Game`, rozšíření `GameEvent` pro útok a logiku pro odstranění zničených jednotek.
- **Co jsem upravil ručně:** Dopsal jsem kontroly pozic, ohlídal zachování starších move eventů a opravil pořadí tak, aby observer event dával smysl i když se jednotka po útoku smaže.
- **Ověření:** Kontrola `Game`, `GameEvent` a návaznosti na `GameCombatIntegrationTest`.
- **Podíl AI:** Tady pomohl hlavně s prvním návrhem integrace, ale detaily kolem eventů a mazání jednotek jsem dolaďoval ručně.

---

## 17. Integrační testy souboje

- **Nástroj:** ChatGPT Codex
- **Datum:** 7. 5. 2026
- **Prompt:**
  > "write integration tests for Game attack valid invalid cases and observer payload"
- **Co udělal Codex:** Připravil základ scénářů pro valid/invalid attack, odstranění jednotek a observer payload po souboji.
- **Co jsem upravil ručně:** Očekávané HP a některé rohy jsem dopočítával podle reálné implementace, nebral jsem slepě první návrh testů.
- **Ověření:** Průchod `GameCombatIntegrationTest` a kontrola proti chování `Game.attack(...)`.
- **Podíl AI:** Codex navrhl dobrý základ scénářů, ale očekávané výsledky jsem doplňoval podle konkrétní implementace.

---

## 18. `Player` a `Turn`

- **Nástroj:** ChatGPT Codex
- **Datum:** 11. 5. 2026
- **Prompt:**
  > "make simple Player and Turn classes for money current player and phase"
- **Co udělal Codex:** Připravil jednoduché mutable třídy `Player` a `Turn` s minimem polí pro peníze, hráče na tahu, číslo kola a fázi.
- **Co jsem upravil ručně:** Nechal jsem to schválně malé a bez další abstrakce. Stačilo mi `P1/P2`, peníze a jednoduché `Phase`, víc by v tu chvíli bylo zbytečné.
- **Ověření:** Kontrola `PlayerTest`, `TurnTest` a základního start stavu v `GameTurnTest`.
- **Podíl AI:** Tady šlo hlavně o boilerplate, ale nechal jsem jen minimum, které projekt opravdu potřeboval.

---

## 19. Tok tahu, příjem a opravy

- **Nástroj:** ChatGPT Codex
- **Datum:** 11. 5. 2026
- **Prompt:**
  > "help me add start end turn city income flag reset and repair at turn start"
- **Co udělal Codex:** Navrhl rozdělení logiky tahu na start, income, reset flagů a opravy na začátku tahu.
- **Co jsem upravil ručně:** Pořadí kroků jsem si hlídal sám. Tady jsem nechtěl převzít generický návrh bez úpravy, protože právě pořadí `income -> repair -> action` a reset jen pro aktivního hráče dělá nejvíc chyb.
- **Ověření:** Kontrola `GameTurnTest`, `GameRepairTest` a souvisejících případů v `BuildingServiceTest`.
- **Podíl AI:** Codex pomohl s rozsekáním logiky, ale pořadí kroků a chování mezi hráči jsem si musel ověřit ručně.

---

## 20. Nákup, dočištění a stabilizace

- **Nástroj:** ChatGPT Codex
- **Datum:** 11. 5. 2026
- **Prompt:**
  > "add purchase logic, write tests, then help me clean small redundancy and keep old tests working"
- **Co udělal Codex:** Pomohl s návrhem `purchaseUnit(...)`, se sadou testů pro nákup a pak ještě s hledáním menších redundancí a fixů po rozšíření projektu.
- **Co jsem upravil ručně:** Některé cleanup návrhy jsem nenechal, protože by zbytečně hýbaly se starším API. Ručně jsem doladil podmínky pro továrnu, peníze, obsazené pole a nakonec i pár minimálních fixů kvůli starým testům.
- **Ověření:** Kontrola `GamePurchaseTest`, `GamePlayerTest`, `AdditionalEdgeCasesTest` a starších veřejných testů z předchozí části projektu.
- **Podíl AI:** Na konci byl Codex užitečný hlavně jako druhý pár očí; finální rozhodnutí jsem dělal podle toho, co ještě nerozbíjelo staré testy.

---

Prompty xmarina00:

## 1. Grafické assety a balík spriteov

- **Nástroj:** ChatGPT Image Generation
- **Datum:** 14. 05. 2026 až 16. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Generate a complete sprite pack in The Escapists pixel-art style. Create transparent PNG files for terrain (`grass`, `forest`, `mountain`, `water`), buildings (`city`, `factory`, `HQ` in red/blue/neutral), and units (`soldier`, `tank`, `artillery` in red/blue). Keep one color style, one shadow style, same scale per tile, good readability in zoom, and use exactly the file names required by the loader."
- **Úprava studentem:**
Ruční vizuální kontrola v JavaFX boardu, přejmenování souborů dle loaderu, výměna nečitelných variant a finální doladění kontrastu.
- **Míra generování:** 60 % (AI kostra a návrh) / 40 % (student úpravy a integrace)

---

## 2. Vizuálna konzistentnosť variantov vlastníctva

- **Nástroj:** ChatGPT/Codex
- **Datum:** 16. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Improve sprite choice in the UI so ownership is clear at first look for both units and buildings, also on mixed-ownership maps."
- **Úprava studentem:**
Ruční kontrola na mapě s neutral městy.
- **Míra generování:** 50 % (AI návrh) / 50 % (student implementace a úpravy)

---

## 3. Maven projekt setup

- **Nástroj:** ChatGPT/Codex
- **Datum:** 16. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Set up Maven project configuration for Java 21 and add dependencies for JavaFX runtime, Gson, and JUnit Jupiter tests."
- **Úprava studentem:**
Ručně validováno přes `mvn test`.
- **Míra generování:** 80 % (AI návrh a implementace) / 20 % (student úpravy a integrace)

---

## 4. Maven pluginy a profily

- **Nástroj:** ChatGPT/Codex
- **Datum:** 16. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Configure Maven plugins (`compiler`, `surefire`, `javafx-maven-plugin`) and platform JavaFX profiles so tests and app startup work correctly on the target OS and architecture."
- **Úprava studentem:**
Ruční spuštění buildu a appky.
- **Míra generování:** 80 % (AI návrh a implementace) / 20 % (student úpravy a integrace)

---

## 5. Validácia metadát JSON mapy

- **Nástroj:** ChatGPT/Codex
- **Datum:** 16. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Add JSON metadata validation: map name must not be empty, width and height must be positive, and grid size must match metadata."
- **Úprava studentem:**
Otestováno na nevalidních JSON v testech.
- **Míra generování:** 40 % (AI podklad) / 60 % (student implementace a ladění)

---

## 6. Parser gridu a tokenov terénu

- **Nástroj:** ChatGPT/Codex
- **Datum:** 16. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Implement grid parser for terrain tokens (`P/F/M/W/C/T/H`) with strict rectangle checks and clear errors for bad rows or unknown tokens."
- **Úprava studentem:**
Ruční doladění textů chyb.
- **Míra generování:** 50 % (AI návrh) / 50 % (student implementace a úpravy)

---

## 7. Validácia budov v mape

- **Nástroj:** ChatGPT/Codex
- **Datum:** 16. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Validate building section so each building tile in grid has exactly one matching building entry, with no duplicates and only supported owners."
- **Úprava studentem:**
Ověřeno porovnáním expected/seen building pozic.
- **Míra generování:** 60 % (AI kostra a návrh) / 40 % (student úpravy a integrace)

---

## 8. Validácia jednotiek v mape

- **Nástroj:** ChatGPT/Codex
- **Datum:** 16. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Validate units in JSON: resolve unit type, check owner, check map bounds, allow only one unit per tile, and reject units on impossible terrain."
- **Úprava studentem:**
Ověřeno testy na duplicate/out-of-bounds.
- **Míra generování:** 80 % (AI návrh a implementace) / 20 % (student úpravy a integrace)

---

## 9. Načítanie runtime stavu hry

- **Nástroj:** ChatGPT/Codex
- **Datum:** 16. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Load runtime state from JSON (`currentPlayer`, `turnNumber`, `phase`, and players) and connect it to GameFactory initialization."
- **Úprava studentem:**
Ruční kontrola startovního stavu po načtení.
- **Míra generování:** 50 % (AI návrh) / 50 % (student implementace a úpravy)

---

## 10. Ukážková mapa scenario-alpha

- **Nástroj:** ChatGPT/Codex
- **Datum:** 16. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Create `scenario-alpha.json` with valid metadata, full building data, legal units, and runtime player state so the map is playable immediately."
- **Úprava studentem:**
Ruční balancing a kontrola načtení.
- **Míra generování:** 40 % (AI podklad) / 60 % (student implementace a ladění)

---

## 11. Ukážková mapa scenario-beta

- **Nástroj:** ChatGPT/Codex
- **Datum:** 16. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Create `scenario-beta.json` as a different playable map with all required terrain/buildings and valid ownership/runtime settings."
- **Úprava studentem:**
Ověřeno map-load testy a ručním spuštěním.
- **Míra generování:** 60 % (AI kostra a návrh) / 40 % (student úpravy a integrace)

---

## 12. Príjem peňazí na začiatku ťahu

- **Nástroj:** ChatGPT/Codex
- **Datum:** 16. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Implement income calculation per player from owned cities and apply this income at turn start/end-turn flow."
- **Úprava studentem:**
Ověřeno na scénářích s různým počtem měst.
- **Míra generování:** 50 % (AI návrh) / 50 % (student implementace a úpravy)

---

## 13. Kontroly nákupu v továrni

- **Nástroj:** ChatGPT/Codex
- **Datum:** 16. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Implement `canPurchaseUnit` checks: valid unit type, owned factory tile, free factory, correct phase, and enough money."
- **Úprava studentem:**
Ověřeno jednotkovými testy purchase flow.
- **Míra generování:** 60 % (AI kostra a návrh) / 40 % (student úpravy a integrace)

---

## 14. Vykonanie nákupu jednotky

- **Nástroj:** ChatGPT/Codex
- **Datum:** 16. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Implement `purchaseUnit`: subtract money, create unit on factory tile, mark it as already used this turn, and emit purchase event."
- **Úprava studentem:**
Ruční kontrola, že nová jednotka nejde znovu použít v tomtéž tahu.
- **Míra generování:** 80 % (AI návrh a implementace) / 20 % (student úpravy a integrace)

---

## 15. Testy ekonomiky a nákupov

- **Nástroj:** ChatGPT/Codex
- **Datum:** 16. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Add economy tests for successful purchase, blocked purchase on occupied factory, and failed purchase when funds are not enough."
- **Úprava studentem:**
Assertiony upraveny podle skutečné implementace.
- **Míra generování:** 80 % (AI návrh a implementace) / 20 % (student úpravy a integrace)

---

## 16. Výhra po obsadení HQ

- **Nástroj:** ChatGPT/Codex
- **Datum:** 16. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Implement HQ capture victory so when enemy HQ is captured, `gameOver` is set and `winnerPlayerId` is the capturing player."
- **Úprava studentem:**
Ověřeno přes test i v UI.
- **Míra generování:** 40 % (AI podklad) / 60 % (student implementace a ladění)

---

## 17. Blokovanie akcií po konci hry

- **Nástroj:** ChatGPT/Codex
- **Datum:** 16. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Protect gameplay APIs so move, attack, capture, purchase, and end turn are disabled after `gameOver = true`."
- **Úprava studentem:**
Ručně ověřeno po dosažení win stavu.
- **Míra generování:** 60 % (AI kostra a návrh) / 40 % (student úpravy a integrace)

---

## 18. Rozšírenie typov herných eventov

- **Nástroj:** ChatGPT/Codex
- **Datum:** 16. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Extend `GameEventType` with WAIT, CAPTURE, PURCHASE, INCOME, and END_TURN so observers can clearly detect each action type."
- **Úprava studentem:**
Ověřeno kompatibilitou s observer update flow.
- **Míra generování:** 50 % (AI návrh) / 50 % (student implementace a úpravy)

---

## 19. Factory metódy a emitovanie eventov

- **Nástroj:** ChatGPT/Codex
- **Datum:** 16. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Add matching factory methods in `GameEvent` and emit these events from engine actions in correct order."
- **Úprava studentem:**
Ručně zkontrolováno v UI logu.
- **Míra generování:** 40 % (AI podklad) / 60 % (student implementace a ladění)

---

## 20. Model snapshotu stavu hry

- **Nástroj:** ChatGPT/Codex
- **Datum:** 17. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Create immutable `GameState` records for tiles, units, player money, turn, and game-over data for replay save/load."
- **Úprava studentem:**
Ověřeno serializací/deserializací logu.
- **Míra generování:** 60 % (AI kostra a návrh) / 40 % (student úpravy a integrace)

---

## 21. Zber snapshotu runtime stavu

- **Nástroj:** ChatGPT/Codex
- **Datum:** 17. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Implement `Game.snapshotState` to capture full runtime state, including tile owner/capture points and unit turn flags, for deterministic replay restore."
- **Úprava studentem:**
Otestováno proti aktuálnímu runtime stavu.
- **Míra generování:** 50 % (AI návrh) / 50 % (student implementace a úpravy)

---

## 22. Obnova hry zo snapshotu

- **Nástroj:** ChatGPT/Codex
- **Datum:** 17. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Implement `Game.fromState` with validation and full restoration of map, units, players, turn state, and game-over fields for replay stepping."
- **Úprava studentem:**
Přidány testy konzistence snapshot -> restore.
- **Míra generování:** 80 % (AI návrh a implementace) / 20 % (student úpravy a integrace)

---

## 23. Abstrakcia UnitFactory

- **Nástroj:** ChatGPT/Codex
- **Datum:** 17. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Create `UnitFactory` interface and `DefaultUnitFactory` implementation to centralize unit creation by type, owner, and position."
- **Úprava studentem:**
Ruční kontrola API jednoduchosti.
- **Míra generování:** 60 % (AI kostra a návrh) / 40 % (student úpravy a integrace)

---

## 24. Pridanie UnitFactory do Game

- **Nástroj:** ChatGPT/Codex
- **Datum:** 17. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Inject `UnitFactory` into `Game`, route all unit creation through it, and throw clear error when factory returns null."
- **Úprava studentem:**
Ověřeno testem delegace a null-guardem.
- **Míra generování:** 50 % (AI návrh) / 50 % (student implementace a úpravy)

---

## 25. Načítanie a cache sprite knižnice

- **Nástroj:** ChatGPT/Codex
- **Datum:** 17. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Implement `SpriteLibrary` with lazy loading cache and deterministic file resolution for owner-specific and neutral variants."
- **Úprava studentem:**
Ruční test, že obrázky se načítají bez opakovaného I/O.
- **Míra generování:** 40 % (AI podklad) / 60 % (student implementace a ladění)

---

## 26. Fallback pri chýbajúcich spriteoch

- **Nástroj:** ChatGPT/Codex
- **Datum:** 17. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Add safe fallback for missing sprites: return null and let renderer show color rectangles instead of crashing UI."
- **Úprava studentem:**
Ověřeno dočasným odebráním vybraných sprite souborů.
- **Míra generování:** 60 % (AI kostra a návrh) / 40 % (student úpravy a integrace)

---

## 27. Command pre útok

- **Nástroj:** ChatGPT/Codex
- **Datum:** 17. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Implement `AttackCommand` that wraps game attack execution, returns combat payload in command result, and is stored in action history."
- **Úprava studentem:**
Ověřeno napojením na UI action flow.
- **Míra generování:** 50 % (AI návrh) / 50 % (student implementace a úpravy)

---

## 28. Command pre obsadenie

- **Nástroj:** ChatGPT/Codex
- **Datum:** 17. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Implement `CaptureCommand` that performs capture and, when needed, consumes unit action by applying wait in same command flow."
- **Úprava studentem:**
Ověřeno testem acted flag po capture.
- **Míra generování:** 40 % (AI podklad) / 60 % (student implementace a ladění)

---

## 29. Command vrstva a história undo/redo

- **Nástroj:** ChatGPT/Codex
- **Datum:** 17. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Implement full command layer (`Move`, `Wait`, `Purchase`, `EndTurn`) and `GameCommandHistory` with undo/redo snapshots and redo branch cut after new divergent action."
- **Úprava studentem:**
Ověřeno testy na undo/redo a no-op commands.
- **Míra generování:** 60 % (AI kostra a návrh) / 40 % (student úpravy a integrace)

---

## 30. Dummy Bot ťah

- **Nástroj:** ChatGPT/Codex
- **Datum:** 17. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Implement `Bot` abstraction and `DummyBot` so one bot turn uses only engine APIs: purchase, legal move, attack/capture/wait, then end turn."
- **Úprava studentem:**
Ověřeno jednotkovými testy a běháním bot tahu.
- **Míra generování:** 80 % (AI návrh a implementace) / 20 % (student úpravy a integrace)

---

## 31. Napojenie botov a commandov do UI

- **Nástroj:** ChatGPT/Codex
- **Datum:** 17. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Connect bot and command features to JavaFX controls (`Bot Turn`, `Bot vs Bot`, command actions) and verify with replay logging and automated tests."
- **Úprava studentem:**
Ruční test v aplikaci + `mvn test`.
- **Míra generování:** 80 % (AI návrh a implementace) / 20 % (student úpravy a integrace)

---

## 32. Priebeh ťahu, opravy a pravidlá pohybu v Game.java

- **Nástroj:** ChatGPT/Codex
- **Datum:** 17. 05. 2026
- **Prompt (nebo způsob použití):**
  > "In `Game.java`, update turn flow: `endTurn` must switch player, increase turn number after P2, reset unit flags, apply city income, and auto-repair healable owned units with money cost. Also enforce `gameOver` checks, allow move-by-zero, allow friendly pass-through only for path search (not destination), and block second move in same turn."
- **Úprava studentem:**
Doladění guard podmínek a ověření chování přes ruční hraní edge případů po každé změně.
- **Míra generování:** 60 % (AI kostra a návrh) / 40 % (student úpravy a integrace)

---

## 33. Kompletná JavaFX aplikácia v GameApp.java

- **Nástroj:** ChatGPT/Codex
- **Datum:** 17. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Build `GameApp.java` as full JavaFX playable UI: board rendering with sprites, unit selection + reachable highlight, action menus (Move/Attack/Capture/Wait/Purchase), replay turn stepping with return to live mode, bot controls, scenario switching, and collapsible right logger panel with readable colors and responsive board sizing."
- **Úprava studentem:**
Ruční UX úpravy tlačítek, stavových hlášek a interakčních detailů menu podle reálného testování v aplikaci.
- **Míra generování:** 50 % (AI návrh) / 50 % (student implementace a úpravy)

---

## 34. Testy AdditionalEdgeCasesTest.java

- **Nástroj:** ChatGPT/Codex
- **Datum:** 17. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Extend `AdditionalEdgeCasesTest.java` with regression tests for current engine changes: runtime players/turn JSON load, invalid runtime phase rejection, friendly pass-through pathfinding with blocked occupied destination, legal move-by-zero with observer event, and rejection of second move in one turn."
- **Úprava studentem:**
Upravené assertiony a názvy testů podle finálního chování implementace a čitelnosti reportu.
- **Míra generování:** 80 % (AI návrh a implementace) / 20 % (student úpravy a integrace)

---

## 35. Validácia scenárov v ScenarioMapsTest.java

- **Nástroj:** ChatGPT/Codex
- **Datum:** 17. 05. 2026
- **Prompt (nebo způsob použití):**
  > "Add `ScenarioMapsTest.java` to validate bundled maps (`scenario-alpha.json`, `scenario-beta.json`): each map must load, contain all terrain types, contain at least one city, and contain HQ for both P1 and P2 as required by assignment minimums."
- **Úprava studentem:**
Ruční kontrola podmínek proti obsahu map a doladění testu na minimální požadavky zadání.
- **Míra generování:** 80 % (AI návrh a implementace) / 20 % (student úpravy a integrace)

