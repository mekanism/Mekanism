package mekanism.common.block.basic;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class BlockSecurityDesk extends BlockTileModel<TileEntitySecurityDesk, BlockTypeTile<TileEntitySecurityDesk>> {

    public BlockSecurityDesk() {
        super(MekanismBlockTypes.SECURITY_DESK);
    }

    @Override
    public void setTileData(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack, TileEntityMekanism tile) {
        if (tile instanceof TileEntitySecurityDesk desk && placer != null) {
            desk.ownerUUID = placer.getUUID();
        }
    }

    @Nonnull
    @Override
    @Deprecated
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand,
          @Nonnull BlockHitResult hit) {
        TileEntitySecurityDesk tile = WorldUtils.getTileEntity(TileEntitySecurityDesk.class, world, pos);
        if (tile != null && !player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                UUID ownerUUID = tile.ownerUUID;
                if (ownerUUID == null || player.getUUID().equals(ownerUUID)) {
                    NetworkHooks.openGui((ServerPlayer) player, Attribute.get(this, AttributeGui.class).getProvider(tile), pos);
                } else {
                    SecurityUtils.displayNoAccess(player);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}