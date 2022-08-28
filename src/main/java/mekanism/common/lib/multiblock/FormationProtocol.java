package mekanism.common.lib.multiblock;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import mekanism.common.lib.multiblock.MultiblockCache.RejectContents;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
    public final Set<UUID> idsFound = new ObjectOpenHashSet<>();

    public FormationProtocol(IMultiblock<T> tile, Structure structure) {
        pointer = tile;
        this.structure = structure;
        manager = tile.getManager();
    }

    public StructureResult buildStructure(IStructureValidator<T> validator) {
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
        Level world = pointer.getTileWorld();
        validator.init(world, manager, structure);
        if (!validator.precheck()) {
            return FormationResult.FAIL;
        }
        StructureResult result = buildStructure(validator);
        T structureFound = result.structureFound;

        BlockPos pointerPos = pointer.getTilePos();
        if (structureFound != null && structureFound.locations.contains(pointerPos)) {
            pointer.setMultiblockData(manager, structureFound);
            structureFound.setFormedForce(true);
            MultiblockCache<T> cache = null;
            UUID idToUse = null;
            if (!result.idsFound.isEmpty()) {
                RejectContents rejectContents = new RejectContents();
                for (UUID id : result.idsFound) {
                    MultiblockCache<T> foundCache = manager.pullInventory(world, id);
                    if (foundCache != null) {
                        if (idToUse == null) {
                            cache = foundCache;
                            idToUse = id;
                        } else {
                            cache.merge(foundCache, rejectContents);
                        }
                    }
                }
                if (!rejectContents.rejectedItems.isEmpty()) {
                    Vec3 dropPosition = Vec3.atCenterOf(pointerPos);
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
                if (!rejectContents.rejectedGases.isEmpty()) {
                    //Dump any rejected gases, if they are radioactive vent them into the atmosphere
                    double radiation = 0;
                    for (GasStack rejectedGas : rejectContents.rejectedGases) {
                        if (rejectedGas.has(GasAttributes.Radiation.class)) {
                            //If we have a radioactive substance, then we need to set the tank to empty
                            radiation += rejectedGas.getAmount() * rejectedGas.get(GasAttributes.Radiation.class).getRadioactivity();
                        }
                    }
                    if (radiation > 0) {
                        Coord4D dumpLocation = new Coord4D(structureFound.getBounds().getCenter(), world);
                        MekanismAPI.getRadiationManager().radiate(dumpLocation, radiation);
                    }
                }
            }
            if (idToUse == null) {
                idToUse = manager.getUniqueInventoryID();
                cache = manager.createCache();
            }

            cache.apply(structureFound);
            structureFound.inventoryID = idToUse;
            structureFound.onCreated(world);
            //TODO: Do we want to validate against overfilled tanks here?
            return FormationResult.SUCCESS;
        }
        pointer.getStructure().removeMultiblock(world);
        return result.getFormationResult();
    }

    protected static Component text(BlockPos pos) {
        return MekanismLang.GENERIC_PARENTHESIS.translate(MekanismLang.GENERIC_BLOCK_POS.translate(pos.getX(), pos.getY(), pos.getZ()));
    }

    public static int explore(BlockPos start, Predicate<BlockPos> checker) {
        return explore(start, checker, MAX_SIZE * MAX_SIZE * MAX_SIZE);
    }

    public static int explore(BlockPos start, Predicate<BlockPos> checker, int maxCount) {
        if (!checker.test(start)) {
            return 0;
        }

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
                BlockPos offset = ptr.relative(side);
                if (!traversed.contains(offset) && checker.test(offset)) {
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

    private StructureResult fail(FormationResult result) {
        return new StructureResult(result, null, null);
    }

    private StructureResult form(T structureFound, Set<UUID> idsFound) {
        return new StructureResult(FormationResult.SUCCESS, structureFound, idsFound);
    }

    private class StructureResult {

        private final FormationResult result;
        private final T structureFound;
        private final Set<UUID> idsFound;

        private StructureResult(FormationResult result, T structureFound, Set<UUID> idsFound) {
            this.result = result;
            this.structureFound = structureFound;
            this.idsFound = idsFound;
        }

        private FormationResult getFormationResult() {
            return result;
        }
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
