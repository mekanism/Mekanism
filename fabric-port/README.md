# fabric-port — transform tooling

Scripted, replayable source transforms for the Fabric port. These are re-run after every
upstream merge (see ../PORTING.md).

## The one design rule

**Shim classes keep the NeoForge simple class name.** `mekanism.fabric_shim.fml.ModList`
shims `net.neoforged.fml.ModList`, `mekanism.fabric_shim.registries.DeferredRegister` shims
`net.neoforged.neoforge.registries.DeferredRegister`, etc. That way the transform is a pure
**import-line rewrite** — call sites never change, so upstream merges touch scripted files
only at import blocks (near-zero conflict surface).

Only when a mapping intentionally changes the simple name (rare; avoid) does the transform
rewrite references, marked with mode `fq` in the mapping table.

## Files

- `mappings/neoforge-to-fabric.tsv` — mapping table: `old_fqcn<TAB>new_fqcn<TAB>[mode]`.
  Mode: empty/`import` = rewrite import lines + fully-qualified usages only (simple names
  must match); `fq` = additionally rewrite simple-name references (use sparingly).
  Lines starting with `#` are comments.
- `remap.py` — applies the mapping table across source sets. Idempotent.
  Usage: `python fabric-port/remap.py [--check]` (`--check` reports without writing).
- `at2aw` — access-transformer → access-widener conversion lives as a buildSrc Gradle task
  (`generateAccessWidener`) because it needs the Minecraft jar to resolve member descriptors.

## Commit convention

Run one transform → commit its full output as a single `[scripted] <transform>: <summary>`
commit. Never mix hand edits into scripted commits (breaks replayability).
