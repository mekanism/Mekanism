package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasModel;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.ThermalEvaporationControllerContainer;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class BlockThermalEvaporationController extends BlockMekanism implements IHasModel, IStateFacing, IStateActive, IHasInventory, IHasDescription,
      IHasGui<TileEntityThermalEvaporationController>, IHasTileEntity<TileEntityThermalEvaporationController> {

    public BlockThermalEvaporationController() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5F, 10F));
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (tile != null) {
                tile.onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    public float getExplosionResistance(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        return 9F;
    }

    @Nonnull
    @Override
    public ActionResultType func_225533_a_(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!player.func_225608_bj_()) {
            TileEntityThermalEvaporationController tile = MekanismUtils.getTileEntity(TileEntityThermalEvaporationController.class, world, pos);
            if (tile != null) {
                if (!world.isRemote) {
                    NetworkHooks.openGui((ServerPlayerEntity) player, getProvider(tile), pos);
                }
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityThermalEvaporationController tile) {
        return new ContainerProvider(getNameTextComponent(), (i, inv, player) -> new ThermalEvaporationControllerContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityThermalEvaporationController> getTileType() {
        return MekanismTileEntityTypes.THERMAL_EVAPORATION_CONTROLLER.getTileEntityType();
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return MekanismLang.DESCRIPTION_THERMAL_EVAPORATION_CONTROLLER;
    }
}