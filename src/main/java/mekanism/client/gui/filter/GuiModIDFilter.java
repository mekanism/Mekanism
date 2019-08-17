package mekanism.client.gui.filter;

import mekanism.api.text.EnumColor;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiModIDFilter<FILTER extends IModIDFilter, TILE extends TileEntityMekanism, CONTAINER extends FilterContainer<TILE, FILTER>> extends GuiTextFilter<FILTER, TILE, CONTAINER> {

    protected GuiModIDFilter(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    protected abstract void updateStackList(String modName);

    @Override
    protected void setText() {
        String name = text.getText();
        if (name.isEmpty()) {
            status = TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.modIDFilter.noID"));
            return;
        } else if (name.equals(filter.getModID())) {
            status = TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.modIDFilter.sameID"));
            return;
        }
        updateStackList(name);
        filter.setModID(name);
        text.setText("");
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(TextComponentUtil.build(Translation.of(isNew ? "gui.new" : "gui.edit"), " " + Translation.of("gui.modIDFilter")), 43, 6, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("mekanism.gui.status"), ": ", status), 35, 20, 0x00CD00);
        renderScaledText(TextComponentUtil.build(Translation.of("mekanism.gui.id"), ": " + filter.getModID()), 35, 32, 0x00CD00, 107);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}