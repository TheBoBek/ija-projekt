IJA 2025/26 Projekt - Advance Wars (JavaFX)

Vedouci tymu: xmarina00
Clenove tymu:
- xmarina00
- xbobkos00

Popis projektu:
Tahova strategicka hra inspirovana Advance Wars. Projekt obsahuje herni engine,
JavaFX GUI, nacitani map ze souboru, ekonomiku (prijmy a nakupy), boj,
dobyvani budov, replay log a jednoduchy Dummy Bot + Bot vs Bot rezim.

Adresarova struktura:
- src/      zdrojove kody (main + test)
- data/     pripravene vstupni mapy (scenario-alpha, scenario-beta)
- lib/      externi soubory (graficke assety)
- pom.xml   Maven build konfigurace
- ai_audit.md
- git_history.txt

Preklad a testy:
1) mvn clean test

Kompletni build (kompilace + Javadoc + JAR):
1) mvn clean package

Po buildu vzniknou artefakty v target/:
- advance-wars-ija-0.1.0-SNAPSHOT.jar
- advance-wars-ija-0.1.0-SNAPSHOT-all.jar
- reports/apidocs/index.html

Spusteni aplikace:
Varianta A (doporucena pro vyvoj):
1) mvn -q -DskipTests javafx:run

Varianta B (zabaleny JAR):
1) java -jar target/advance-wars-ija-0.1.0-SNAPSHOT-all.jar

Poznamky:
- Projekt cilí na Java SE 21.
- GUI je implementovane v JavaFX.
