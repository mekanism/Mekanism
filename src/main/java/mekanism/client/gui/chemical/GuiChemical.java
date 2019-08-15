package mekanism.client.gui.chemical;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiChemical<TILE extends TileEntityMekanism, CONTAINER extends Container> extends GuiMekanismTile<TILE, CONTAINER> {

    protected GuiChemical(TILE tile, CONTAINER container, PlayerInventory inv) {
        super(tile, container, inv);
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
            displayTooltip(EnergyDisplay.of(tileEntity.getEnergy(), tileEntity.getMaxEnergy()).getTextComponent(), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}