package mekanism.client.gui.filter;

import mekanism.api.EnumColor;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.common.content.filter.IFilter;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.LangUtils;
import net.minecraft.entity.player.EntityPlayer;

public abstract class GuiTypeFilter<FILTER extends IFilter, TILE extends TileEntityContainerBlock> extends GuiFilter<TILE> {

    protected String status = EnumColor.DARK_GREEN + LangUtils.localize("gui.allOK");
    protected boolean isNew = false;
    protected FILTER origFilter;
    protected FILTER filter;
    protected int ticker;

    protected GuiTypeFilter(EntityPlayer player, TILE tile) {
        super(player, tile);
    }

    protected void drawItemStackBackground(int guiWidth, int guiHeight, int xAxis, int yAxis) {
    }

    @Override
    public void initGui() {
        super.initGui();
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        buttonList.clear();
        addButtons(guiWidth, guiHeight);
        if (isNew) {
            buttonList.get(1).enabled = false;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(getGuiLocation());
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight);
        int xAxis = mouseX - guiWidth;
        int yAxis = mouseY - guiHeight;
        drawTexturedModalRect(guiWidth + 5, guiHeight + 5, 176, xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16, 11);
        if (tileEntity instanceof TileEntityDigitalMiner) {
            drawTexturedModalRect(guiWidth + 148, guiHeight + 45, 199, xAxis >= 148 && xAxis <= 162 && yAxis >= 45 && yAxis <= 59, 11);
            if (xAxis >= 149 && xAxis <= 165 && yAxis >= 19 && yAxis <= 35) {
                MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).disableLighting().disableDepth().colorMaskAlpha();
                int x = guiWidth + 149;
                int y = guiHeight + 19;
                drawRect(x, y, x + 16, y + 16, 0x80FFFFFF);
                renderHelper.cleanup();
            }
        } else if (tileEntity instanceof TileEntityLogisticalSorter) {
            drawTexturedModalRect(guiWidth + 11, guiHeight + 64, 198, xAxis >= 11 && xAxis <= 22 && yAxis >= 64 && yAxis <= 75, 11);
        }
        //Draw the itemstack specific background
        drawItemStackBackground(guiWidth, guiHeight, xAxis, yAxis);

        if (xAxis >= 12 && xAxis <= 28 && yAxis >= 19 && yAxis <= 35) {
            MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).disableLighting().disableDepth().colorMaskAlpha();
            int x = guiWidth + 12;
            int y = guiHeight + 19;
            drawRect(x, y, x + 16, y + 16, 0x80FFFFFF);
            renderHelper.cleanup();
        }
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    }
}