#!/usr/bin/env python3
"""Convert NeoForge access transformers to a Fabric access widener.

Mekanism's ATs use Mojang-mapped names, matching Loom's officialMojangMappings(),
so class and method entries convert textually. FIELD entries are the catch: AT omits
the field descriptor but access-widener v2 requires one, so field lines need the
Minecraft class files to resolve descriptors.

Usage:
    python fabric-port/at2aw.py --check                 # parse + categorize, no output file
    python fabric-port/at2aw.py                          # write AW; fields needing descriptors -> TODO block
    python fabric-port/at2aw.py --mc-jar <path-to-client.jar>   # resolve field descriptors fully

The build wires this as the `generateAccessWidener` task (which always has the MC jar),
but it runs standalone here for review/testing.
"""
from __future__ import annotations

import argparse
import re
import sys
import zipfile
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
AT_FILES = [
    REPO_ROOT / "src" / "main" / "resources" / "META-INF" / "accesstransformer.cfg",
    # additions AT folded in when that module is enabled:
    # REPO_ROOT / "src" / "additions" / "resources" / "META-INF" / "accesstransformer.cfg",
]
# Lives in the fabric bootstrap resources for now; moves to src/main/resources when Phase 1
# activates the main source set (update loom.accessWidenerPath + fabric.mod.json alongside).
OUT_FILE = REPO_ROOT / "src" / "fabric" / "resources" / "mekanism.accesswidener"

ACCESS_KEYWORDS = {"public", "protected", "private", "default"}

# Upstream AT entries that target classes/members which no longer exist in vanilla 1.21.1
# (verified stale; nothing in Mekanism source references them). Keyed by (owner, member),
# member None for class-level entries.
KNOWN_STALE = {
    ("net.minecraft.world.item.ArmorItem", "ARMOR_MODIFIER_UUID_PER_TYPE"),
    # renamed to CopyCustomDataFunction in 1.20.5; unreferenced leftover
    ("net.minecraft.world.level.storage.loot.functions.CopyNbtFunction$Path", None),
}


class Entry:
    __slots__ = ("access", "definalize", "owner", "member", "desc", "kind", "raw")

    def __init__(self, access, definalize, owner, member, desc, kind, raw):
        self.access = access
        self.definalize = definalize
        self.owner = owner          # dotted, e.g. net.minecraft.client.Gui
        self.member = member        # name or None
        self.desc = desc            # descriptor or None
        self.kind = kind            # 'class' | 'method' | 'field'
        self.raw = raw


def strip_comment(line: str) -> str:
    # AT inline comments start with '#'; none of the tokens legitimately contain '#'
    return line.split("#", 1)[0].rstrip()


def parse_at(path: Path) -> list[Entry]:
    entries = []
    for lineno, raw in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        line = strip_comment(raw).strip()
        if not line:
            continue
        toks = line.split()
        stale_key = (toks[1], toks[2].split("(")[0]) if len(toks) > 2 else (toks[1], None)
        if len(toks) > 1 and stale_key in KNOWN_STALE:
            continue
        access = toks[0]
        definalize = access.endswith("-f")
        access_base = access[:-2] if definalize else access
        if access_base not in ACCESS_KEYWORDS:
            sys.exit(f"{path}:{lineno}: unrecognized access token {access!r}")
        owner = toks[1]
        if len(toks) == 2:
            entries.append(Entry(access_base, definalize, owner, None, None, "class", raw))
        else:
            member = toks[2]
            m = re.match(r"^([^(]+)(\(.*)$", member)
            if m:  # method: name + descriptor (constructor uses <init>)
                entries.append(Entry(access_base, definalize, owner, m.group(1), m.group(2), "method", raw))
            else:  # field: bare name, descriptor unknown from AT
                entries.append(Entry(access_base, definalize, owner, member, None, "field", raw))
    return entries


def internal(owner_dotted: str) -> str:
    return owner_dotted.replace(".", "/")


def resolve_field_descriptors(entries: list[Entry], mc_jar: Path) -> dict[tuple[str, str], str]:
    """Read field descriptors straight from the constant pool region of each class file.

    Minimal classfile field-table parser: enough to map (ownerInternal, fieldName) -> descriptor
    without a full bytecode library.
    """
    wanted: dict[str, set[str]] = {}
    for e in entries:
        if e.kind == "field":
            wanted.setdefault(internal(e.owner), set()).add(e.member)
    resolved: dict[tuple[str, str], str] = {}
    with zipfile.ZipFile(mc_jar) as jar:
        names = set(jar.namelist())
        for owner_internal, fields in wanted.items():
            entry_name = owner_internal + ".class"
            if entry_name not in names:
                continue
            data = jar.read(entry_name)
            for name, desc in _iter_class_fields(data):
                if name in fields:
                    resolved[(owner_internal, name)] = desc
    return resolved


def _iter_class_fields(data: bytes):
    """Yield (fieldName, descriptor) from a classfile via constant-pool + field_info walk."""
    import struct

    pos = 8  # magic + minor/major
    const_count = struct.unpack_from(">H", data, pos)[0]
    pos += 2
    consts: dict[int, tuple] = {}
    i = 1
    while i < const_count:
        tag = data[pos]; pos += 1
        if tag == 1:  # Utf8
            length = struct.unpack_from(">H", data, pos)[0]; pos += 2
            consts[i] = ("utf8", data[pos:pos + length].decode("utf-8", "replace")); pos += length
        elif tag in (7, 8, 16, 19, 20):  # Class, String, MethodType, Module, Package (u2)
            pos += 2
        elif tag in (15,):  # MethodHandle (u1+u2)
            pos += 3
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):  # Int/Float/refs/NameAndType/Dynamic (u4)
            pos += 4
        elif tag in (5, 6):  # Long/Double take two slots
            pos += 8; i += 1
        else:
            return  # unknown tag; bail safely
        i += 1
    # access_flags(2) this_class(2) super_class(2)
    pos += 6
    iface_count = struct.unpack_from(">H", data, pos)[0]; pos += 2 + iface_count * 2
    field_count = struct.unpack_from(">H", data, pos)[0]; pos += 2
    for _ in range(field_count):
        pos += 2  # access_flags
        name_idx = struct.unpack_from(">H", data, pos)[0]; pos += 2
        desc_idx = struct.unpack_from(">H", data, pos)[0]; pos += 2
        attr_count = struct.unpack_from(">H", data, pos)[0]; pos += 2
        for _a in range(attr_count):
            pos += 2  # attr name idx
            alen = struct.unpack_from(">I", data, pos)[0]; pos += 4 + alen
        name = consts.get(name_idx, ("", ""))[1]
        desc = consts.get(desc_idx, ("", ""))[1]
        yield name, desc


def to_aw_lines(entries: list[Entry], field_desc: dict[tuple[str, str], str]) -> tuple[list[str], list[str]]:
    lines, todo = [], []
    for e in entries:
        owner = internal(e.owner)
        if e.kind == "class":
            lines.append(f"accessible class {owner}")
            if e.definalize:
                lines.append(f"extendable class {owner}")
        elif e.kind == "method":
            lines.append(f"accessible method {owner} {e.member} {e.desc}")
            if e.definalize:
                lines.append(f"extendable method {owner} {e.member} {e.desc}")
        else:  # field
            desc = field_desc.get((owner, e.member))
            if desc is None:
                todo.append(f"# TODO(descriptor): accessible field {owner} {e.member} <desc>   (from: {e.raw.strip()})")
            else:
                lines.append(f"accessible field {owner} {e.member} {desc}")
                if e.definalize:
                    lines.append(f"mutable field {owner} {e.member} {desc}")
    return lines, todo


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true", help="parse and categorize only; do not write")
    ap.add_argument("--mc-jar", type=Path, help="Minecraft client jar to resolve field descriptors")
    args = ap.parse_args()

    entries: list[Entry] = []
    for at in AT_FILES:
        if at.is_file():
            entries.extend(parse_at(at))
        else:
            print(f"warning: AT file missing, skipping: {at}")

    counts = {"class": 0, "method": 0, "field": 0}
    for e in entries:
        counts[e.kind] += 1
    print(f"parsed {len(entries)} AT entries: "
          f"{counts['class']} class, {counts['method']} method, {counts['field']} field")

    field_desc: dict[tuple[str, str], str] = {}
    if args.mc_jar:
        if not args.mc_jar.is_file():
            sys.exit(f"--mc-jar not found: {args.mc_jar}")
        field_desc = resolve_field_descriptors(entries, args.mc_jar)
        print(f"resolved {len(field_desc)} field descriptors from {args.mc_jar.name}")

    lines, todo = to_aw_lines(entries, field_desc)
    header = ["accessWidener v2 named"]

    if args.check:
        print(f"would emit {len(lines)} AW lines; {len(todo)} field entries pending descriptors")
        for t in todo:
            print("  " + t)
        return

    out = header + [""] + lines
    if todo:
        out += ["", "# --- fields awaiting descriptor resolution (run with --mc-jar or via generateAccessWidener) ---"]
        out += todo
    OUT_FILE.write_text("\n".join(out) + "\n", encoding="utf-8", newline="")
    print(f"wrote {OUT_FILE.relative_to(REPO_ROOT)} ({len(lines)} lines, {len(todo)} TODO fields)")


if __name__ == "__main__":
    main()
