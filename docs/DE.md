# shae-java-library-manager (Deutsch)

Befehlszeilen-Bibliotheksverwaltung in Java, nach dem SHAE Field Manual
Design System: warmes Papier #F2EEE5, Tinte #191712, ein Oxid-Rot #B3341B
als einziges Signal pro Ansicht, 0px Radius, 1px Rahmen, Monospace-
Metadaten, WCAG-AA-Kontrast, reduzierte-Bewegung-Fallbacks.

## Funktionen

- Buch hinzufügen (Titel, Autor, ISBN)
- Alle Bücher auflisten (haardünne, linksbündige Tabelle)
- Suche nach Titel oder Autor
- Buch ausleihen / zurückgeben
- Speichern / Laden in einer Textdatei (oder JSON mit Endung `.json`)

## Installation

Es wird ein JDK (`javac` + `java`) benötigt — nicht lokal verfügbar.

```bash
sudo apt install default-jdk   # Beispiel: Debian/Ubuntu

git clone https://github.com/spdedsec/shae-java-library-manager.git
cd shae-java-library-manager
make build
```

## Verwendung

```bash
java -cp build/classes LibraryManager add "Das Handbuch" "Shae Studio" "ISBN-001"
java -cp build/classes LibraryManager list
java -cp build/classes LibraryManager search-title "Handbuch"
java -cp build/classes LibraryManager checkout ISBN-001 "Leser Eins"
java -cp build/classes LibraryManager checkin ISBN-001
java -cp build/classes LibraryManager save bibliothek.json
java -cp build/classes LibraryManager load bibliothek.json
```

## Design-System

Siehe `DESIGN.md`. Die Konsolenausgabe nutzt die SHAE-Palette über ANSI-
Escape-Sequenzen.

## Lizenz

MIT. Siehe `LICENSE`.
