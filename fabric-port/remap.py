#!/usr/bin/env python3
"""Replayable NeoForge -> Fabric/shim import remapper for the Mekanism Fabric port.

Reads mappings/neoforge-to-fabric.tsv and rewrites Java sources in the configured
source sets. Designed to be idempotent and re-run after every upstream merge.

Usage:
    python fabric-port/remap.py           # apply
    python fabric-port/remap.py --check   # dry run, report what would change
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
MAPPING_FILE = Path(__file__).resolve().parent / "mappings" / "neoforge-to-fabric.tsv"
# Source sets the transform applies to. datagen/gameTest stay NeoForge-side for now.
SOURCE_ROOTS = [
    REPO_ROOT / "src" / "api" / "java",
    REPO_ROOT / "src" / "main" / "java",
]


def load_mappings() -> list[tuple[str, str, str]]:
    mappings = []
    for lineno, line in enumerate(MAPPING_FILE.read_text(encoding="utf-8").splitlines(), 1):
        line = line.rstrip()
        if not line or line.lstrip().startswith("#"):
            continue
        parts = line.split("\t")
        if len(parts) < 2:
            sys.exit(f"{MAPPING_FILE}:{lineno}: malformed line (need at least 2 tab-separated columns): {line!r}")
        old, new = parts[0].strip(), parts[1].strip()
        mode = parts[2].strip() if len(parts) > 2 else "import"
        if old.rsplit(".", 1)[-1] != new.rsplit(".", 1)[-1] and mode != "fq":
            sys.exit(
                f"{MAPPING_FILE}:{lineno}: simple names differ ({old} -> {new}) but mode is not 'fq'. "
                "Shims must keep NeoForge simple names (see README)."
            )
        mappings.append((old, new, mode))
    return mappings


def apply_to_source(text: str, mappings: list[tuple[str, str, str]]) -> str:
    for old, new, mode in mappings:
        # import lines (incl. static imports of members: `import old.Cls.MEMBER;` handled by prefix match)
        text = re.sub(
            rf"^(import\s+(?:static\s+)?){re.escape(old)}(?=[;.])",
            rf"\g<1>{new}",
            text,
            flags=re.MULTILINE,
        )
        # fully-qualified references outside import lines
        text = re.sub(
            rf"(?<![\w.]){re.escape(old)}(?![\w])",
            new,
            text,
        )
        if mode == "fq":
            old_simple = old.rsplit(".", 1)[-1]
            new_simple = new.rsplit(".", 1)[-1]
            text = re.sub(rf"(?<![\w.]){re.escape(old_simple)}(?![\w])", new_simple, text)
    return text


def main() -> None:
    check_only = "--check" in sys.argv
    mappings = load_mappings()
    changed = 0
    scanned = 0
    for root in SOURCE_ROOTS:
        if not root.is_dir():
            print(f"warning: source root missing, skipping: {root}")
            continue
        for path in sorted(root.rglob("*.java")):
            scanned += 1
            original = path.read_text(encoding="utf-8")
            updated = apply_to_source(original, mappings)
            if updated != original:
                changed += 1
                if check_only:
                    print(f"would rewrite: {path.relative_to(REPO_ROOT)}")
                else:
                    path.write_text(updated, encoding="utf-8", newline="")
    verb = "would rewrite" if check_only else "rewrote"
    print(f"{verb} {changed} of {scanned} files using {len(mappings)} mappings")


if __name__ == "__main__":
    main()
