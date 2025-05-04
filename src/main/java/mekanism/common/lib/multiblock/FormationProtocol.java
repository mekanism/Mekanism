package mekanism.common.lib.multiblock;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import mekanism.common.lib.multiblock.MultiblockCache.RejectContents;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;

public class FormationProtocol<T extends MultiblockData> {

    public static final int MAX_SIZE = 18;

    /**
     * The original block the calculation is getting run from.
     */
    private final IMultiblock<T> pointer;
    private final Structure structure;
    private final MultiblockManager<T> manager;

    public final Set<BlockPos> locations = new ObjectOpenHashSet<>();
    public final Set<BlockPos> internalLocations = new ObjectOpenHashSet<>();
    public final Set<ValveData> valves = new ObjectOpenHashSet<>();
    public final Map<UUID, MultiblockCache<T>> idsFound = new HashMap<>();

    public FormationProtocol(IMultiblock<T> tile, Structure structure) {
        pointer = tile;
        this.structure = structure;
        manager = tile.getManager();
    }

    private StructureResult<T> buildStructure(IStructureValidator<T> validator) {
        T structure = pointer.createMultiblock();
        if (!structure.setShape(validator.getShape())) {
            return fail(FormationResult.FAIL);
        }

        Long2ObjectMap<ChunkAccess> chunkMap = new Long2ObjectOpenHashMap<>();
        FormationResult result = validator.validate(this, chunkMap);
        if (!result.isFormed()) {
            return fail(result);
        }

        structure.locations = locations;
        structure.internalLocations = internalLocations;
        structure.valves = valves;
        result = validator.postcheck(structure, chunkMap);
        return result.isFormed() ? form(structure, idsFound) : fail(result);
    }

    /**
     * Runs the protocol and updates all nodes that make a part of the multiblock.
     */
    public FormationResult doUpdate() {
        IStructureValidator<T> validator = manager.createValidator();
        Level world = pointer.getLevel();
        validator.init(world, manager, structure);
        if (!validator.precheck()) {
            return FormationResult.FAIL;
        }
        StructureResult<T> result = buildStructure(validator);
        T structureFound = result.structureFound;

        BlockPos pointerPos = pointer.getBlockPos();
        if (structureFound != null && structureFound.locations.contains(pointerPos)) {
            pointer.setMultiblockData(manager, structureFound);
            structureFound.setFormedForce(true);
            MultiblockCache<T> cache = null;
            //Note: We use a new id each time to ensure that any other multiblocks that reference any potentially found id become properly stale
            // instead of them still having a reference to our newly formed multiblock's cache
            // In theory we could partially get around this issue by keeping track of all the positions for the multiblock in the cache and only
            // reuse the id if we contain all elements we previously had, but doing so is not currently worth all the extra checks
            UUID idToUse = manager.getUniqueInventoryID();
            if (!result.idsFound().isEmpty()) {
                RejectContents rejectContents = new RejectContents();
                for (Map.Entry<UUID, MultiblockCache<T>> entry : result.idsFound().entrySet()) {
                    if (cache == null) {
                        cache = entry.getValue();
                    } else {
                        cache.merge(entry.getValue(), rejectContents);
                    }
                }
                //Replace the caches for all the old ids with a singular merged cache with our desired id
                manager.replaceCaches(result.idsFound().keySet(), idToUse, cache);
                if (!rejectContents.rejectedItems.isEmpty()) {
                    //TODO - 1.20.4: Don't drop it in the center if there is no nearest player, maybe drop it on top of the multiblock? Or to one of the sides
                    Vec3 dropPosition = pointerPos.getCenter();
                    //Try to see which player was nearest to multiblocks that have rejected items
                    Player nearestPlayer = world.getNearestPlayer(dropPosition.x, dropPosition.y, dropPosition.z, 25, true);
                    if (nearestPlayer != null) {
                        //If there is one drop at the player instead of at the block that triggered the formation
                        dropPosition = nearestPlayer.position();
                    }
                    for (ItemStack rejectedItem : rejectContents.rejectedItems) {
                        world.addFreshEntity(new ItemEntity(world, dropPosition.x, dropPosition.y, dropPosition.z, rejectedItem));
                    }
                }
                if (!rejectContents.rejectedChemicals.isEmpty() && RadiationManager.isGlobalRadiationEnabled()) {
                    //Dump any rejected gases, if they are radioactive vent them into the atmosphere
                    // we are able to skip this if radiation is disabled as it will just NO-OP further down the line
                    double radiation = 0;
                    for (ChemicalStack rejectedChemical : rejectContents.rejectedChemicals) {
                        //If we have a radioactive substance, then we need to set the tank to empty
                        radiation += rejectedChemical.getRadioactivity();
                    }
                    if (radiation > 0) {
                        IRadiationManager.INSTANCE.radiate(world, structureFound.getBounds().getCenter(), radiation);
                    }
                }
            }
            boolean trackCache = cache == null;
            if (trackCache) {
                cache = manager.createCache();
            }

            cache.apply(world.registryAccess(), structureFound);
            structureFound.inventoryID = idToUse;
            structureFound.onCreated(world);
            if (trackCache) {
                //If it is a new fresh cache we need to make sure to then sync the multiblock back to the cache
                // so that we don't save it with empty data as otherwise we may end up with crashes in when merging multiblock caches
                cache.sync(structureFound);
                // and then we let the manager start tracking the cache so that it gets saved to the manager and can be used by multiblocks
                manager.trackCache(idToUse, cache);
            }
            //TODO: Do we want to validate against overfilled tanks here?
            return FormationResult.SUCCESS;
        }
        pointer.getStructure().removeMultiblock(world);
        return result.result();
    }

    protected static Component text(BlockPos pos) {
        return MekanismLang.GENERIC_PARENTHESIS.translate(MekanismLang.GENERIC_BLOCK_POS.translate(pos.getX(), pos.getY(), pos.getZ()));
    }

    protected static Component text(BlockState state) {
        return state.getBlock().getName();
    }

    @FunctionalInterface
    public interface FormationChecker<NODE> {

        boolean check(Level level, Long2ObjectMap<ChunkAccess> chunkMap, BlockPos start, NODE node, BlockPos toCheck);
    }

    public static <NODE> int explore(Level level, Long2ObjectMap<ChunkAccess> chunkMap, BlockPos start, NODE node, FormationChecker<NODE> checker) {
        return explore(level, chunkMap, start, node, checker, MAX_SIZE * MAX_SIZE * MAX_SIZE);
    }

    public static <NODE> int explore(Level level, Long2ObjectMap<ChunkAccess> chunkMap, BlockPos start, NODE node, FormationChecker<NODE> checker, int maxCount) {
        if (!checker.check(level, chunkMap, start, node, start)) {
            return 0;
        }

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        Queue<BlockPos> openSet = new LinkedList<>();
        Set<BlockPos> traversed = new ObjectOpenHashSet<>();
        openSet.add(start);
        traversed.add(start);
        while (!openSet.isEmpty()) {
            BlockPos ptr = openSet.poll();
            int traversedSize = traversed.size();
            if (traversedSize >= maxCount) {
                return traversedSize;
            }
            for (Direction side : EnumUtils.DIRECTIONS) {
                mutable.setWithOffset(ptr, side);
                if (!traversed.contains(mutable) && checker.check(level, chunkMap, start, node, mutable)) {
                    BlockPos offset = mutable.immutable();
                    openSet.add(offset);
                    traversed.add(offset);
                }
            }
        }
        return traversed.size();
    }

    public static class FormationResult {

        public static final FormationResult SUCCESS = new FormationResult(true, null, false);
        public static final FormationResult FAIL = new FormationResult(false, null, false);

        private final Component resultText;
        private final boolean formed;
        private final boolean noIgnore;

        private FormationResult(boolean formed, Component resultText, boolean noIgnore) {
            this.formed = formed;
            this.resultText = resultText;
            this.noIgnore = noIgnore;
        }

        public static FormationResult fail(ILangEntry text, BlockPos pos) {
            return fail(text, pos, false);
        }

        public static FormationResult fail(ILangEntry text, BlockPos pos, boolean noIgnore) {
            return fail(text.translateColored(EnumColor.GRAY, EnumColor.INDIGO, text(pos)), noIgnore);
        }

        public static FormationResult fail(ILangEntry text, BlockPos pos, BlockState state) {
            return fail(text, pos, state, false);
        }

        public static FormationResult fail(ILangEntry text, BlockPos pos, BlockState state, boolean noIgnore) {
            return fail(text.translateColored(EnumColor.GRAY, EnumColor.INDIGO, text(pos), EnumColor.WHITE, text(state)), noIgnore);
        }

        public static FormationResult fail(ILangEntry text) {
            return fail(text, false);
        }

        public static FormationResult fail(ILangEntry text, boolean noIgnore) {
            return fail(text.translateColored(EnumColor.GRAY), noIgnore);
        }

        public static FormationResult fail(Component text) {
            return fail(text, false);
        }

        public static FormationResult fail(Component text, boolean noIgnore) {
            return new FormationResult(false, text, noIgnore);
        }

        public boolean isFormed() {
            return formed;
        }

        public boolean isNoIgnore() {
            return noIgnore;
        }

        public Component getResultText() {
            return resultText;
        }
    }

    private StructureResult<T> fail(FormationResult result) {
        return new StructureResult<>(result, null, null);
    }

    private StructureResult<T> form(T structureFound, Map<UUID, MultiblockCache<T>> idsFound) {
        return new StructureResult<>(FormationResult.SUCCESS, structureFound, idsFound);
    }

    private record StructureResult<T extends MultiblockData>(FormationResult result, T structureFound, Map<UUID, MultiblockCache<T>> idsFound) {
    }

    public enum CasingType {
        FRAME,
        VALVE,
        OTHER,
        INVALID;

        boolean isFrame() {
            return this == FRAME;
        }

        boolean isValve() {
            return this == VALVE;
        }
    }

    public enum StructureRequirement {
        IGNORED,
        FRAME,
        OTHER,
        INNER;

        public static final StructureRequirement[] REQUIREMENTS = values();

        boolean needsFrame() {
            return this == FRAME;
        }

        boolean isCasing() {
            return this != INNER;
        }
    }
}
