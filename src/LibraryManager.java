import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * shae-java-library-manager
 * -------------------------
 * Command-line library management system written in Java.
 *
 * Features:
 *   - add a book (title, author, ISBN)
 *   - list all books (table format)
 *   - search by title or author
 *   - check out / check in a book
 *   - save / load to a text file (or JSON with --json)
 *
 * Build:  javac src/LibraryManager.java
 * Run:    java LibraryManager <command> [args]
 *
 * Design system (SHAE Field Manual):
 *   Console output uses the paper/ink/oxide palette via ANSI escapes:
 *     warm paper #F2EEE5, ink #191712, oxide signal #B3341B.
 *   Tables are hairline-ruled, left-aligned, monospace metadata;
 *   no gradients, glass, pills, or shadows in the UI layer.
 */
public class LibraryManager {

    // ANSI colour map derived from the SHAE design system palette.
    private static final String ANSI_RESET   = "\u001B[0m";
    private static final String ANSI_INK     = "\u001B[38;2;25;23;18m";   // #191712
    private static final String ANSI_OXIDE   = "\u001B[38;2;179;52;27m";  // #B3341B
    private static final String ANSI_PAPER   = "\u001B[38;2;242;238;229m";// #F2EEE5
    private static final String ANSI_BOLD    = "\u001B[1m";

    private static final String DEFAULT_TEXT_FILE = "library.data";
    private static final String DEFAULT_JSON_FILE = "library.json";

    public static class Book {
        public final String title;
        public final String author;
        public final String isbn;
        public boolean checkedOut;
        public String checkedOutBy;

        public Book(String title, String author, String isbn) {
            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException("title is required");
            }
            if (author == null || author.isBlank()) {
                throw new IllegalArgumentException("author is required");
            }
            if (isbn == null || isbn.isBlank()) {
                throw new IllegalArgumentException("isbn is required");
            }
            this.title = title;
            this.author = author;
            this.isbn = isbn;
            this.checkedOut = false;
            this.checkedOutBy = null;
        }

        public String status() {
            return checkedOut ? "checked out (" + checkedOutBy + ")" : "available";
        }
    }

    private final List<Book> books = new ArrayList<>();

    /* ---------------------------------------------------------------- */
    // Core operations
    /* ---------------------------------------------------------------- */

    public void addBook(String title, String author, String isbn) {
        if (findByIsbn(isbn) != null) {
            throw new IllegalArgumentException("A book with ISBN " + isbn + " already exists.");
        }
        books.add(new Book(title, author, isbn));
    }

    public Book findByIsbn(String isbn) {
        return books.stream()
                .filter(b -> b.isbn.equalsIgnoreCase(isbn))
                .findFirst()
                .orElse(null);
    }

    public List<Book> listBooks() {
        return new ArrayList<>(books);
    }

    public List<Book> searchByTitle(String query) {
        String q = query.toLowerCase();
        return books.stream()
                .filter(b -> b.title.toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public List<Book> searchByAuthor(String query) {
        String q = query.toLowerCase();
        return books.stream()
                .filter(b -> b.author.toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public void checkOut(String isbn, String borrower) {
        Book book = findByIsbn(isbn);
        if (book == null) {
            throw new IllegalArgumentException("No book with ISBN " + isbn + ".");
        }
        if (book.checkedOut) {
            throw new IllegalStateException(
                    "Book is already checked out by " + book.checkedOutBy + ".");
        }
        book.checkedOut = true;
        book.checkedOutBy = borrower;
    }

    public void checkIn(String isbn) {
        Book book = findByIsbn(isbn);
        if (book == null) {
            throw new IllegalArgumentException("No book with ISBN " + isbn + ".");
        }
        if (!book.checkedOut) {
            throw new IllegalStateException("Book is not currently checked out.");
        }
        book.checkedOut = false;
        book.checkedOutBy = null;
    }

    /* ---------------------------------------------------------------- */
    // Persistence (text + JSON)
    /* ---------------------------------------------------------------- */

    private static String escapeJson(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder out = new StringBuilder();
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default   -> out.append(c);
            }
        }
        out.append('"');
        return out.toString();
    }

    public void saveToFile(Path path, boolean asJson) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            if (asJson) {
                writer.write("[\n");
                for (int i = 0; i < books.size(); i++) {
                    Book b = books.get(i);
                    writer.write("  {\n");
                    writer.write("    \"title\": " + escapeJson(b.title) + ",\n");
                    writer.write("    \"author\": " + escapeJson(b.author) + ",\n");
                    writer.write("    \"isbn\": " + escapeJson(b.isbn) + ",\n");
                    writer.write("    \"checkedOut\": " + b.checkedOut + ",\n");
                    writer.write("    \"checkedOutBy\": " + escapeJson(b.checkedOutBy) + "\n");
                    writer.write("  }" + (i < books.size() - 1 ? "," : "") + "\n");
                }
                writer.write("]\n");
            } else {
                for (Book b : books) {
                    writer.write(b.title + "|" + b.author + "|" + b.isbn + "|"
                            + b.checkedOut + "|" + (b.checkedOutBy == null ? "" : b.checkedOutBy) + "\n");
                }
            }
        }
    }

    public void loadFromFile(Path path, boolean asJson) throws IOException {
        if (!Files.isReadable(path)) {
            throw new IOException("Cannot read file: " + path);
        }
        books.clear();
        if (asJson) {
            loadFromJson(path);
        } else {
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) {
                        continue;
                    }
                    String[] parts = line.split("\\|", -1);
                    if (parts.length < 4) {
                        continue;
                    }
                    Book book = new Book(parts[0], parts[1], parts[2]);
                    book.checkedOut = Boolean.parseBoolean(parts[3]);
                    if (parts.length > 4 && !parts[4].isBlank()) {
                        book.checkedOutBy = parts[4];
                    }
                    books.add(book);
                }
            }
        }
    }

    private void loadFromJson(Path path) throws IOException {
        String content = Files.readString(path, StandardCharsets.UTF_8).trim();
        if (content.isEmpty() || !content.startsWith("[")) {
            throw new IOException("Invalid JSON library file: " + path);
        }
        // Minimal hand-rolled JSON array parser (no external dependency).
        int i = 1;
        while (i < content.length()) {
            char c = content.charAt(i);
            if (c == ']' || Character.isWhitespace(c) || c == ',') {
                i++;
                continue;
            }
            if (c == '{') {
                Book b = readJsonObject(content, i);
                books.add(b);
                i = skipToEnd(content, i);
            } else {
                i++;
            }
        }
    }

    private Book readJsonObject(String content, int start) {
        String title = null, author = null, isbn = null;
        boolean checkedOut = false;
        String checkedOutBy = null;
        int i = start + 1;
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        boolean inKey = true;
        boolean inString = false;
        while (i < content.length()) {
            char c = content.charAt(i);
            if (inString) {
                if (c == '\\') { i += 2; continue; }
                if (c == '"') { inString = false; }
                value.append(c);
                i++;
                continue;
            }
            if (c == '"') {
                inString = true;
                i++;
                continue;
            }
            if (c == ':') { inKey = false; i++; continue; }
            if (c == ',') {
                assignField(key.toString().trim(), value.toString().trim(), title == null ? null : title,
                        author == null ? null : author, isbn == null ? null : isbn,
                        checkedOut, checkedOutBy);
                // reassign via helper below is awkward; use direct mapping instead
                key.setLength(0);
                value.setLength(0);
                inKey = true;
                i++;
                continue;
            }
            if (c == '}') {
                assignFieldDirect(key.toString().trim(), value.toString().trim(),
                        new Object[]{title, author, isbn, checkedOut, checkedOutBy});
                break;
            }
            if (inKey) {
                key.append(c);
            } else {
                value.append(c);
            }
            i++;
        }
        // Re-parse cleanly using a second pass for correctness.
        return parseBookObject(content, start);
    }

    // Clean JSON object parser.
    private Book parseBookObject(String content, int start) {
        String title = null, author = null, isbn = null, checkedOutBy = null;
        boolean checkedOut = false;
        int i = start + 1;
        while (i < content.length()) {
            char c = content.charAt(i);
            if (c == '}' || c == ',' || Character.isWhitespace(c)) { i++; continue; }
            if (c == '"') {
                String key = readJsonString(content, i);
                i += key.length() + 2; // skip quotes + key
                // skip colon and whitespace
                while (i < content.length() && (content.charAt(i) == ':' || Character.isWhitespace(content.charAt(i)))) {
                    i++;
                }
                String val = readJsonString(content, i);
                switch (key) {
                    case "title"       -> title = val;
                    case "author"      -> author = val;
                    case "isbn"        -> isbn = val;
                    case "checkedOut"  -> checkedOut = Boolean.parseBoolean(val);
                    case "checkedOutBy" -> checkedOutBy = (val == null || val.equals("null")) ? null : val;
                }
                i += val.length() + 2;
            } else {
                i++;
            }
        }
        Book book = new Book(title, author, isbn);
        book.checkedOut = checkedOut;
        book.checkedOutBy = checkedOutBy;
        return book;
    }

    private String readJsonString(String content, int start) {
        // start points at the opening quote.
        int i = start + 1;
        StringBuilder out = new StringBuilder();
        while (i < content.length()) {
            char c = content.charAt(i);
            if (c == '\\') {
                i++;
                if (i < content.length()) {
                    char e = content.charAt(i);
                    switch (e) {
                        case '"'  -> out.append('"');
                        case '\\' -> out.append('\\');
                        case '/'  -> out.append('/');
                        case 'b'  -> out.append('\b');
                        case 'f'  -> out.append('\f');
                        case 'n'  -> out.append('\n');
                        case 'r'  -> out.append('\r');
                        case 't'  -> out.append('\t');
                        default   -> out.append(e);
                    }
                }
                i++;
                continue;
            }
            if (c == '"') {
                return out.toString();
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }

    private int skipToEnd(String content, int start) {
        int depth = 1;
        int i = start;
        boolean inString = false;
        while (i < content.length() && depth > 0) {
            char c = content.charAt(i);
            if (inString) {
                if (c == '\\') { i += 2; continue; }
                if (c == '"') inString = false;
            } else {
                if (c == '"') inString = true;
                else if (c == '{') depth++;
                else if (c == '}') depth--;
            }
            i++;
        }
        return i;
    }

    /* ---------------------------------------------------------------- */
    // Rendering
    /* ---------------------------------------------------------------- */

    private static void print(String s) {
        System.out.print(s);
    }

    private static void println(String s) {
        System.out.println(s);
    }

    public void renderTable(List<Book> rows) {
        println(ANSI_BOLD + ANSI_INK + "Library catalogue" + ANSI_RESET);
        println(ANSI_PAPER + "--------------------------------------------------------------------------------" + ANSI_RESET);
        println(ANSI_INK + String.format(" %-38s | %-24s | %-14s | %-12s",
                "Title", "Author", "ISBN", "Status") + ANSI_RESET);
        println(ANSI_PAPER + "--------------------------------------------------------------------------------" + ANSI_RESET);
        if (rows.isEmpty()) {
            println(ANSI_OXIDE + "  No books found." + ANSI_RESET);
        } else {
            for (Book b : rows) {
                String status = b.checkedOut
                        ? ANSI_OXIDE + "checked out" + ANSI_RESET
                        : ANSI_INK + "available" + ANSI_RESET;
                if (b.checkedOut && b.checkedOutBy != null) {
                    status += " by " + b.checkedOutBy;
                }
                println(String.format(" %-38s | %-24s | %-14s | %-12s %s",
                        truncate(b.title, 38), truncate(b.author, 24),
                        truncate(b.isbn, 14), status, ANSI_RESET));
            }
        }
        println(ANSI_PAPER + "--------------------------------------------------------------------------------" + ANSI_RESET);
    }

    private static String truncate(String s, int n) {
        if (s == null) return "";
        return s.length() > n ? s.substring(0, n - 1) + "…" : s;
    }

    /* ---------------------------------------------------------------- */
    // CLI entry point
    /* ---------------------------------------------------------------- */

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        String command = args[0];
        LibraryManager mgr = new LibraryManager();

        try {
            switch (command) {
                case "add" -> {
                    if (args.length < 4) {
                        throw new IllegalArgumentException("Usage: add <title> <author> <isbn>");
                    }
                    mgr.addBook(args[1], args[2], args[3]);
                    println(ANSI_OXIDE + "Added book." + ANSI_RESET);
                }
                case "list" -> mgr.renderTable(mgr.listBooks());
                case "search-title" -> {
                    if (args.length < 2) {
                        throw new IllegalArgumentException("Usage: search-title <query>");
                    }
                    mgr.renderTable(mgr.searchByTitle(args[1]));
                }
                case "search-author" -> {
                    if (args.length < 2) {
                        throw new IllegalArgumentException("Usage: search-author <query>");
                    }
                    mgr.renderTable(mgr.searchByAuthor(args[1]));
                }
                case "checkout" -> {
                    if (args.length < 3) {
                        throw new IllegalArgumentException("Usage: checkout <isbn> <borrower>");
                    }
                    mgr.checkOut(args[1], args[2]);
                    println(ANSI_OXIDE + "Checked out " + args[1] + " to " + args[2] + "." + ANSI_RESET);
                }
                case "checkin" -> {
                    if (args.length < 2) {
                        throw new IllegalArgumentException("Usage: checkin <isbn>");
                    }
                    mgr.checkIn(args[1]);
                    println(ANSI_OXIDE + "Checked in " + args[1] + "." + ANSI_RESET);
                }
                case "save" -> {
                    String file = args.length > 1 ? args[1] : DEFAULT_TEXT_FILE;
                    boolean asJson = file.endsWith(".json");
                    mgr.saveToFile(Paths.get(file), asJson);
                    println(ANSI_OXIDE + "Saved " + mgr.listBooks().size() + " book(s) to " + file + "." + ANSI_RESET);
                }
                case "load" -> {
                    if (args.length < 2) {
                        throw new IllegalArgumentException("Usage: load <file>");
                    }
                    boolean asJson = args[1].endsWith(".json");
                    mgr.loadFromFile(Paths.get(args[1]), asJson);
                    println(ANSI_OXIDE + "Loaded " + mgr.listBooks().size() + " book(s) from " + args[1] + "." + ANSI_RESET);
                }
                default -> {
                    println(ANSI_OXIDE + "Unknown command: " + command + ANSI_RESET);
                    printUsage();
                }
            }
        } catch (Exception e) {
            println(ANSI_OXIDE + "Error: " + e.getMessage() + ANSI_RESET);
            System.exit(1);
        }
    }

    private static void printUsage() {
        println(ANSI_BOLD + ANSI_INK + "shae-java-library-manager" + ANSI_RESET);
        println(ANSI_PAPER + "----------------------------------------" + ANSI_RESET);
        println("  add <title> <author> <isbn>      add a book");
        println("  list                             list all books (table)");
        println("  search-title <query>             search by title");
        println("  search-author <query>            search by author");
        println("  checkout <isbn> <borrower>       check out a book");
        println("  checkin <isbn>                   check in a book");
        println("  save [file]                      save to text (or .json)");
        println("  load <file>                      load from text (.json)");
        println();
        println("Design system: paper #F2EEE5, ink #191712, oxide #B3341B");
    }
}
