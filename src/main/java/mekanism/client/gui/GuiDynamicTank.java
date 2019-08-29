package mekanism.client.gui;

import mekanism.client.gui.element.GuiContainerEditMode;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.inventory.container.tile.fluid.DynamicTankContainer;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

@OnlyIn(Dist.CLIENT)
public class GuiDynamicTank extends GuiEmbeddedGaugeTile<TileEntityDynamicTank, DynamicTankContainer> {

    public GuiDynamicTank(DynamicTankContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiContainerEditMode(this, tileEntity, getGuiLocation()));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), (xSize / 2) - (getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 94) + 2, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("mekanism.gui.volume"), ": " + tileEntity.clientCapacity / TankUpdateProtocol.FLUID_PER_TANK), 53, 26, 0x00CD00);
        FluidStack fluidStored = tileEntity.structure != null ? tileEntity.structure.fluidStored : FluidStack.EMPTY;
        if (fluidStored.isEmpty()) {
            renderScaledText(TextComponentUtil.translate("mekanism.gui.noFluid"), 53, 44, 0x00CD00, 74);
        } else {
            //TODO: Can these two be combined
            renderScaledText(TextComponentUtil.build(fluidStored, ":"), 53, 44, 0x00CD00, 74);
            drawString(fluidStored.getAmount() + "mB", 53, 53, 0x00CD00);
        }
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 7 && xAxis <= 39 && yAxis >= 14 && yAxis <= 72) {
            if (fluidStored.isEmpty()) {
                displayTooltip(TextComponentUtil.translate("mekanism.gui.empty"), xAxis, yAxis);
            } else {
                displayTooltip(TextComponentUtil.build(fluidStored, ": " + fluidStored.getAmount() + "mB"), xAxis, yAxis);
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
        return MekanismUtils.getResource(ResourceType.GUI, "dynamic_tank.png");
    }

    @Override
    protected ResourceLocation getGaugeResource() {
        return getGuiLocation();
    }
}