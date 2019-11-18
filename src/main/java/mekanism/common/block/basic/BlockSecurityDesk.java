package mekanism.common.block.basic;

import java.util.UUID;
import javax.annotation.Nullable;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.SecurityDeskContainer;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class BlockSecurityDesk extends BlockTileDrops implements IStateFacing, IHasGui<TileEntitySecurityDesk>, IHasInventory, IHasTileEntity<TileEntitySecurityDesk> {

    public BlockSecurityDesk() {
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
    public void setTileData(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, TileEntityMekanism tile) {
        if (tile instanceof TileEntitySecurityDesk) {
            ((TileEntitySecurityDesk) tile).ownerUUID = placer.getUniqueID();
        }
    }

    @Override
    public float getExplosionResistance(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        return 9F;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntitySecurityDesk tile = MekanismUtils.getTileEntity(TileEntitySecurityDesk.class, world, pos);
        //TODO
        if (tile != null) {
            if (!player.isSneaking()) {
                if (!world.isRemote) {
                    UUID ownerUUID = tile.ownerUUID;
                    if (ownerUUID == null || player.getUniqueID().equals(ownerUUID)) {
                        NetworkHooks.openGui((ServerPlayerEntity) player, getProvider(tile), pos);
                    } else {
                        SecurityUtils.displayNoAccess(player);
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntitySecurityDesk tile) {
        return new ContainerProvider("mekanism.container.security_desk", (i, inv, player) -> new SecurityDeskContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntitySecurityDesk> getTileType() {
        return MekanismTileEntityTypes.SECURITY_DESK;
    }
}