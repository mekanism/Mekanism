# Mekanism Fabric Port — Framework & Procedure

This branch (`fabric/1.21.x`) is an unofficial Fabric port of Mekanism, structured for
**repeatable re-porting of upstream releases** (Create: Fabric model): keep the diff against
upstream minimal, concentrate loader differences in bridge libraries + a small shim layer,
and make mechanical rewrites scripted and replayable.

## Branch model

- `1.21.x` — pristine upstream tracking branch (never commit port work here).
- `fabric/1.21.x` — the port. History is organized as:
  1. **Scripted commits** — output of `fabric-port/` transforms, replayable (`[scripted]` prefix).
  2. **Hand-edit commits** — shims, build system, mixins, adaptations (`[port]` prefix).
- Future: `fabric/1.20.x` — 1.20.1 Fabric track, based on upstream `1.20.x` (v10.4.x, Forge-era),
  same pipeline with a Forge→Fabric mapping table. New upstream features/fixes get cherry-picked
  and adapted; candidates tracked per release below. Stand up after the 1.21.1 first milestone.

## Upstream release merge procedure

1. `git fetch upstream && git checkout fabric/1.21.x`
2. `git merge <upstream-release-tag>` — resolve conflicts:
   - `build.gradle`, `settings.gradle`: keep ours; review upstream for dependency/source-set changes.
   - Scripted-rewrite files (e.g. FluidStack imports): take **theirs**, then re-run transforms (step 3).
3. Re-run transforms: `python fabric-port/remap.py` (import/type remaps),
   `python fabric-port/at2aw.py` (regenerate access widener from upstream's `accesstransformer.cfg`).
4. Fix residuals: `gradlew build` → work down compile errors; consult the residual checklist below.
5. Verify: dev client boots, smoke test (machine place/GUI/energy), gametest subset.
6. Tag `v<upstream-version>+fabric`.

### Residual checklist (things transforms can't catch)

- [ ] New `@SubscribeEvent` handlers → wire into the event glue (`mekanism.fabric_shim.event`)
- [ ] New capabilities registered via `RegisterCapabilitiesEvent` → register in the Fabric lookup registrar
- [ ] New packets → confirm they flow through `PacketHandler` funnel (they should)
- [ ] New config options → no action (Forge Config API Port), but verify spec loads
- [ ] New NeoForge-only integration hooks → gate behind `ModList`-shim `isLoaded` checks
- [ ] New client extensions (`IClientItemExtensions` etc.) → add Fabric renderer registrations
- [ ] AT changes upstream → regenerate access widener; check for AW-inexpressible entries

## Bridge dependencies (verified 2026-07-04)

| Dependency | Coordinates | Version | Repo |
|---|---|---|---|
| Fabric Loader | `net.fabricmc:fabric-loader` | 0.19.3 | maven.fabricmc.net |
| Fabric API | `net.fabricmc.fabric-api:fabric-api` | 0.116.1+1.21.1 | maven.fabricmc.net |
| Fabric Loom | `fabric-loom` (plugin) | 1.17 (needs Gradle ≥9.5) | maven.fabricmc.net |
| Porting Lib | `io.github.fabricators_of_create.Porting-Lib:<module>` | 3.1.0 (1.21.1 branch) | mvn.devos.one/releases |
| Forge Config API Port | `fuzs.forgeconfigapiport:forgeconfigapiport-fabric` | 21.1.6 | raw.githubusercontent.com/Fuzss/modresources/main/maven |
| team-reborn Energy | `teamreborn:energy` | 4.1.0 | maven.modmuss50.me |
| NeoForge event bus (standalone, JiJ) | `net.neoforged:bus` | 8.0.5 (matches NeoForge 21.1.200) | maven.neoforged.net/releases |
| TypeTools (bus's listener-type resolution) | `net.jodah:typetools` | 0.6.3 | Maven Central |

Using the real (loader-independent) NeoForge bus keeps all `net.neoforged.bus.api.*` imports
(IEventBus/Event/EventPriority/SubscribeEvent) unchanged — no remap, priorities and untyped
`addListener` behave exactly as upstream expects. Verified at runtime in dev (untyped
method-reference listeners resolve; registry lifecycle posts through the bus).

Porting Lib modules of interest: `obj_loader` (transmitter OBJ models), `transfer`,
`fake_players`, `tool_actions`, `client_events`, `level_events`, `attributes`.
Exact module names/availability to be confirmed when Phase 2/4 wire them in.

Mappings: **Mojang official** (via `loom.officialMojangMappings()`) — upstream code stays
textually identical; no Yarn remap.

## Port status

- [x] Phase 0a: branch + framework scaffolding
- [x] Phase 0b: Loom build boots an empty mod (api/main source sets dormant) — dev server boot verified 2026-07-05
- [x] Phase 0c: AT→AW generation wired into build (`fabric-port/at2aw.py`; 92 live entries → 94 AW lines, validated by Loom; 2 stale upstream AT entries skip-listed)
- [ ] Phase 1: entry points, registries, config
  - [x] 1a: `mekanism.fabric_shim` skeleton — DeferredRegister/DeferredHolder/RegistryBuilder +
        RegisterEvent/NewRegistryEvent replayed on the real NeoForge bus (JiJ'd `net.neoforged:bus`)
        in NeoForge's registration order; environment shims (FMLEnvironment/Dist/OnlyIn/ModList/
        ServerLifecycleHooks/FakePlayer). Dev-server verified 2026-07-05.
  - [ ] 1b: custom Chemical/Module/RobitSkin registries (MekanismAPI RegistryBuilder path + datapack registry)
  - [ ] 1c: entry-point split (loader-neutral Mekanism init from Fabric bootstrap)
  - [ ] 1d: config via Forge Config API Port, attachments, SavedData server holder
  - [ ] 1e: api source set compiling (scripted remap + shim growth)
- [ ] Phase 2: capabilities + transfer/energy bridge (critical path)
- [ ] Phase 3: events + networking
- [ ] Phase 4: client (models, renderers, shaders)
- [ ] Phase 5: integrations + API cleanup
- [ ] Phase 6: datagen import, gametests, parity QA

Full assessment and phase details: see the approved port plan (session artifact) and
`fabric-port/README.md` for transform tooling usage.
