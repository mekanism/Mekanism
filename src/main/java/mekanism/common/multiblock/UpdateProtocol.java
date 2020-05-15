package mekanism.common.multiblock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Action;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.multiblock.IValveHandler.ValveData;
import mekanism.common.multiblock.MultiblockCache.CacheSubstance;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public abstract class UpdateProtocol<T extends MultiblockData> {

    private static final int MAX_SIZE = 18;

    /**
     * The original block the calculation is getting run from.
     */
    public TileEntityMultiblock<T> pointer;

    public UpdateProtocol(TileEntityMultiblock<T> tile) {
        pointer = tile;
    }

    public StructureResult buildStructure(BlockPos corner) {
        BlockPos min = corner, max = traverse(corner, 0, Direction.EAST, Direction.UP, Direction.SOUTH);

        Set<BlockPos> locations = new ObjectOpenHashSet<>();
        Set<BlockPos> innerNodes = new ObjectOpenHashSet<>();
        Set<ValveData> valves = new ObjectOpenHashSet<>();

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (x == min.getX() || x == max.getX() || y == min.getY() || y == max.getY() || z == min.getZ() || z == max.getZ()) {
                        CasingType type = getCasingType(pos);
                        if (!checkNode(pos) || isFramePos(pos, min, max) && !type.isFrame()) {
                            //If it is not a valid node or if it is supposed to be a frame but is invalid
                            // then we are not valid over all
                            return new StructureResult(FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_FRAME, pos));
                        } else {
                            locations.add(pos);
                            if (type.isValve()) {
                                ValveData data = new ValveData();
                                data.location = pos;
                                data.side = getSide(data.location, min, max);
                                valves.add(data);
                            }
                        }
                    } else if (!isValidInnerNode(pos)) {
                        return new StructureResult(FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_INNER, pos));
                    } else if (!pointer.getWorld().isAirBlock(pos)) {
                        innerNodes.add(pos);
                    }
                }
            }
        }

        int length = Math.abs(max.getX() - min.getX()) + 1;
        int height = Math.abs(max.getY() - min.getY()) + 1;
        int width = Math.abs(max.getZ() - min.getZ()) + 1;
        T structure = getNewStructure();
        structure.locations = locations;
        structure.valves = valves;
        structure.length = length;
        structure.width = width;
        structure.height = height;
        structure.setVolume(structure.length * structure.width * structure.height);
        structure.renderLocation = corner.offset(Direction.UP);
        structure.minLocation = min;
        structure.maxLocation = max;

        if (length >= 3 && length <= MAX_SIZE && height >= 3 && height <= MAX_SIZE && width >= 3 && width <= MAX_SIZE) {
            if (validate(structure, innerNodes).isFormed()) {
                return new StructureResult(structure);
            }
        }

        return new StructureResult(FormationResult.FAIL);
    }

    private BlockPos traverse(BlockPos orig, int dist, Direction... sides) {
        if (dist + 1 > MAX_SIZE * 3)
            return null;
        for (Direction side : sides) {
            BlockPos offset = orig.offset(side);
            if (checkNode(offset)) {
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
     * @return Whether or not the block at the specified location is a viable node for a multiblock structure.
     */
    protected boolean checkNode(BlockPos pos) {
        TileEntity tile = MekanismUtils.getTileEntity(pointer.getWorld(), pos);
        if (tile instanceof IStructuralMultiblock && ((IStructuralMultiblock) tile).canInterface(pointer)) {
            return true;
        }
        return MultiblockManager.areCompatible(tile, pointer, true);
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

    protected void onFormed(T structureFound) {
        for (BlockPos pos : structureFound.internalLocations) {
            TileEntityInternalMultiblock tile = MekanismUtils.getTileEntity(TileEntityInternalMultiblock.class, pointer.getWorld(), pos);
            if (tile != null) {
                tile.setMultiblock(structureFound.inventoryID);
            }
        }

        if (shouldCap(CacheSubstance.FLUID)) {
            for (IExtendedFluidTank tank : structureFound.getFluidTanks(null)) {
                tank.setStackSize(Math.min(tank.getFluidAmount(), tank.getCapacity()), Action.EXECUTE);
            }
        }
        if (shouldCap(CacheSubstance.GAS)) {
            for (IGasTank tank : structureFound.getGasTanks(null)) {
                tank.setStackSize(Math.min(tank.getStored(), tank.getCapacity()), Action.EXECUTE);
            }
        }
        if (shouldCap(CacheSubstance.ENERGY)) {
            for (IEnergyContainer container : structureFound.getEnergyContainers(null)) {
                container.setEnergy(container.getEnergy().min(container.getMaxEnergy()));
            }
        }
    }

    protected boolean shouldCap(CacheSubstance type) {
        return true;
    }

    /**
     * Runs the protocol and updates all nodes that make a part of the multiblock.
     */
    public FormationResult doUpdate() {
        long time = System.nanoTime();
        BlockPos corner = traverse(pointer.getPos(), 0, Direction.WEST, Direction.DOWN, Direction.NORTH);
        if (corner == null) {
            return FormationResult.FAIL;
        }
        StructureResult result = buildStructure(corner);
        T structureFound = result.structureFound;

        if (structureFound != null && structureFound.locations.contains(pointer.getPos())) {
            structureFound.form();

            Set<UUID> idsFound = new HashSet<>();
            for (BlockPos obj : structureFound.locations) {
                TileEntity tile = MekanismUtils.getTileEntity(pointer.getWorld(), obj);
                if (tile instanceof TileEntityMultiblock) {
                    @SuppressWarnings("unchecked")
                    TileEntityMultiblock<T> multiblockTile = (TileEntityMultiblock<T>) tile;
                    UUID uuid = multiblockTile.getCacheID();
                    if (uuid != null && multiblockTile.getManager() == getManager()) {
                        getManager().updateCache(multiblockTile, false);
                        idsFound.add(uuid);
                    }
                }
            }

            MultiblockCache<T> cache = getManager().getNewCache();
            MultiblockManager<T> manager = getManager();
            UUID idToUse = null;
            if (idsFound.isEmpty()) {
                idToUse = manager.getUniqueInventoryID();
            } else {
                List<ItemStack> rejectedItems = new ArrayList<>();
                for (UUID id : idsFound) {
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

            List<IStructuralMultiblock> structures = new ArrayList<>();
            BlockPos toUse = null;

            for (BlockPos obj : structureFound.locations) {
                TileEntity tile = MekanismUtils.getTileEntity(pointer.getWorld(), obj);
                if (tile instanceof IMultiblock) {
                    ((IMultiblock<T>) tile).setMultiblock(structureFound);
                    if (toUse == null) {
                        toUse = obj;
                    }
                } else if (tile instanceof IStructuralMultiblock) {
                    ((IStructuralMultiblock) tile).setMultiblock(structureFound);
                }
            }

            structureFound.onCreated();
            onFormed(structureFound);

            //Remove all structural multiblocks from locations, set controllers
            for (IStructuralMultiblock node : structures) {
                structureFound.locations.remove(((TileEntity) node).getPos());
            }

            return FormationResult.SUCCESS;
        } else {
            pointer.getMultiblock().remove(pointer.getWorld());
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

    private class StructureResult {

        private FormationResult result;
        private T structureFound;

        private StructureResult(FormationResult result, T structureFound) {
            this.result = result;
            this.structureFound = structureFound;
        }

        private StructureResult(T structureFound) {
            this(FormationResult.SUCCESS, structureFound);
        }

        private StructureResult(FormationResult result) {
            this(result, null);
        }

        private FormationResult getFormationResult() {
            return result;
        }
    }

    protected static ITextComponent text(BlockPos pos) {
        return MekanismLang.GENERIC_BLOCK_POS.translate(pos.getX(), pos.getY(), pos.getZ());
    }

    public static class NodeCounter {

        public Set<BlockPos> iterated = new ObjectOpenHashSet<>();

        public Predicate<BlockPos> checker;

        public NodeCounter(Predicate<BlockPos> c) {
            checker = c;
        }

        public void loop(BlockPos pos) {
            iterated.add(pos);

            for (Direction side : EnumUtils.DIRECTIONS) {
                BlockPos offset = pos.offset(side);
                if (!iterated.contains(offset) && checker.test(offset)) {
                    loop(offset);
                }
            }
        }

        public int calculate(BlockPos pos) {
            if (!checker.test(pos)) {
                return 0;
            }
            loop(pos);
            return iterated.size();
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
