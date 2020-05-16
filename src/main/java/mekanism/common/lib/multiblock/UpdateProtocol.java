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
import mekanism.common.lib.multiblock.IMultiblockBase.UpdateType;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.EnumUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public abstract class UpdateProtocol<T extends MultiblockData> {

    public static final int MAX_SIZE = 18;

    /**
     * The original block the calculation is getting run from.
     */
    public TileEntityMultiblock<T> pointer;

    public UpdateProtocol(TileEntityMultiblock<T> tile) {
        pointer = tile;
    }

    public StructureResult buildStructure(BlockPos corner) {
        BlockPos min = corner, max = traverse(corner, 0, Direction.EAST, Direction.UP, Direction.SOUTH);
        T structure = getNewStructure();
        if (!structure.buildStructure(min, max)) {
            return fail(FormationResult.FAIL);
        }

        Set<BlockPos> locations = new ObjectOpenHashSet<>();
        Set<BlockPos> innerNodes = new ObjectOpenHashSet<>();
        Set<ValveData> valves = new ObjectOpenHashSet<>();
        Set<UUID> idsFound = new ObjectOpenHashSet<>();

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (x == min.getX() || x == max.getX() || y == min.getY() || y == max.getY() || z == min.getZ() || z == max.getZ()) {
                        CasingType type = getCasingType(pos);
                        TileEntity tile = pointer.getWorld().getTileEntity(pos);
                        // terminate if we encounter a node that already failed this tick
                        if (tile instanceof IMultiblockBase && ((IMultiblockBase) tile).updatedThisTick()) {
                            return fail(FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_FRAME, pos));
                        }
                        if (!checkNode(tile, true) || isFramePos(pos, min, max) && !type.isFrame()) {
                            //If it is not a valid node or if it is supposed to be a frame but is invalid
                            // then we are not valid over all
                            return fail(FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_FRAME, pos));
                        } else {
                            if (tile instanceof IMultiblock) {
                                @SuppressWarnings("unchecked")
                                IMultiblock<T> multiblockTile = (IMultiblock<T>) tile;
                                UUID uuid = multiblockTile.getCacheID();
                                if (uuid != null && multiblockTile.getManager() == getManager()) {
                                    getManager().updateCache(multiblockTile, false);
                                    idsFound.add(uuid);
                                }
                                multiblockTile.setMultiblock(structure);
                                locations.add(pos);
                            } else if (tile instanceof IStructuralMultiblock) {
                                ((IStructuralMultiblock) tile).setMultiblock(structure);
                            }

                            if (type.isValve()) {
                                ValveData data = new ValveData();
                                data.location = pos;
                                data.side = getSide(data.location, min, max);
                                valves.add(data);
                            }
                        }
                    } else if (!isValidInnerNode(pos)) {
                        return fail(FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_INNER, pos));
                    } else if (!pointer.getWorld().isAirBlock(pos)) {
                        innerNodes.add(pos);
                    }
                }
            }
        }

        structure.locations = locations;
        structure.valves = valves;
        FormationResult result = validate(structure, innerNodes);
        return result.isFormed() ? form(structure, idsFound) : fail(result);
    }

    private BlockPos traverse(BlockPos orig, int dist, Direction... sides) {
        if (dist + 1 > MAX_SIZE * 3)
            return null;
        for (Direction side : sides) {
            BlockPos offset = orig.offset(side);
            if (checkNode(pointer.getWorld().getTileEntity(offset), false)) {
                return traverse(offset, dist + 1, sides);
            }
        }
        return orig;
    }

    protected FormationResult validate(T structure, Set<BlockPos> innerNodes) {
        return FormationResult.SUCCESS;
    }

    public Direction getSide(BlockPos pos, BlockPos min, BlockPos max) {
        if (pos.getX() == min.getX()) {
            return Direction.WEST;
        } else if (pos.getX() == max.getX()) {
            return Direction.EAST;
        } else if (pos.getY() == min.getY()) {
            return Direction.DOWN;
        } else if (pos.getY() == max.getY()) {
            return Direction.UP;
        } else if (pos.getZ() == min.getZ()) {
            return Direction.NORTH;
        } else if (pos.getZ() == max.getZ()) {
            return Direction.SOUTH;
        }
        return null;
    }

    protected boolean isValidInnerNode(BlockPos pos) {
        return pointer.getWorld().isAirBlock(pos);
    }

    /**
     * If the TileEntity will function as a 'structural block' (casing or structural multiblock)
     * for this structure. This will also mark the tile in this position as having just received a multiblock
     * update if 'markUpdated' is true.
     * @return Whether or not the tile is a viable node for a multiblock structure
     */
    protected boolean checkNode(TileEntity tile, boolean markUpdated) {
        if (markUpdated && tile instanceof IMultiblockBase) {
            ((IMultiblockBase) tile).markUpdated();
        }
        if (tile instanceof IStructuralMultiblock && ((IStructuralMultiblock) tile).canInterface(pointer)) {
            return true;
        }
        return MultiblockManager.areCompatible(tile, pointer);
    }

    private boolean isFramePos(BlockPos obj, BlockPos min, BlockPos max) {
        boolean xMatches = obj.getX() == min.getX() || obj.getX() == max.getX();
        boolean yMatches = obj.getY() == min.getY() || obj.getY() == max.getY();
        boolean zMatches = obj.getZ() == min.getZ() || obj.getZ() == max.getZ();
        return xMatches && yMatches || xMatches && zMatches || yMatches && zMatches;
    }

    protected abstract CasingType getCasingType(BlockPos pos);

    protected abstract MultiblockManager<T> getManager();

    protected T getNewStructure() {
        return pointer.getNewStructure();
    }

    /**
     * Runs the protocol and updates all nodes that make a part of the multiblock.
     */
    public FormationResult doUpdate(UpdateType type) {
        BlockPos corner = traverse(pointer.getPos(), 0, Direction.WEST, Direction.DOWN, Direction.NORTH);
        if (corner == null) {
            return FormationResult.FAIL;
        }
        StructureResult result = buildStructure(corner);
        T structureFound = result.structureFound;

        if (structureFound != null && structureFound.locations.contains(pointer.getPos())) {
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
            pointer.getMultiblock().remove(pointer.getWorld());
            if (type == UpdateType.INITIAL) {
                // run a traversal over connected multiblocks of the same type to make sure we don't re-run an update for every connected block
                new Explorer(pos -> checkNode(pointer.getWorld().getTileEntity(pos), true)).explore(pointer.getPos());
            }
            return result.getFormationResult();
        }
    }

    public static class FormationResult {

        public static final FormationResult SUCCESS = new FormationResult(true, null);
        public static final FormationResult FAIL = new FormationResult(false, null);

        private ITextComponent resultText;
        private boolean formed;

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

        private FormationResult result;
        private T structureFound;
        private Set<UUID> idsFound;

        private StructureResult(FormationResult result, T structureFound, Set<UUID> idsFound) {
            this.result = result;
            this.structureFound = structureFound;
            this.idsFound = idsFound;
        }

        private FormationResult getFormationResult() {
            return result;
        }
    }

    protected static ITextComponent text(BlockPos pos) {
        return MekanismLang.GENERIC_BLOCK_POS.translate(pos.getX(), pos.getY(), pos.getZ());
    }

    protected static class Explorer {

        protected Predicate<BlockPos> checker;
        protected int maxCount;

        public Explorer(Predicate<BlockPos> checker, int maxCount) {
            this.checker = checker;
            this.maxCount = maxCount;
        }

        public Explorer(Predicate<BlockPos> checker) {
           this(checker, MAX_SIZE * MAX_SIZE * MAX_SIZE);
        }

        public int explore(BlockPos start) {
            if (!checker.test(start)) {
                return 0;
            }

            Queue<BlockPos> openSet = new LinkedList<>();
            Set<BlockPos> traversed = new ObjectOpenHashSet<>();
            openSet.add(start);
            traversed.add(start);
            while (!openSet.isEmpty()) {
                BlockPos ptr = openSet.poll();
                if (traversed.size() >= maxCount)
                    return traversed.size();
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
