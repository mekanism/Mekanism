package mekanism.common.item.block;

import java.util.List;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemBlockSecurityDesk extends ItemBlockTooltip<BlockTileModel<TileEntitySecurityDesk, BlockTypeTile<TileEntitySecurityDesk>>> {

    public ItemBlockSecurityDesk(BlockTileModel<TileEntitySecurityDesk, BlockTypeTile<TileEntitySecurityDesk>> block) {
        super(block);
    }

    @Override
    protected void addDetails(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        //Note: We manually override this as we don't want to display the security mode for the security desk as while it technically
        // has one in reality it is always private
        IItemSecurityUtils.INSTANCE.addOwnerTooltip(stack, tooltip);
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.hasInventory(stack)));
    }
}
