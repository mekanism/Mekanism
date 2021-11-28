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
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class BlockRadioactiveWasteBarrel extends BlockTileModel<TileEntityRadioactiveWasteBarrel, BlockTypeTile<TileEntityRadioactiveWasteBarrel>> {

    public BlockRadioactiveWasteBarrel() {
        super(MekanismBlockTypes.RADIOACTIVE_WASTE_BARREL);
    }

    @Nonnull
    @Override
    @Deprecated
    public ActionResultType use(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand,
          @Nonnull BlockRayTraceResult hit) {
        if (!player.isShiftKeyDown()) {
            return ActionResultType.PASS;
        }
        TileEntityRadioactiveWasteBarrel tile = WorldUtils.getTileEntity(TileEntityRadioactiveWasteBarrel.class, world, pos);
        if (tile == null) {
            return ActionResultType.PASS;
        } else if (!world.isClientSide()) {
            GasStack stored = tile.getGas();
            ITextComponent text;
            if (stored.isEmpty()) {
                text = MekanismLang.NO_GAS.translateColored(EnumColor.GRAY);
            } else {
                text = MekanismLang.STORED_MB_PERCENTAGE.translateColored(EnumColor.ORANGE, EnumColor.ORANGE, stored, EnumColor.GRAY,
                      TextUtils.format(stored.getAmount()), TextUtils.getPercent(tile.getGasScale()));
            }
            player.sendMessage(text, Util.NIL_UUID);
        }
        return ActionResultType.SUCCESS;
    }
}
