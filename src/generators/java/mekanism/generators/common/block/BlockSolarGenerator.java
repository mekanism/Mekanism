package mekanism.generators.common.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockMekanismContainer;
import mekanism.common.block.interfaces.IBlockDisableable;
import mekanism.common.block.interfaces.IBlockElectric;
import mekanism.common.block.interfaces.IBlockSound;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasSecurity;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

public class BlockSolarGenerator extends BlockMekanismContainer implements IHasGui, IBlockElectric, IHasInventory, IHasSecurity, IBlockSound,
      IHasTileEntity<TileEntitySolarGenerator>, IBlockDisableable {

    private static final AxisAlignedBB SOLAR_BOUNDS = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.7F, 1.0F);
    private static final SoundEvent SOUND_EVENT = new SoundEvent(new ResourceLocation(Mekanism.MODID, "tile.gen.solar"));

    private BooleanValue enabledReference;

    public BlockSolarGenerator() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 8F));
        setRegistryName(new ResourceLocation(MekanismGenerators.MODID, "solar_generator"));
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            final TileEntity tileEntity = MekanismUtils.getTileEntity(world, pos);
            if (tileEntity instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(BlockState state, @Nonnull PlayerEntity player, @Nonnull World world, @Nonnull BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return SecurityUtils.canAccess(player, tile) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isRemote) {
            return true;
        }
        TileEntityMekanism tileEntity = (TileEntityMekanism) world.getTileEntity(pos);
        if (tileEntity.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return true;
        }
        if (tileEntity.openGui(player)) {
            return true;
        }
        return false;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        return new TileEntitySolarGenerator();
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Nonnull
    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(BlockState state, IBlockReader world, BlockPos pos) {
        return SOLAR_BOUNDS;
    }

    @Override
    public int getGuiID() {
        return 1;
    }

    @Override
    public double getStorage() {
        return 96000;
    }

    @Override
    public int getInventorySize() {
        return 1;
    }

    @Nullable
    @Override
    public Class<? extends TileEntitySolarGenerator> getTileClass() {
        return TileEntitySolarGenerator.class;
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return SOUND_EVENT;
    }

    @Override
    public boolean isEnabled() {
        return enabledReference == null ? true : enabledReference.get();
    }

    @Override
    public void setEnabledConfigReference(BooleanValue enabledReference) {
        this.enabledReference = enabledReference;
    }
}