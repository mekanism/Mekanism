package mekanism.client.gui.filter;

import mekanism.api.text.EnumColor;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiItemStackFilter<FILTER extends IItemStackFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>, CONTAINER extends
      FilterContainer<FILTER, TILE>> extends GuiTypeFilter<FILTER, TILE, CONTAINER> {

    protected GuiItemStackFilter(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
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
        drawString(TextComponentUtil.build(Translation.of(isNew ? "gui.new" : "gui.edit"), " " + Translation.of("gui.itemFilter")), 43, 6, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("gui.mekanism.status"), ": ", status), 35, 20, 0x00CD00);
        drawString(TextComponentUtil.build(Translation.of("gui.mekanism.itemFilter.details"), ":"), 35, 32, 0x00CD00);
        drawForegroundLayer(mouseX, mouseY);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}