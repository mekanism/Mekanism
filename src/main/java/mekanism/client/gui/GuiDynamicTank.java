package mekanism.client.gui;

import mekanism.client.gui.element.GuiContainerEditMode;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.inventory.container.tile.DynamicTankContainer;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public class GuiDynamicTank extends GuiEmbeddedGaugeTile<TileEntityDynamicTank, DynamicTankContainer> {

    public GuiDynamicTank(DynamicTankContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiContainerEditMode(this, tile, getGuiLocation()));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (xSize / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 94) + 2, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("gui.mekanism.volume"), ": " + tile.clientCapacity / TankUpdateProtocol.FLUID_PER_TANK), 53, 26, 0x00CD00);
        //TODO: 1.14 Convert to GuiElement
        FluidStack fluidStored = tile.structure != null ? tile.structure.fluidStored : FluidStack.EMPTY;
        if (fluidStored.isEmpty()) {
            renderScaledText(TextComponentUtil.translate("gui.mekanism.noFluid"), 53, 44, 0x00CD00, 74);
        } else {
            //TODO: Can these two be combined
            renderScaledText(TextComponentUtil.build(fluidStored, ":"), 53, 44, 0x00CD00, 74);
            drawString(fluidStored.getAmount() + "mB", 53, 53, 0x00CD00);
        }
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 7 && xAxis <= 39 && yAxis >= 14 && yAxis <= 72) {
            if (fluidStored.isEmpty()) {
                displayTooltip(TextComponentUtil.translate("gui.mekanism.empty"), xAxis, yAxis);
            } else {
                displayTooltip(TextComponentUtil.build(fluidStored, ": " + fluidStored.getAmount() + "mB"), xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        int scaledFluidLevel = tile.getScaledFluidLevel(58);
        if (scaledFluidLevel > 0) {
            displayGauge(7, 14, scaledFluidLevel, tile.structure.fluidStored, 0);
            displayGauge(23, 14, scaledFluidLevel, tile.structure.fluidStored, 1);
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "dynamic_tank.png");
    }

    @Override
    protected ResourceLocation getGaugeResource() {
        return getGuiLocation();
    }
}