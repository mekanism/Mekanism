package mekanism.client.gui;

import java.util.Arrays;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiRateBar;
import mekanism.client.gui.element.GuiRateBar.IRateInfoHandler;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.tab.GuiMatrixTab;
import mekanism.client.gui.element.tab.GuiMatrixTab.MatrixTab;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiMatrixStats extends GuiMekanismTile<TileEntityInductionCasing> {

    public GuiMatrixStats(PlayerInventory inventory, TileEntityInductionCasing tile) {
        super(tile, new ContainerNull(inventory.player, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiMatrixTab(this, tileEntity, MatrixTab.MAIN, resource));
        addGuiElement(new GuiEnergyGauge(() -> tileEntity, GuiEnergyGauge.Type.STANDARD, this, resource, 6, 13));
        addGuiElement(new GuiRateBar(this, new IRateInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return TextComponentUtil.build(Translation.of("mekanism.gui.receiving"), ": ", EnergyDisplay.of(tileEntity.getLastInput()), "/t");
            }

            @Override
            public double getLevel() {
                return tileEntity.structure == null ? 0 : tileEntity.getLastInput() / tileEntity.structure.getTransferCap();
            }
        }, resource, 30, 13));
        addGuiElement(new GuiRateBar(this, new IRateInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return TextComponentUtil.build(Translation.of("mekanism.gui.outputting"), ": ", EnergyDisplay.of(tileEntity.getLastOutput()), "/t");
            }

            @Override
            public double getLevel() {
                return tileEntity.structure == null ? 0 : tileEntity.getLastOutput() / tileEntity.structure.getTransferCap();
            }
        }, resource, 38, 13));
        addGuiElement(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("mekanism.gui.storing"), ": ", EnergyDisplay.of(tileEntity.getEnergy(), tileEntity.getMaxEnergy())),
              TextComponentUtil.build(Translation.of("mekanism.gui.input"), ": ", EnergyDisplay.of(tileEntity.getLastInput()), "/t"),
              TextComponentUtil.build(Translation.of("mekanism.gui.output"), ": ", EnergyDisplay.of(tileEntity.getLastOutput()), "/t")),
              this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String stats = LangUtils.localize("gui.matrixStats");
        drawString(stats, (xSize / 2) - (getStringWidth(stats) / 2), 6, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("gui.input"), ":"), 53, 26, 0x797979);
        drawString(TextComponentUtil.build(EnergyDisplay.of(tileEntity.getLastInput(), tileEntity.getTransferCap())), 59, 35, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("gui.output"), ":"), 53, 46, 0x797979);
        drawString(TextComponentUtil.build(EnergyDisplay.of(tileEntity.getLastOutput(), tileEntity.getTransferCap())), 59, 55, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("gui.dimensions"), ":"), 8, 82, 0x797979);
        if (tileEntity.structure != null) {
            drawString(tileEntity.structure.volWidth + " x " + tileEntity.structure.volHeight + " x " + tileEntity.structure.volLength, 14, 91, 0x404040);
        }
        drawString(TextComponentUtil.build(Translation.of("gui.constituents"), ":"), 8, 102, 0x797979);
        drawString(tileEntity.getCellCount() + " " + LangUtils.localize("gui.cells"), 14, 111, 0x404040);
        drawString(tileEntity.getProviderCount() + " " + LangUtils.localize("gui.providers"), 14, 120, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiNull.png");
    }
}