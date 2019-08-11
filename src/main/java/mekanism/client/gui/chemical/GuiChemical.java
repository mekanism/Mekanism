package mekanism.client.gui.chemical;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.inventory.container.Container;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiChemical<TILE extends TileEntityMekanism> extends GuiMekanismTile<TILE> {

    protected GuiChemical(TILE tile, Container container) {
        super(tile, container);
    }

    protected abstract void drawForegroundText();

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        drawTexturedRect(guiLeft + 116, guiTop + 76, 176, 0, tileEntity.getScaledEnergyLevel(52), 4);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawForegroundText();
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 116 && xAxis <= 168 && yAxis >= 76 && yAxis <= 80) {
            displayTooltip(TextComponentUtil.build(EnergyDisplay.of(tileEntity.getEnergy(), tileEntity.getMaxEnergy())), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}