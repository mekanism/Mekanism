package mekanism.common.block;

import com.google.common.cache.LoadingCache;
import javax.annotation.Nonnull;
import mekanism.common.block.basic.BlockResource;
import mekanism.common.resource.BlockResourceInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.pattern.BlockPattern.PatternHelper;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

//TODO: Rename
public class PortalHelper {

    public static class Size extends BlockPortal.Size {

        private int portalBlockCount;

        public Size(World world, BlockPos pos, Axis axis) {
            super(world, pos, axis);
        }

        private boolean isFrame(IBlockState state) {
            Block block = state.getBlock();
            BlockResourceInfo resourceInfo = null;
            if (block instanceof BlockResource) {
                resourceInfo = ((BlockResource) block).getResourceInfo();
            }
            return block == Blocks.OBSIDIAN || resourceInfo == BlockResourceInfo.REFINED_OBSIDIAN;
        }

        @Override
        protected int getDistanceUntilEdge(BlockPos pos, @Nonnull EnumFacing facing) {
            int i;
            for (i = 0; i < 22; ++i) {
                BlockPos blockpos = pos.offset(facing, i);
                if (!this.isEmptyBlock(this.world.getBlockState(blockpos).getBlock()) || !isFrame(this.world.getBlockState(blockpos.down()))) {
                    break;
                }
            }
            return isFrame(this.world.getBlockState(pos.offset(facing, i))) ? i : 0;
        }

        @Override
        protected int calculatePortalHeight() {
            label56:
            for (this.height = 0; this.height < 21; ++this.height) {
                for (int i = 0; i < this.width; ++i) {
                    BlockPos blockpos = this.bottomLeft.offset(this.rightDir, i).up(this.height);
                    Block block = this.world.getBlockState(blockpos).getBlock();
                    if (!this.isEmptyBlock(block)) {
                        break label56;
                    }
                    if (block == Blocks.PORTAL) {
                        ++this.portalBlockCount;
                    }
                    if (i == 0) {
                        if (!isFrame(this.world.getBlockState(blockpos.offset(this.leftDir)))) {
                            break label56;
                        }
                    } else if (i == this.width - 1) {
                        if (!isFrame(this.world.getBlockState(blockpos.offset(this.rightDir)))) {
                            break label56;
                        }
                    }
                }
            }

            for (int j = 0; j < this.width; ++j) {
                if (!isFrame(this.world.getBlockState(this.bottomLeft.offset(this.rightDir, j).up(this.height)))) {
                    this.height = 0;
                    break;
                }
            }
            if (this.height <= 21 && this.height >= 3) {
                return this.height;
            }
            this.bottomLeft = null;
            this.width = 0;
            this.height = 0;
            return 0;
        }
    }

    public static class BlockPortalOverride extends BlockPortal {

        public static final BlockPortalOverride instance = new BlockPortalOverride();

        public BlockPortalOverride() {
            super();
            setRegistryName(new ResourceLocation("minecraft", "portal"));
            setHardness(-1.0F);
            setSoundType(SoundType.GLASS);
            setLightLevel(0.75F);
        }

        @Nonnull
        @Override
        public PatternHelper createPatternHelper(@Nonnull World world, BlockPos pos) {
            Axis axis = Axis.Z;
            PortalHelper.Size size = new PortalHelper.Size(world, pos, Axis.X);
            if (!size.isValid()) {
                axis = Axis.X;
                size = new PortalHelper.Size(world, pos, EnumFacing.Axis.Z);
            }
            LoadingCache<BlockPos, BlockWorldState> loadingCache = BlockPattern.createLoadingCache(world, true);
            if (!size.isValid()) {
                return new PatternHelper(pos, EnumFacing.NORTH, EnumFacing.UP, loadingCache, 1, 1, 1);
            }
            int[] aint = new int[AxisDirection.values().length];
            EnumFacing enumfacing = size.rightDir.rotateYCCW();
            BlockPos blockpos = size.bottomLeft.up(size.getHeight() - 1);

            for (AxisDirection direction : AxisDirection.values()) {
                PatternHelper patternHelper = new PatternHelper(enumfacing.getAxisDirection() == direction ? blockpos : blockpos.offset(size.rightDir, size.getWidth() - 1),
                      EnumFacing.getFacingFromAxis(direction, axis), EnumFacing.UP, loadingCache, size.getWidth(), size.getHeight(), 1);

                for (int i = 0; i < size.getWidth(); ++i) {
                    for (int j = 0; j < size.getHeight(); ++j) {
                        if (patternHelper.translateOffset(i, j, 1).getBlockState().getMaterial() != Material.AIR) {
                            ++aint[direction.ordinal()];
                        }
                    }
                }
            }

            AxisDirection axisDirection = AxisDirection.POSITIVE;
            for (AxisDirection direction : AxisDirection.values()) {
                if (aint[direction.ordinal()] < aint[axisDirection.ordinal()]) {
                    axisDirection = direction;
                }
            }
            return new PatternHelper(enumfacing.getAxisDirection() == axisDirection ? blockpos : blockpos.offset(size.rightDir, size.getWidth() - 1),
                  EnumFacing.getFacingFromAxis(axisDirection, axis), EnumFacing.UP, loadingCache, size.getWidth(), size.getHeight(), 1);
        }

        @Override
        public void neighborChanged(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, Block blockIn, BlockPos fromPos) {
            Axis axis = state.getValue(AXIS);
            if (axis == Axis.X || axis == Axis.Z) {
                PortalHelper.Size size = new PortalHelper.Size(world, pos, axis);
                if (!size.isValid() || size.portalBlockCount < size.width * size.height) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState());
                }
            }
        }

        @Override
        public boolean trySpawnPortal(@Nonnull World world, BlockPos pos) {
            return trySpawnPortal(world, pos, Axis.X) || trySpawnPortal(world, pos, Axis.Z);
        }

        private boolean trySpawnPortal(@Nonnull World world, BlockPos pos, Axis axis) {
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
                World world = e.getWorld();
                Block newBlock = world.getBlockState(e.getPos()).getBlock();
                if (newBlock instanceof BlockFire) {
                    BlockPortalOverride.instance.trySpawnPortal(world, e.getPos());
                }
            }
        }
    }
}