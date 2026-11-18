# Makefile — shae-java-library-manager
#
# Targets: build test run install clean release help
#
# SHAE design system applied: output uses the paper / ink / oxide palette
# (#F2EEE5 / #191712 / #B3341B) via ANSI escapes in the Java source.
#
# NOTE: javac/java (JDK) are required and are NOT available locally.
# Install a JDK before building.

.PHONY: build test run install clean release help

JAVA       ?= javac
JAVA_RUN   ?= java
SRC        := src/LibraryManager.java
CLASS_DIR  := build/classes
DIST       := dist
VERSION    := v0.1.0
PKG_NAME   := shae-java-library-manager

build:
	@echo "$(shell tput setaf 1 2>/dev/null || true)==> Building $(PKG_NAME)$(shell tput sgr0 2>/dev/null || true)"
	mkdir -p $(CLASS_DIR)
	$(JAVA) -d $(CLASS_DIR) $(SRC)
	@echo "Build complete: $(CLASS_DIR)"

test: build
	@echo "==> No automated test suite (compile-checked only)."
	$(JAVA_RUN) -cp $(CLASS_DIR) LibraryManager

run: build
	$(JAVA_RUN) -cp $(CLASS_DIR) LibraryManager $(ARGS)

install: build
	@echo "==> Install $(PKG_NAME)"
	@echo "Add build/classes to your classpath, or copy LibraryManager.class to a classpath dir."
	@echo "Example: java -cp build/classes LibraryManager list"

clean:
	rm -rf $(CLASS_DIR) $(DIST)

release: clean build
	@echo "==> Preparing release $(VERSION)"
	mkdir -p $(DIST)
	tar czf $(DIST)/$(PKG_NAME)-$(VERSION)-src.tar.gz -C .. --exclude='.git' $(PKG_NAME)
	@echo "Artifact: $(DIST)/$(PKG_NAME)-$(VERSION)-src.tar.gz"
	@echo "Create the GitHub release: gh release create $(VERSION) $(DIST)/*.tar.gz --title '$(PKG_NAME) $(VERSION)'"

help:
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@echo "  build    compile the Java sources"
	@echo "  test     compile and run the CLI help"
	@echo "  run      run the CLI (pass args via ARGS=\"...\")"
	@echo "  install  usage instructions"
	@echo "  clean    remove build/dist output"
	@echo "  release  build a source tarball for GitHub release"
	@echo "  help     show this help"
