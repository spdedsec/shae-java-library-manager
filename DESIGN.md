# DESIGN.md — shae-java-library-manager

SHAE Field Manual design system applied to this CLI tool.

## Palette

| Role | Hex | Usage |
|------|-----|-------|
| Warm paper | #F2EEE5 | Table rules, negative space, header bands |
| Ink | #191712 | Primary text, titles, labels |
| Oxide signal | #B3341B | One signal per view: status text, errors, actions |

Console output emits these colours via ANSI escapes (`ANSI_INK`,
`ANSI_OXIDE`, `ANSI_PAPER`) so the terminal renders the SHAE palette
directly.

## Typography

- Body / labels: Instrument Sans (documented intent; terminal falls back
  to the system monospace stack)
- Code / metadata: JetBrains Mono (tabular numerals for aligned ISBN /
  status columns)

## Layout & form

- 0px radius — zero rounded corners anywhere
- 1px borders — hairline rules (`----`) separate sections
- Border-defined cards — the catalogue block is bordered by rules, not
  by shadows or glass
- Left-aligned data — every column is left-aligned; no centered heroes
- Uppercase only for short labels — command names stay lowercase

## Motion

This is a CLI; there is no animated UI. Any future TUI layer must use
140–240ms state changes, transform/opacity/clip-path only, with
`prefers-reduced-motion` and `prefers-reduced-transparency` fallbacks.
No animation libraries for simple output.

## Accessibility

- WCAG AA contrast between ink and paper (oxide used only as a signal,
  never as sole meaning)
- Visible oxide focus rings (in any future interactive/TUI layer)
- Complete keyboard paths — every command is keyboard-driven
- 44px touch targets — N/A for CLI; applies to any future GUI/TUI
- No horizontal page overflow — columns are truncated to fixed widths

## Anti-patterns avoided

No gradients, glows, pills, glass cards, shadows, or centered heroes.

## Files

- `src/LibraryManager.java` — source, ANSI-coloured console UI
- `README.md` — install + usage (design-referenced)
- `scripts/install.sh`, `Makefile` — build/test/release
- `docs/DE.md`, `docs/ES.md` — German / Spanish translations
