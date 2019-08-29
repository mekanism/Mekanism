package mekanism.api.gas;

import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.fluids.FluidAttributes;

//TODO: Should we let them actually flow
public class GaseousFluid extends FlowingFluid {

    private final FluidAttributes attributes;

    private final Gas gas;

    public GaseousFluid(Gas gas) {
        this.gas = gas;

        int tint = gas.getTint();
        //Fluids use ARGB so make sure that we are not using a fully transparent tint.
        // This fixes issues with some mods rendering our fluids as invisible
        if ((tint & 0xFF000000) == 0) {
            tint = 0xFF000000 | tint;
        }
        //TODO: Set bucket/block?
        attributes = FluidAttributes.builder(gas.getName(), gas.getIcon(), gas.getIcon())
              .gaseous().color(tint)
              .build();
        setRegistryName(gas.getRegistryName());
    }

    @Nonnull
    @Override
    public Fluid getFlowingFluid() {
        //TODO: Support registry replacement? Or is that not needed at all given that would override these methods anyways
        return this;
    }

    @Nonnull
    @Override
    public Fluid getStillFluid() {
        //TODO: Support registry replacement? Or is that not needed at all given that would override these methods anyways
        return this;
    }

    @Override
    protected boolean canSourcesMultiply() {
        return false;
    }

    @Override
    protected void beforeReplacingBlock(IWorld worldIn, @Nonnull BlockPos pos, BlockState state) {
        // copied from the WaterFluid implementation
        TileEntity tileentity = state.getBlock().hasTileEntity() ? worldIn.getTileEntity(pos) : null;
        Block.spawnDrops(state, worldIn.getWorld(), pos, tileentity);
    }

    @Override
    protected int getSlopeFindDistance(@Nonnull IWorldReader world) {
        //TODO
        return 0;
    }

    @Override
    protected int getLevelDecreasePerBlock(@Nonnull IWorldReader world) {
        return 0;
    }

    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Nonnull
    @Override
    public Item getFilledBucket() {
        //TODO: Support registry replacement? Or is that not needed at all given that would override these methods anyways
        //TODO: Register a bucket item
        return Items.AIR;//test_fluid_bucket;
    }

    @Override
    protected boolean func_215665_a(@Nonnull IFluidState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull Fluid fluid, @Nonnull Direction direction) {
        //TODO
        return false;//direction == Direction.DOWN && !fluid.isIn(FluidTags.WATER);
    }

    @Override
    public int getTickRate(@Nonnull IWorldReader world) {
        //TODO
        return 0;
    }

    @Override
    protected float getExplosionResistance() {
        //TODO
        return 1;
    }

    @Nonnull
    @Override
    protected BlockState getBlockState(@Nonnull IFluidState state) {
        //TODO: Support registry replacement? Or is that not needed at all given that would override these methods anyways
        //TODO: Make there be an actual fluid block
        return Blocks.AIR.getDefaultState();//test_fluid_block.getDefaultState();
    }

    @Override
    public boolean isSource(@Nonnull IFluidState state) {
        return true;
    }

    @Override
    public int getLevel(@Nonnull IFluidState state) {
        //TODO: Should this be 8?
        return 0;
    }

    @Override
    public boolean isEquivalentTo(Fluid other) {
        //TODO: Support registry replacement? Or is that not needed at all given that would override these methods anyways
        //TODO: Check if this should have extra handling for tags
        return this == other;
    }

    @Nonnull
    @Override
    protected FluidAttributes createAttributes(Fluid fluid) {
        return attributes;
    }
}