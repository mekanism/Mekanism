package mekanism.common.lib.multiblock;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import mekanism.common.util.EnumUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public class FormationProtocol<T extends MultiblockData> {

    public static final int MAX_SIZE = 18;

    /**
     * The original block the calculation is getting run from.
     */
    private final IMultiblock<T> pointer;
    private final Structure structure;
    private final MultiblockManager<T> manager;

    public final Set<BlockPos> locations = new ObjectOpenHashSet<>();
    public final Set<BlockPos> innerNodes = new ObjectOpenHashSet<>();
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

        FormationResult result = validator.validate(this);
        if (!result.isFormed()) {
            return fail(result);
        }

        structure.locations = locations;
        structure.innerNodes = innerNodes;
        structure.valves = valves;
        result = validator.postcheck(structure, innerNodes);
        return result.isFormed() ? form(structure, idsFound) : fail(result);
    }

    /**
     * Runs the protocol and updates all nodes that make a part of the multiblock.
     */
    public FormationResult doUpdate() {
        IStructureValidator<T> validator = manager.createValidator();
        validator.init(pointer.getTileWorld(), manager, structure);
        if (!validator.precheck()) {
            return FormationResult.FAIL;
        }
        StructureResult result = buildStructure(validator);
        T structureFound = result.structureFound;

        if (structureFound != null && structureFound.locations.contains(pointer.getTilePos())) {
            pointer.setMultiblockData(manager, structureFound);
            structureFound.setFormedForce(true);
            MultiblockCache<T> cache = manager.createCache();
            UUID idToUse = null;
            if (result.idsFound.isEmpty()) {
                idToUse = manager.getUniqueInventoryID();
            } else {
                List<ItemStack> rejectedItems = new ArrayList<>();
                for (UUID id : result.idsFound) {
                    if (manager.inventories.get(id) != null) {
                        cache.merge(manager.pullInventory(pointer.getTileWorld(), id), rejectedItems);
                        idToUse = id;
                    }
                }
                //TODO someday: drop all items in rejectedItems
                //TODO seriously this needs to happen soon
                //TODO perhaps drop from pointer?
            }

            cache.apply(structureFound);
            structureFound.inventoryID = idToUse;
            structureFound.onCreated(pointer.getTileWorld());
            return FormationResult.SUCCESS;
        } else {
            pointer.getStructure().removeMultiblock(pointer.getTileWorld());
            return result.getFormationResult();
        }
    }

    protected static ITextComponent text(BlockPos pos) {
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
            if (traversed.size() >= maxCount) {
                return traversed.size();
            }
            for (Direction side : EnumUtils.DIRECTIONS) {
                BlockPos offset = ptr.offset(side);
                if (!traversed.contains(offset) && checker.test(offset)) {
                    openSet.add(offset);
                    traversed.add(offset);
                }
            }
        }
        return traversed.size();
    }

    public static class FormationResult {

        public static final FormationResult SUCCESS = new FormationResult(true, null);
        public static final FormationResult FAIL = new FormationResult(false, null);

        private final ITextComponent resultText;
        private final boolean formed;

        private FormationResult(boolean formed, ITextComponent resultText) {
            this.formed = formed;
            this.resultText = resultText;
        }

        public static FormationResult fail(ILangEntry text, BlockPos pos) {
            return new FormationResult(false, text.translateColored(EnumColor.GRAY, EnumColor.INDIGO, text(pos)));
        }

        public static FormationResult fail(ILangEntry text) {
            return new FormationResult(false, text.translateColored(EnumColor.GRAY));
        }

        public boolean isFormed() {
            return formed;
        }

        public ITextComponent getResultText() {
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
