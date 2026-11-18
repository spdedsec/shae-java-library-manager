# shae-java-library-manager (Español)

Sistema de gestión de biblioteca de línea de comandos en Java, basado en
el SHAE Field Manual design system: papel cálido #F2EEE5, tinta #191712,
una señal óxido #B3341B por vista, 0px de radio, bordes de 1px,
metadatos monoespaciados, contraste WCAG AA, fallback de movimiento
reducido.

## Funcionalidades

- Añadir un libro (título, autor, ISBN)
- Listar todos los libros (tabla alineada a la izquierda, reglas finas)
- Buscar por título o autor
- Prestar / devolver un libro
- Guardar / cargar en archivo de texto (o JSON con extensión `.json`)

## Instalación

Se requiere un JDK (`javac` + `java`) — no disponible localmente.

```bash
sudo apt install default-jdk   # ejemplo: Debian/Ubuntu

git clone https://github.com/spdedsec/shae-java-library-manager.git
cd shae-java-library-manager
make build
```

## Uso

```bash
java -cp build/classes LibraryManager add "El Manual" "Shae Studio" "ISBN-001"
java -cp build/classes LibraryManager list
java -cp build/classes LibraryManager search-title "Manual"
java -cp build/classes LibraryManager checkout ISBN-001 "Lector Uno"
java -cp build/classes LibraryManager checkin ISBN-001
java -cp build/classes LibraryManager save biblioteca.json
java -cp build/classes LibraryManager load biblioteca.json
```

## Sistema de diseño

Véase `DESIGN.md`. La salida por consola usa la paleta SHAE mediante
secuencias de escape ANSI.

## Licencia

MIT. Véase `LICENSE`.
