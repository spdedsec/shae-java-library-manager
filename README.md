# shae-java-library-manager

Command-line library management system written in Java, built on the
SHAE Field Manual design system: warm paper #F2EEE5, ink #191712, one
oxide-red #B3341B signal per view, 0px radius, 1px borders, monospace
metadata, WCAG AA contrast, reduced-motion fallbacks.

## Features

- add a book (title, author, ISBN)
- list all books (hairline-ruled, left-aligned table)
- search by title or author
- check out / check in a book
- save / load to a text file (or JSON with `.json` extension)

## Install

```bash
# Requires a JDK (javac + java) — not available locally.
sudo apt install default-jdk   # Debian/Ubuntu example

git clone https://github.com/spdedsec/shae-java-library-manager.git
cd shae-java-library-manager

make build
```

Or run the installer:

```bash
./scripts/install.sh
```

## Usage

```bash
java -cp build/classes LibraryManager add "The Field Manual" "Shae Studio" "ISBN-001"
java -cp build/classes LibraryManager list
java -cp build/classes LibraryManager search-title "Field"
java -cp build/classes LibraryManager search-author "Shae"
java -cp build/classes LibraryManager checkout ISBN-001 "Reader One"
java -cp build/classes LibraryManager checkin ISBN-001
java -cp build/classes LibraryManager save library.json
java -cp build/classes LibraryManager load library.json
```

Commands:

| Command | Arguments | Description |
|---------|-----------|-------------|
| add | `<title> <author> <isbn>` | Add a book |
| list | — | List all books (table) |
| search-title | `<query>` | Search by title |
| search-author | `<query>` | Search by author |
| checkout | `<isbn> <borrower>` | Check out a book |
| checkin | `<isbn>` | Check in a book |
| save | `[file]` | Save to text or `.json` |
| load | `<file>` | Load from text or `.json` |

## Design system

See `DESIGN.md`. Console rendering applies the SHAE palette:

- Warm paper `#F2EEE5` — rules and negative space
- Ink `#191712` — primary text
- Oxide `#B3341B` — single signal colour per view (status, errors)

Tables use JetBrains Mono-style monospace metadata, left-aligned, with
1px hairline borders. No gradients, glass, pills, or shadows.

## Build / release

```bash
make build        # javac
make release      # source tarball for GitHub release v0.1.0
```

`javac`/`java` (JDK) are required and are not available locally.

## License

MIT. See `LICENSE`.
