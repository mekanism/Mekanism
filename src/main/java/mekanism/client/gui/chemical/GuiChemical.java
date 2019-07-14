package mekanism.client.gui.chemical;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.common.tile.prefab.TileEntityElectricBlock;
import mekanism.common.util.MekanismUtils;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiChemical<TILE extends TileEntityElectricBlock> extends GuiMekanismTile<TILE> {

    protected GuiChemical(TILE tile, Container container) {
        super(tile, container);
    }

    protected abstract void drawForegroundText();

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        drawTexturedModalRect(guiLeft + 116, guiTop + 76, 176, 0, tileEntity.getScaledEnergyLevel(52), 4);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawForegroundText();
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 116 && xAxis <= 168 && yAxis >= 76 && yAxis <= 80) {
            displayTooltip(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}