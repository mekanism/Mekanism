package mekanism.generators.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge.Type;
import mekanism.client.gui.element.gauge.GuiNumberGauge;
import mekanism.client.gui.element.gauge.GuiNumberGauge.INumberInfoHandler;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.generators.client.gui.element.GuiReactorTab;
import mekanism.generators.client.gui.element.GuiReactorTab.ReactorTab;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiReactorHeat extends GuiReactorInfo {

    public GuiReactorHeat(InventoryPlayer inventory, TileEntityReactorController tile) {
        super(tile, new ContainerNull(inventory.player, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiEnergyInfo(() -> tileEntity.isFormed() ? Arrays.asList(
              LangUtils.localize("gui.storing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()),
              LangUtils.localize("gui.producing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getReactor().getPassiveGeneration(false, true)) + "/t")
                                                                    : new ArrayList<>(), this, resource));
        addGuiElement(new GuiNumberGauge(new INumberInfoHandler() {
            @Override
            public TextureAtlasSprite getIcon() {
                return MekanismRenderer.getBaseFluidTexture(FluidRegistry.LAVA, FluidType.STILL);
            }

            @Override
            public double getLevel() {
                return TemperatureUnit.AMBIENT.convertToK(tileEntity.getPlasmaTemp(), true);
            }

            @Override
            public double getMaxLevel() {
                return 5E8;
            }

            @Override
            public String getText(double level) {
                return "Plasma: " + MekanismUtils.getTemperatureDisplay(level, TemperatureUnit.KELVIN);
            }
        }, Type.STANDARD, this, resource, 7, 50));
        addGuiElement(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.getPlasmaTemp() > tileEntity.getCaseTemp() ? 1 : 0;
            }
        }, ProgressBar.SMALL_RIGHT, this, resource, 27, 75));
        addGuiElement(new GuiNumberGauge(new INumberInfoHandler() {
            @Override
            public TextureAtlasSprite getIcon() {
                return MekanismRenderer.getBaseFluidTexture(FluidRegistry.LAVA, FluidType.STILL);
            }

            @Override
            public double getLevel() {
                return TemperatureUnit.AMBIENT.convertToK(tileEntity.getCaseTemp(), true);
            }

            @Override
            public double getMaxLevel() {
                return 5E8;
            }

            @Override
            public String getText(double level) {
                return "Case: " + MekanismUtils.getTemperatureDisplay(level, TemperatureUnit.KELVIN);
            }
        }, Type.STANDARD, this, resource, 61, 50));
        addGuiElement(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.getCaseTemp() > 0 ? 1 : 0;
            }
        }, ProgressBar.SMALL_RIGHT, this, resource, 81, 60));
        addGuiElement(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return (tileEntity.getCaseTemp() > 0 && tileEntity.waterTank.getFluidAmount() > 0 && tileEntity.steamTank.getFluidAmount() < tileEntity.steamTank.getCapacity()) ? 1 : 0;
            }
        }, ProgressBar.SMALL_RIGHT, this, resource, 81, 90));
        addGuiElement(new GuiFluidGauge(() -> tileEntity.waterTank, Type.SMALL, this, resource, 115, 84));
        addGuiElement(new GuiFluidGauge(() -> tileEntity.steamTank, Type.SMALL, this, resource, 151, 84));
        addGuiElement(new GuiEnergyGauge(() -> tileEntity, Type.SMALL, this, resource, 115, 46));
        addGuiElement(new GuiReactorTab(this, tileEntity, ReactorTab.FUEL, resource));
        addGuiElement(new GuiReactorTab(this, tileEntity, ReactorTab.STAT, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        fontRenderer.drawString(tileEntity.getName(), 46, 6, 0x404040);
    }
}