package mekanism.common.block;

import com.google.common.cache.LoadingCache;
import javax.annotation.Nonnull;
import mekanism.common.block.basic.BlockResource;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPattern.PatternHelper;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

//TODO: Rename
public class PortalHelper {

    public static class Size extends NetherPortalBlock.Size {

        private int portalBlockCount;

        public Size(IWorld world, BlockPos pos, Axis axis) {
            super(world, pos, axis);
        }

        private boolean isFrame(BlockState state) {
            Block block = state.getBlock();
            if (block instanceof BlockResource) {
                return ((BlockResource) block).getResourceInfo() == BlockResourceInfo.REFINED_OBSIDIAN;
            }
            return block == Blocks.OBSIDIAN;
        }

        @Override
        protected int getDistanceUntilEdge(BlockPos pos, @Nonnull Direction facing) {
            int i;
            for (i = 0; i < 22; ++i) {
                BlockPos blockpos = pos.offset(facing, i);
                //TODO: func_196900_a -> isEmptyBlock
                if (!this.func_196900_a(this.world.getBlockState(blockpos)) || !isFrame(this.world.getBlockState(blockpos.down()))) {
                    break;
                }
            }
            return isFrame(this.world.getBlockState(pos.offset(facing, i))) ? i : 0;
        }

        @Override
        protected int calculatePortalHeight() {
            label56:
            for (this.height = 0; getHeight() < 21; ++this.height) {
                for (int i = 0; i < getWidth(); ++i) {
                    BlockPos blockpos = this.bottomLeft.offset(this.rightDir, i).up(getHeight());
                    BlockState blockState = this.world.getBlockState(blockpos);
                    Block block = blockState.getBlock();
                    if (!this.func_196900_a(blockState)) {
                        break label56;
                    }
                    if (block == Blocks.NETHER_PORTAL) {
                        ++this.portalBlockCount;
                    }
                    if (i == 0) {
                        if (!isFrame(this.world.getBlockState(blockpos.offset(this.leftDir)))) {
                            break label56;
                        }
                    } else if (i == getWidth() - 1) {
                        if (!isFrame(this.world.getBlockState(blockpos.offset(this.rightDir)))) {
                            break label56;
                        }
                    }
                }
            }

            for (int j = 0; j < getWidth(); ++j) {
                if (!isFrame(this.world.getBlockState(this.bottomLeft.offset(this.rightDir, j).up(getHeight())))) {
                    this.height = 0;
                    break;
                }
            }
            if (getHeight() <= 21 && getHeight() >= 3) {
                return getHeight();
            }
            this.bottomLeft = null;
            this.width = 0;
            this.height = 0;
            return 0;
        }
    }

    public static class BlockPortalOverride extends NetherPortalBlock {

        //TODO: Does it make sense to store this in MekanismBlock
        public static final BlockPortalOverride instance = new BlockPortalOverride();

        public BlockPortalOverride() {
            super(Block.Properties.create(Material.PORTAL).doesNotBlockMovement().tickRandomly().hardnessAndResistance(-1.0F).sound(SoundType.GLASS).lightValue(11).noDrops());
            setRegistryName(new ResourceLocation("minecraft", "nether_portal"));
        }

        @Nonnull
        @Override
        public PatternHelper createPatternHelper(@Nonnull IWorld world, BlockPos pos) {
            Axis axis = Axis.Z;
            PortalHelper.Size size = new PortalHelper.Size(world, pos, Axis.X);
            if (!size.isValid()) {
                axis = Axis.X;
                size = new PortalHelper.Size(world, pos, Direction.Axis.Z);
            }
            LoadingCache<BlockPos, CachedBlockInfo> loadingCache = BlockPattern.createLoadingCache(world, true);
            if (!size.isValid()) {
                return new PatternHelper(pos, Direction.NORTH, Direction.UP, loadingCache, 1, 1, 1);
            }
            int[] aint = new int[EnumUtils.AXIS_DIRECTIONS.length];
            Direction dir = MekanismUtils.getRight(size.rightDir);
            BlockPos blockpos = size.bottomLeft.up(size.getHeight() - 1);

            for (AxisDirection direction : EnumUtils.AXIS_DIRECTIONS) {
                PatternHelper patternHelper = new PatternHelper(dir.getAxisDirection() == direction ? blockpos : blockpos.offset(size.rightDir, size.getWidth() - 1),
                      Direction.getFacingFromAxis(direction, axis), Direction.UP, loadingCache, size.getWidth(), size.getHeight(), 1);

                for (int i = 0; i < size.getWidth(); ++i) {
                    for (int j = 0; j < size.getHeight(); ++j) {
                        if (patternHelper.translateOffset(i, j, 1).getBlockState().getMaterial() != Material.AIR) {
                            ++aint[direction.ordinal()];
                        }
                    }
                }
            }

            AxisDirection axisDirection = AxisDirection.POSITIVE;
            for (AxisDirection direction : EnumUtils.AXIS_DIRECTIONS) {
                if (aint[direction.ordinal()] < aint[axisDirection.ordinal()]) {
                    axisDirection = direction;
                }
            }
            return new PatternHelper(dir.getAxisDirection() == axisDirection ? blockpos : blockpos.offset(size.rightDir, size.getWidth() - 1),
                  Direction.getFacingFromAxis(axisDirection, axis), Direction.UP, loadingCache, size.getWidth(), size.getHeight(), 1);
        }

        @Override
        public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
            Axis axis = state.get(AXIS);
            if (axis == Axis.X || axis == Axis.Z) {
                PortalHelper.Size size = new PortalHelper.Size(world, pos, axis);
                if (!size.isValid() || size.portalBlockCount < size.getWidth() * size.getHeight()) {
                    world.removeBlock(pos, isMoving);
                }
            }
        }

        @Override
        public boolean trySpawnPortal(@Nonnull IWorld world, @Nonnull BlockPos pos) {
            return trySpawnPortal(world, pos, Axis.X) || trySpawnPortal(world, pos, Axis.Z);
        }

        private boolean trySpawnPortal(@Nonnull IWorld world, BlockPos pos, Axis axis) {
            PortalHelper.Size size = new PortalHelper.Size(world, pos, axis);
            if (size.isValid() && size.portalBlockCount == 0 && !ForgeEventFactory.onTrySpawnPortal(world, pos, size)) {
                size.placePortalBlocks();
                return true;
            }
            return false;
        }
    }

    public static class NeighborListener {

        public static final NeighborListener instance = new NeighborListener();

        public NeighborListener() {
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onNeighborNotify(NeighborNotifyEvent e) {
            if (e.getState().getBlock() == Blocks.OBSIDIAN) {
                IWorld world = e.getWorld();
                Block newBlock = world.getBlockState(e.getPos()).getBlock();
                if (newBlock instanceof FireBlock) {
                    BlockPortalOverride.instance.trySpawnPortal(world, e.getPos());
                }
            }
        }
    }
}