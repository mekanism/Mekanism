package mekanism.common.block.basic;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.block.states.IStateWaterLogged;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.SecurityDeskContainer;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.VoxelShapeUtils;
import mekanism.common.util.text.TextComponentUtil;
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
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class BlockSecurityDesk extends BlockMekanism implements IStateFacing, IHasGui<TileEntitySecurityDesk>, IHasInventory, IHasTileEntity<TileEntitySecurityDesk>,
      IStateWaterLogged, IHasDescription {

    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        VoxelShape desk = VoxelShapeUtils.combine(
              makeCuboidShape(0, 6, 0, 16, 13, 16),
              makeCuboidShape(0, 0, 0, 16, 5, 16),
              makeCuboidShape(1, 5, 1, 15, 6, 15),
              //Rough estimated of things that are at angles, so that we do not have overly complex shapes
              //keyboard
              makeCuboidShape(3, 13, 2, 13, 14, 7),
              //stand base
              makeCuboidShape(4, 13, 10, 12, 14, 14),
              //stand neck
              makeCuboidShape(7, 14, 13, 9, 15.5, 14),
              makeCuboidShape(7, 15.5, 12.875, 9, 17, 13.875),
              makeCuboidShape(7, 17, 12.75, 9, 18.5, 13.75),
              makeCuboidShape(7, 18.5, 12.625, 9, 20, 13.625),
              //monitor
              makeCuboidShape(1, 14.5, 9, 15, 15, 10),
              makeCuboidShape(1, 15, 8, 15, 16, 10.5),
              makeCuboidShape(1, 16, 8.5, 15, 17, 11),
              makeCuboidShape(1, 17, 9, 15, 18, 11.5),
              makeCuboidShape(1, 18, 9.5, 15, 19, 12),
              makeCuboidShape(1, 19, 10, 15, 20, 12.5),
              makeCuboidShape(1, 20, 10.5, 15, 21, 13),
              makeCuboidShape(1, 21, 11, 15, 22, 13.5),
              makeCuboidShape(1, 22, 11.5, 15, 23, 14),
              makeCuboidShape(1, 23, 12, 15, 24, 14.25),
              makeCuboidShape(1, 24, 12.5, 15, 24.5, 13),
              //monitor back
              makeCuboidShape(2, 16, 11, 14, 17, 12),
              makeCuboidShape(2, 17, 11.5, 14, 18, 12.5),
              makeCuboidShape(2, 18, 12, 14, 19, 13),
              makeCuboidShape(2, 19, 12.5, 14, 20, 13.5),
              makeCuboidShape(2, 20, 13, 14, 21.5, 14)
        );
        for (Direction side : EnumUtils.HORIZONTAL_DIRECTIONS) {
            bounds[side.ordinal() - 2] = VoxelShapeUtils.rotateHorizontal(desk, side);
        }
    }

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

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return bounds[getDirection(state).ordinal() - 2];
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

    @Nonnull
    @Override
    public ActionResultType func_225533_a_(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntitySecurityDesk tile = MekanismUtils.getTileEntity(TileEntitySecurityDesk.class, world, pos);
        //TODO
        if (tile != null) {
            if (!player.func_225608_bj_()) {
                if (!world.isRemote) {
                    UUID ownerUUID = tile.ownerUUID;
                    if (ownerUUID == null || player.getUniqueID().equals(ownerUUID)) {
                        NetworkHooks.openGui((ServerPlayerEntity) player, getProvider(tile), pos);
                    } else {
                        SecurityUtils.displayNoAccess(player);
                    }
                }
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntitySecurityDesk tile) {
        return new ContainerProvider(TextComponentUtil.translate(getTranslationKey()), (i, inv, player) -> new SecurityDeskContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntitySecurityDesk> getTileType() {
        return MekanismTileEntityTypes.SECURITY_DESK.getTileEntityType();
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return MekanismLang.DESCRIPTION_SECURITY_DESK;
    }
}