package mekanism.common.lib.multiblock;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import mekanism.common.util.EnumUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public abstract class FormationProtocol<T extends MultiblockData> {

    public static final int MAX_SIZE = 18;

    /**
     * The original block the calculation is getting run from.
     */
    public IMultiblock<T> pointer;

    public FormationProtocol(IMultiblock<T> tile) {
        pointer = tile;
    }

    public StructureResult buildStructure(IStructureValidator validator) {
        T structure = pointer.createMultiblock();
        if (!structure.setShape(validator.getShape())) {
            return fail(FormationResult.FAIL);
        }

        ValidationContext ctx = new ValidationContext(validator);
        FormationResult result = validator.validate(this, ctx);
        if (!result.isFormed()) {
            return fail(result);
        }

        structure.locations = ctx.locations;
        structure.valves = ctx.valves;
        result = validate(structure, ctx.innerNodes);
        return result.isFormed() ? form(structure, ctx.idsFound) : fail(result);
    }

    protected FormationResult validate(T structure, Set<BlockPos> innerNodes) {
        return FormationResult.SUCCESS;
    }

    protected boolean isValidInnerNode(BlockPos pos) {
        return pointer.getWorld().isAirBlock(pos);
    }

    /**
     * If the TileEntity will function as a 'structural block' (casing or structural multiblock) for this structure. This will also mark the tile in this position as
     * having just received a multiblock update if 'markUpdated' is true.
     *
     * @return Whether or not the tile is a viable node for a multiblock structure
     */
    protected boolean checkNode(TileEntity tile) {
        if (tile instanceof IStructuralMultiblock && ((IStructuralMultiblock) tile).canInterface(getManager())) {
            return true;
        }
        return getManager().isCompatible(tile);
    }

    protected abstract CasingType getCasingType(BlockPos pos);

    protected abstract MultiblockManager<T> getManager();

    /**
     * Runs the protocol and updates all nodes that make a part of the multiblock.
     */
    public FormationResult doUpdate(IStructureValidator validator) {
        StructureResult result = buildStructure(validator);
        T structureFound = result.structureFound;

        if (structureFound != null && structureFound.locations.contains(pointer.getPos())) {
            pointer.setMultiblockData(structureFound);
            structureFound.setFormedForce(true);
            MultiblockCache<T> cache = getManager().getNewCache();
            MultiblockManager<T> manager = getManager();
            UUID idToUse = null;
            if (result.idsFound.isEmpty()) {
                idToUse = manager.getUniqueInventoryID();
            } else {
                List<ItemStack> rejectedItems = new ArrayList<>();
                for (UUID id : result.idsFound) {
                    if (manager.inventories.get(id) != null) {
                        cache.merge(manager.pullInventory(pointer.getWorld(), id), rejectedItems);
                        idToUse = id;
                    }
                }
                //TODO someday: drop all items in rejectedItems
                //TODO seriously this needs to happen soon
                //TODO perhaps drop from pointer?
            }

            cache.apply(structureFound);
            structureFound.inventoryID = idToUse;
            structureFound.onCreated(pointer.getWorld());
            return FormationResult.SUCCESS;
        } else {
            pointer.getStructure().removeMultiblock(pointer.getWorld());
            return result.getFormationResult();
        }
    }

    protected static ITextComponent text(BlockPos pos) {
        return MekanismLang.GENERIC_BLOCK_POS.translate(pos.getX(), pos.getY(), pos.getZ());
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

    public class ValidationContext {

        Set<BlockPos> locations = new ObjectOpenHashSet<>();
        Set<BlockPos> innerNodes = new ObjectOpenHashSet<>();
        Set<ValveData> valves = new ObjectOpenHashSet<>();
        Set<UUID> idsFound = new ObjectOpenHashSet<>();
        IStructureValidator validator;

        public ValidationContext(IStructureValidator validator) {
            this.validator = validator;
        }

        public FormationResult validateFrame(BlockPos pos, boolean needsFrame) {
            CasingType type = getCasingType(pos);
            IMultiblockBase tile = pointer.getStructure().getTile(pos);
            // terminate if we encounter a node that already failed this tick
            if (!checkNode((TileEntity) tile) || (needsFrame && !type.isFrame())) {
                //If it is not a valid node or if it is supposed to be a frame but is invalid
                // then we are not valid over all
                return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_FRAME, pos);
            } else {
                if (tile instanceof IMultiblock) {
                    @SuppressWarnings("unchecked")
                    IMultiblock<T> multiblockTile = (IMultiblock<T>) tile;
                    UUID uuid = multiblockTile.getCacheID();
                    if (uuid != null && multiblockTile.getManager() == getManager() && multiblockTile.hasCache()) {
                        getManager().updateCache(multiblockTile);
                        idsFound.add(uuid);
                    }
                }
                locations.add(pos);
                if (type.isValve()) {
                    ValveData data = new ValveData();
                    data.location = pos;
                    data.side = validator.getSide(data.location);
                    valves.add(data);
                }
            }
            return FormationResult.SUCCESS;
        }

        public FormationResult validateInner(BlockPos pos) {
            if (!isValidInnerNode(pos)) {
                return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_INNER, pos);
            } else if (!pointer.getWorld().isAirBlock(pos)) {
                innerNodes.add(pos);
            }
            return FormationResult.SUCCESS;
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
}
