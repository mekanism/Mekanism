package mekanism.client.gui.chemical;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiChemical<TILE extends TileEntityMekanism, CONTAINER extends MekanismTileContainer<TILE>> extends GuiMekanismTile<TILE, CONTAINER> {

    protected GuiChemical(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
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