package mekanism.generators.common.block;

import buildcraft.api.tools.IToolWrench;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import mekanism.common.tile.prefab.TileEntityElectricBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorsBlocks;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.block.states.BlockStateReactor;
import mekanism.generators.common.block.states.BlockStateReactor.ReactorBlock;
import mekanism.generators.common.block.states.BlockStateReactor.ReactorBlockType;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.reactor.TileEntityReactorPort;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockReactor extends Block implements ITileEntityProvider {

    public BlockReactor() {
        super(Material.IRON);
        setHardness(3.5F);
        setResistance(8F);
        setCreativeTab(Mekanism.tabMekanism);
    }

    public static BlockReactor getReactorBlock(ReactorBlock block) {
        return new BlockReactor() {
            @Override
            public ReactorBlock getReactorBlock() {
                return block;
            }
        };
    }

    public abstract ReactorBlock getReactorBlock();

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity tile = MekanismUtils.getTileEntitySafe(worldIn, pos);

        if (tile instanceof TileEntityReactorController) {
            state = state.withProperty(BlockStateReactor.activeProperty, ((IActiveState) tile).getActive());
        }

        if (tile instanceof TileEntityReactorPort) {
            state = state.withProperty(BlockStateReactor.activeProperty, ((TileEntityReactorPort) tile).fluidEject);
        }

        return state;
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateReactor(this, getTypeProperty());
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        ReactorBlockType type = ReactorBlockType.get(getReactorBlock(), meta & 0xF);

        return getDefaultState().withProperty(getTypeProperty(), type);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        ReactorBlockType type = state.getValue(getTypeProperty());
        return type.meta;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getBlock().getMetaFromState(state);
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock,
          BlockPos neighborPos) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);

            if (tileEntity instanceof TileEntityBasicBlock) {
                ((TileEntityBasicBlock) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer,
          EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        TileEntityElectricBlock tileEntity = (TileEntityElectricBlock) world.getTileEntity(pos);
        int metadata = state.getBlock().getMetaFromState(state);
        ItemStack stack = entityplayer.getHeldItem(hand);

        if (!stack.isEmpty()) {
            if (MekanismUtils.isBCWrench(stack.getItem()) && !stack.getTranslationKey().contains("omniwrench")) {
                if (entityplayer.isSneaking()) {
                    dismantleBlock(world, pos, false);
                    return true;
                }

                ((IToolWrench) stack.getItem()).wrenchUsed(entityplayer, hand, stack,
                      new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos));

                return true;
            }
        }

        if (tileEntity instanceof TileEntityReactorController) {
            if (!entityplayer.isSneaking()) {
                entityplayer.openGui(MekanismGenerators.instance, ReactorBlockType.get(this, metadata).guiId, world,
                      pos.getX(), pos.getY(), pos.getZ());
                return true;
            }
        }

        if (tileEntity instanceof TileEntityReactorLogicAdapter) {
            if (!entityplayer.isSneaking()) {
                entityplayer.openGui(MekanismGenerators.instance,
                      BlockStateReactor.ReactorBlockType.get(this, metadata).guiId, world, pos.getX(), pos.getY(),
                      pos.getZ());
                return true;
            }
        }

        return false;
    }

    @Override
    public void getSubBlocks(CreativeTabs creativetabs, NonNullList<ItemStack> list) {
        for (BlockStateReactor.ReactorBlockType type : BlockStateReactor.ReactorBlockType.values()) {
            if (type.blockType == getReactorBlock()) {
                list.add(new ItemStack(this, 1, type.meta));
            }
        }
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        int metadata = state.getBlock().getMetaFromState(state);

        if (ReactorBlockType.get(getReactorBlock(), metadata) == null) {
            return null;
        }

        return ReactorBlockType.get(getReactorBlock(), metadata).create();
    }

    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return this == GeneratorsBlocks.Reactor ? BlockRenderLayer.CUTOUT : BlockRenderLayer.TRANSLUCENT;
    }

    @Nonnull
    @Override
    @Deprecated
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    /*This method is not used, metadata manipulation is required to create a Tile Entity.*/
    @Override
    public TileEntity createNewTileEntity(@Nonnull World world, int meta) {
        return null;
    }

    @Override
    @Deprecated
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
          EnumFacing side) {
        int meta = state.getBlock().getMetaFromState(state);
        ReactorBlockType type = ReactorBlockType.get(getReactorBlock(), meta);

        if (type == ReactorBlockType.REACTOR_GLASS || type == ReactorBlockType.LASER_FOCUS_MATRIX) {
            IBlockState stateOffset = world.getBlockState(pos.offset(side));
            if (this == stateOffset.getBlock()) {
                int metaOffset = stateOffset.getBlock().getMetaFromState(stateOffset);
                ReactorBlockType typeOffset = ReactorBlockType.get(getReactorBlock(), metaOffset);
                if (typeOffset == ReactorBlockType.REACTOR_GLASS || typeOffset == ReactorBlockType.LASER_FOCUS_MATRIX) {
                    return false;
                }
            }
        }

        return super.shouldSideBeRendered(state, world, pos, side);
    }

    @Override
    @Deprecated
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity tile = MekanismUtils.getTileEntitySafe(world, pos);

        if (tile instanceof TileEntityReactorLogicAdapter) {
            return ((TileEntityReactorLogicAdapter) tile).checkMode() ? 15 : 0;
        }

        return 0;
    }

    @Override
    @Deprecated
    public boolean isSideSolid(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side) {
        ReactorBlockType type = ReactorBlockType.get(getReactorBlock(), state.getBlock().getMetaFromState(state));

        switch (type) {
            case REACTOR_FRAME:
            case REACTOR_PORT:
            case REACTOR_LOGIC_ADAPTER:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        ReactorBlockType type = BlockStateReactor.ReactorBlockType.get(this, state.getBlock().getMetaFromState(state));

        return type == ReactorBlockType.REACTOR_LOGIC_ADAPTER;
    }

    public ItemStack dismantleBlock(World world, BlockPos pos, boolean returnBlock) {
        IBlockState state = world.getBlockState(pos);
        ItemStack itemStack = new ItemStack(this, 1, state.getBlock().getMetaFromState(state));

        world.setBlockToAir(pos);

        if (!returnBlock) {
            float motion = 0.7F;
            double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

            EntityItem entityItem = new EntityItem(world, pos.getX() + motionX, pos.getY() + motionY,
                  pos.getZ() + motionZ, itemStack);

            world.spawnEntity(entityItem);
        }

        return itemStack;
    }

    public PropertyEnum<ReactorBlockType> getTypeProperty() {
        return getReactorBlock().getProperty();
    }
}
