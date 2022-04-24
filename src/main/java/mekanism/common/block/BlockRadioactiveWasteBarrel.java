package mekanism.common.block;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityRadioactiveWasteBarrel;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.TextUtils;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BlockRadioactiveWasteBarrel extends BlockTileModel<TileEntityRadioactiveWasteBarrel, BlockTypeTile<TileEntityRadioactiveWasteBarrel>> {

    public BlockRadioactiveWasteBarrel() {
        super(MekanismBlockTypes.RADIOACTIVE_WASTE_BARREL);
    }

    @Nonnull
    @Override
    @Deprecated
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand,
          @Nonnull BlockHitResult hit) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        TileEntityRadioactiveWasteBarrel tile = WorldUtils.getTileEntity(TileEntityRadioactiveWasteBarrel.class, world, pos);
        if (tile == null) {
            return InteractionResult.PASS;
        } else if (!world.isClientSide()) {
            GasStack stored = tile.getGas();
            Component text;
            if (stored.isEmpty()) {
                text = MekanismLang.NO_GAS.translateColored(EnumColor.GRAY);
            } else {
                text = MekanismLang.STORED_MB_PERCENTAGE.translateColored(EnumColor.ORANGE, EnumColor.ORANGE, stored, EnumColor.GRAY,
                      TextUtils.format(stored.getAmount()), TextUtils.getPercent(tile.getGasScale()));
            }
            player.sendMessage(text, Util.NIL_UUID);
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }
}
