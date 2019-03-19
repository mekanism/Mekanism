package mekanism.client.gui;

import mekanism.client.gui.element.GuiContainerEditMode;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.inventory.container.ContainerDynamicTank;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiDynamicTank extends GuiMekanismTile<TileEntityDynamicTank> {

    public GuiDynamicTank(InventoryPlayer inventory, TileEntityDynamicTank tile) {
        super(tile, new ContainerDynamicTank(inventory, tile));
        addGuiElement(new GuiContainerEditMode(this, tileEntity, getGuiLocation()));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer
              .drawString(tileEntity.getName(), (xSize / 2) - (fontRenderer.getStringWidth(tileEntity.getName()) / 2),
                    6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 94) + 2, 0x404040);
        fontRenderer.drawString(
              LangUtils.localize("gui.volume") + ": " + tileEntity.clientCapacity / TankUpdateProtocol.FLUID_PER_TANK,
              53, 26, 0x00CD00);
        renderScaledText(
              tileEntity.structure.fluidStored != null ? LangUtils.localizeFluidStack(tileEntity.structure.fluidStored)
                    + ":" : LangUtils.localize("gui.noFluid"), 53, 44, 0x00CD00, 74);
        if (tileEntity.structure.fluidStored != null) {
            fontRenderer.drawString(tileEntity.structure.fluidStored.amount + "mB", 53, 53, 0x00CD00);
        }
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        if (xAxis >= 7 && xAxis <= 39 && yAxis >= 14 && yAxis <= 72) {
            drawHoveringText(tileEntity.structure.fluidStored != null ?
                        LangUtils.localizeFluidStack(tileEntity.structure.fluidStored) + ": "
                              + tileEntity.structure.fluidStored.amount + "mB" : LangUtils.localize("gui.empty"), xAxis,
                  yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
        mc.renderEngine.bindTexture(getGuiLocation());
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        if (tileEntity.getScaledFluidLevel(58) > 0) {
            displayGauge(7, 14, tileEntity.getScaledFluidLevel(58), tileEntity.structure.fluidStored, 0);
            displayGauge(23, 14, tileEntity.getScaledFluidLevel(58), tileEntity.structure.fluidStored, 1);
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiDynamicTank.png");
    }

    public void displayGauge(int xPos, int yPos, int scale, FluidStack fluid, int side /*0-left, 1-right*/) {
        if (fluid == null) {
            return;
        }

        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;

        int start = 0;

        while (true) {
            int renderRemaining;

            if (scale > 16) {
                renderRemaining = 16;
                scale -= 16;
            } else {
                renderRemaining = scale;
                scale = 0;
            }

            mc.renderEngine.bindTexture(MekanismRenderer.getBlocksTexture());
            drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos + 58 - renderRemaining - start,
                  MekanismRenderer.getFluidTexture(fluid.getFluid(), FluidType.STILL), 16, 16 - (16 - renderRemaining));
            start += 16;

            if (renderRemaining == 0 || scale == 0) {
                break;
            }
        }

        mc.renderEngine.bindTexture(getGuiLocation());
        drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos, 176, side == 0 ? 0 : 54, 16, 54);
    }
}