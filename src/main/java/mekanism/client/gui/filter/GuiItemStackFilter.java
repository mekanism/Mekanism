package mekanism.client.gui.filter;

import mekanism.api.EnumColor;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.LangUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiItemStackFilter<FILTER extends IItemStackFilter, TILE extends TileEntityContainerBlock> extends GuiTypeFilter<FILTER, TILE> {

    protected GuiItemStackFilter(EntityPlayer player, TILE tile) {
        super(player, tile);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (ticker > 0) {
            ticker--;
        } else {
            status = EnumColor.DARK_GREEN + LangUtils.localize("gui.allOK");
        }
    }
}