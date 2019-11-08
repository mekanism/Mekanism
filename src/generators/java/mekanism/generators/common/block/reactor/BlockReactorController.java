package mekanism.generators.common.block.reactor;

import mekanism.api.block.IBlockElectric;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.states.IStateActive;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.inventory.container.reactor.ReactorControllerContainer;
import mekanism.generators.common.tile.GeneratorsTileEntityTypes;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class BlockReactorController extends BlockTileDrops implements IHasGui<TileEntityReactorController>, IStateActive, IBlockElectric, IHasInventory, IHasTileEntity<TileEntityReactorController> {

    public BlockReactorController() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 8F));
        setRegistryName(new ResourceLocation(MekanismGenerators.MODID, "reactor_controller"));
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
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isRemote) {
            return true;
        }
        TileEntityMekanism tileEntity = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tileEntity == null) {
            return false;
        }
        if (tileEntity.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return true;
        }
        return tileEntity.openGui(player);
    }

    @Override
    public double getStorage() {
        return 1_000_000_000;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityReactorController tile) {
        return new ContainerProvider("mekanismgenerators.container.reactor_controller", (i, inv, player) -> new ReactorControllerContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityReactorController> getTileType() {
        return GeneratorsTileEntityTypes.REACTOR_CONTROLLER;
    }
}