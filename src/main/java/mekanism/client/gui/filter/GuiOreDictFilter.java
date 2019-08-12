package mekanism.client.gui.filter;

import mekanism.api.text.EnumColor;
import mekanism.common.content.filter.IOreDictFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiOreDictFilter<FILTER extends IOreDictFilter, TILE extends TileEntityMekanism> extends GuiTextFilter<FILTER, TILE> {

    protected GuiOreDictFilter(PlayerEntity player, TILE tile) {
        super(player, tile);
    }

    protected abstract void updateStackList(String oreName);

    @Override
    protected void setText() {
        String name = text.getText();
        if (name.isEmpty()) {
            status = TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.oredictFilter.noKey"));
            return;
        } else if (name.equals(filter.getOreDictName())) {
            status = TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.oredictFilter.sameKey"));
            return;
        }
        updateStackList(name);
        filter.setOreDictName(name);
        text.setText("");
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(TextComponentUtil.build(Translation.of(isNew ? "gui.new" : "gui.edit"), " " + Translation.of("gui.oredictFilter")), 43, 6, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("mekanism.gui.status"), ": ", status), 35, 20, 0x00CD00);
        renderScaledText(TextComponentUtil.build(Translation.of("gui.key"), ": " + filter.getOreDictName()), 35, 32, 0x00CD00, 107);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}