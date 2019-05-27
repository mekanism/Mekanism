package mekanism.client.gui;

import mekanism.client.gui.element.GuiContainerEditMode;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.inventory.container.ContainerDynamicTank;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiDynamicTank extends GuiEmbeddedGaugeTile<TileEntityDynamicTank> {

    public GuiDynamicTank(InventoryPlayer inventory, TileEntityDynamicTank tile) {
        super(tile, new ContainerDynamicTank(inventory, tile));
        addGuiElement(new GuiContainerEditMode(this, tileEntity, getGuiLocation()));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(tileEntity.getName(), (xSize / 2) - (fontRenderer.getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 94) + 2, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.volume") + ": " + tileEntity.clientCapacity / TankUpdateProtocol.FLUID_PER_TANK, 53, 26, 0x00CD00);
        renderScaledText(tileEntity.structure.fluidStored != null ? LangUtils.localizeFluidStack(tileEntity.structure.fluidStored)
                                                                    + ":" : LangUtils.localize("gui.noFluid"), 53, 44, 0x00CD00, 74);
        if (tileEntity.structure.fluidStored != null) {
            fontRenderer.drawString(tileEntity.structure.fluidStored.amount + "mB", 53, 53, 0x00CD00);
        }
        int xAxis = mouseX - (width - xSize) / 2;
        int yAxis = mouseY - (height - ySize) / 2;
        if (xAxis >= 7 && xAxis <= 39 && yAxis >= 14 && yAxis <= 72) {
            drawHoveringText(tileEntity.structure.fluidStored != null
                             ? LangUtils.localizeFluidStack(tileEntity.structure.fluidStored) + ": " + tileEntity.structure.fluidStored.amount + "mB"
                             : LangUtils.localize("gui.empty"), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
        mc.renderEngine.bindTexture(getGuiLocation());
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int scaledFluidLevel = tileEntity.getScaledFluidLevel(58);
        if (scaledFluidLevel > 0) {
            displayGauge(7, 14, scaledFluidLevel, tileEntity.structure.fluidStored, 0);
            displayGauge(23, 14, scaledFluidLevel, tileEntity.structure.fluidStored, 1);
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiDynamicTank.png");
    }

    @Override
    protected ResourceLocation getGaugeResource() {
        return getGuiLocation();
    }
}