package mekanism.client.gui;

import mekanism.client.gui.element.GuiContainerEditMode;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.inventory.container.ContainerDynamicTank;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

@OnlyIn(Dist.CLIENT)
public class GuiDynamicTank extends GuiEmbeddedGaugeTile<TileEntityDynamicTank> {

    public GuiDynamicTank(PlayerInventory inventory, TileEntityDynamicTank tile) {
        super(tile, new ContainerDynamicTank(inventory, tile));
        addGuiElement(new GuiContainerEditMode(this, tileEntity, getGuiLocation()));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), (xSize / 2) - (font.getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        drawString(LangUtils.localize("container.inventory"), 8, (ySize - 94) + 2, 0x404040);
        drawString(LangUtils.localize("gui.volume") + ": " + tileEntity.clientCapacity / TankUpdateProtocol.FLUID_PER_TANK, 53, 26, 0x00CD00);
        FluidStack fluidStored = tileEntity.structure != null ? tileEntity.structure.fluidStored : null;
        if (fluidStored != null) {
            //TODO: Can these two be combined
            renderScaledText(TextComponentUtil.build(fluidStored, ":"), 53, 44, 0x00CD00, 74);
            drawString(fluidStored.amount + "mB", 53, 53, 0x00CD00);
        } else {
            renderScaledText(TextComponentUtil.build(Translation.of("mrekanism.gui.noFluid")), 53, 44, 0x00CD00, 74);
        }
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 7 && xAxis <= 39 && yAxis >= 14 && yAxis <= 72) {
            if (fluidStored != null) {
                displayTooltip(TextComponentUtil.build(fluidStored, ": " + fluidStored.amount + "mB"), xAxis, yAxis);
            } else {
                displayTooltip(TextComponentUtil.build(Translation.of("mekanism.gui.empty")), xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
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