package mekanism.client.gui.filter;

import mekanism.api.EnumColor;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.LangUtils;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiItemStackFilter<FILTER extends IItemStackFilter, TILE extends TileEntityMekanism> extends GuiTypeFilter<FILTER, TILE> {

    protected GuiItemStackFilter(PlayerEntity player, TILE tile) {
        super(player, tile);
    }

    @Override
    public void tick() {
        super.tick();
        if (ticker > 0) {
            ticker--;
        } else {
            status = TextComponentUtil.build(EnumColor.DARK_GREEN, Translation.of("gui.allOK"));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString((isNew ? LangUtils.localize("gui.new") : LangUtils.localize("gui.edit")) + " " + LangUtils.localize("gui.itemFilter"), 43, 6, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("mekanism.gui.status"), ": ", status), 35, 20, 0x00CD00);
        drawString(TextComponentUtil.build(Translation.of("mekanism.gui.itemFilter.details"), ":"), 35, 32, 0x00CD00);
        drawForegroundLayer(mouseX, mouseY);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}