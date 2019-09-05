package mekanism.client.gui.filter;

import mekanism.api.text.EnumColor;
import mekanism.common.content.filter.IModIDFilter;
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
public abstract class GuiModIDFilter<FILTER extends IModIDFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>, CONTAINER extends
      FilterContainer<FILTER, TILE>> extends GuiTextFilter<FILTER, TILE, CONTAINER> {

    protected GuiModIDFilter(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    protected abstract void updateStackList(String modName);

    @Override
    protected void setText() {
        String name = text.getText();
        if (name.isEmpty()) {
            status = TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.mekanism.modIDFilter.noID"));
            return;
        } else if (name.equals(filter.getModID())) {
            status = TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.mekanism.modIDFilter.sameID"));
            return;
        }
        updateStackList(name);
        filter.setModID(name);
        text.setText("");
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(TextComponentUtil.build(Translation.of(isNew ? "gui.mekanism.new" : "gui.mekanism.edit"), " ", Translation.of("gui.mekanism.modIDFilter")), 43, 6, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("gui.mekanism.status"), ": ", status), 35, 20, 0x00CD00);
        renderScaledText(TextComponentUtil.build(Translation.of("gui.mekanism.id"), ": " + filter.getModID()), 35, 32, 0x00CD00, 107);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}