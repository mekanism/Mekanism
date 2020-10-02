package mekanism.common.lib.multiblock;

import java.util.Set;
import java.util.UUID;
import mekanism.common.MekanismLang;
import mekanism.common.lib.math.voxel.IShape;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.math.voxel.VoxelCuboid.WallRelative;
import mekanism.common.lib.multiblock.FormationProtocol.CasingType;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.lib.multiblock.FormationProtocol.StructureRequirement;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class CuboidStructureValidator<T extends MultiblockData> implements IStructureValidator<T> {

    private final VoxelCuboid minBounds;
    private final VoxelCuboid maxBounds;

    protected VoxelCuboid cuboid;
    protected Structure structure;

    protected World world;
    protected MultiblockManager<T> manager;

    public CuboidStructureValidator() {
        this(new VoxelCuboid(3, 3, 3), new VoxelCuboid(18, 18, 18));
    }

    public CuboidStructureValidator(VoxelCuboid minBounds, VoxelCuboid maxBounds) {
        this.minBounds = minBounds;
        this.maxBounds = maxBounds;
    }

    @Override
    public void init(World world, MultiblockManager<T> manager, Structure structure) {
        this.world = world;
        this.manager = manager;
        this.structure = structure;
    }

    @Override
    public FormationResult validate(FormationProtocol<T> ctx) {
        BlockPos min = cuboid.getMinPos(), max = cuboid.getMaxPos();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    FormationResult ret = validateNode(ctx, pos);
                    if (!ret.isFormed()) {
                        return ret;
                    }
                }
            }
        }
        return FormationResult.SUCCESS;
    }

    protected FormationResult validateNode(FormationProtocol<T> ctx, BlockPos pos) {
        StructureRequirement requirement = getStructureRequirement(pos);
        if (requirement.isCasing()) {
            BlockState state = world.getBlockState(pos);
            CasingType type = getCasingType(pos, state);
            FormationResult ret = validateFrame(ctx, pos, state, type, requirement.needsFrame());
            if (requirement != StructureRequirement.IGNORED && !ret.isFormed()) {
                return ret;
            }
        } else {
            if (!validateInner(pos)) {
                return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_INNER, pos);
            } else if (!world.isAirBlock(pos)) {
                ctx.innerNodes.add(pos);
            }
        }
        return FormationResult.SUCCESS;
    }

    protected boolean validateInner(BlockPos pos) {
        return world.isAirBlock(pos);
    }

    protected abstract CasingType getCasingType(BlockPos pos, BlockState state);

    protected boolean isFrameCompatible(TileEntity tile) {
        if (tile instanceof IStructuralMultiblock && ((IStructuralMultiblock) tile).canInterface(manager)) {
            return true;
        }
        return manager.isCompatible(tile);
    }

    protected FormationResult validateFrame(FormationProtocol<T> ctx, BlockPos pos, BlockState state, CasingType type, boolean needsFrame) {
        IMultiblockBase tile = structure.getTile(pos);
        // terminate if we encounter a node that already failed this tick
        if (!isFrameCompatible((TileEntity) tile) || (needsFrame && !type.isFrame())) {
            //If it is not a valid node or if it is supposed to be a frame but is invalid
            // then we are not valid over all
            return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_FRAME, pos);
        } else {
            if (tile instanceof IMultiblock) {
                @SuppressWarnings("unchecked")
                IMultiblock<T> multiblockTile = (IMultiblock<T>) tile;
                UUID uuid = multiblockTile.getCacheID();
                if (uuid != null && multiblockTile.getManager() == manager && multiblockTile.hasCache()) {
                    manager.updateCache(multiblockTile, multiblockTile.getMultiblock());
                    ctx.idsFound.add(uuid);
                }
            }
            ctx.locations.add(pos);
            if (type.isValve()) {
                ValveData data = new ValveData();
                data.location = pos;
                data.side = getSide(data.location);
                ctx.valves.add(data);
            }
        }
        return FormationResult.SUCCESS;
    }

    @Override
    public FormationResult postcheck(T structure, Set<BlockPos> innerNodes) {
        return FormationResult.SUCCESS;
    }

    protected StructureRequirement getStructureRequirement(BlockPos pos) {
        WallRelative relative = cuboid.getWallRelative(pos);
        if (relative.isOnEdge()) {
            return StructureRequirement.FRAME;
        }
        return relative.isWall() ? StructureRequirement.OTHER : StructureRequirement.INNER;
    }

    protected Direction getSide(BlockPos pos) {
        return cuboid.getSide(pos);
    }

    @Override
    public IShape getShape() {
        return cuboid;
    }

    @Override
    public boolean precheck() {
        cuboid = StructureHelper.fetchCuboid(structure, minBounds, maxBounds);
        return cuboid != null;
    }

    public void loadCuboid(VoxelCuboid cuboid) {
        this.cuboid = cuboid;
    }
}
