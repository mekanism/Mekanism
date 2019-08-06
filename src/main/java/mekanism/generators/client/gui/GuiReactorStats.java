package mekanism.generators.client.gui;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import mekanism.api.EnumColor;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.generators.client.gui.element.GuiReactorTab;
import mekanism.generators.client.gui.element.GuiReactorTab.ReactorTab;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiReactorStats extends GuiReactorInfo {

    private static final NumberFormat nf = NumberFormat.getIntegerInstance();

    public GuiReactorStats(PlayerInventory inventory, final TileEntityReactorController tile) {
        super(tile, new ContainerNull(inventory.player, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiEnergyInfo(() -> tileEntity.isFormed() ? Arrays.asList(
              LangUtils.localize("gui.storing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()),
              LangUtils.localize("gui.producing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getReactor().getPassiveGeneration(false, true)) + "/t")
                                                                    : new ArrayList<>(), this, resource));
        addGuiElement(new GuiReactorTab(this, tileEntity, ReactorTab.HEAT, resource));
        addGuiElement(new GuiReactorTab(this, tileEntity, ReactorTab.FUEL, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(tileEntity.getName(), 46, 6, 0x404040);
        if (tileEntity.isFormed()) {
            fontRenderer.drawString(EnumColor.DARK_GREEN + LangUtils.localize("gui.passive"), 6, 26, 0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.minInject") + ": " + tileEntity.getReactor().getMinInjectionRate(false), 16, 36, 0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.ignition") + ": " +
                                    MekanismUtils.getTemperatureDisplay(tileEntity.getReactor().getIgnitionTemperature(false), TemperatureUnit.AMBIENT), 16, 46, 0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.maxPlasma") + ": " +
                                    MekanismUtils.getTemperatureDisplay(tileEntity.getReactor().getMaxPlasmaTemperature(false), TemperatureUnit.AMBIENT), 16, 56, 0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.maxCasing") + ": " +
                                    MekanismUtils.getTemperatureDisplay(tileEntity.getReactor().getMaxCasingTemperature(false), TemperatureUnit.AMBIENT), 16, 66, 0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.passiveGeneration") + ": " +
                                    MekanismUtils.getEnergyDisplay(tileEntity.getReactor().getPassiveGeneration(false, false)) + "/t", 16, 76, 0x404040);
            fontRenderer.drawString(EnumColor.DARK_BLUE + LangUtils.localize("gui.active"), 6, 92, 0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.minInject") + ": " + tileEntity.getReactor().getMinInjectionRate(true), 16, 102, 0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.ignition") + ": " +
                                    MekanismUtils.getTemperatureDisplay(tileEntity.getReactor().getIgnitionTemperature(true), TemperatureUnit.AMBIENT), 16, 112, 0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.maxPlasma") + ": " +
                                    MekanismUtils.getTemperatureDisplay(tileEntity.getReactor().getMaxPlasmaTemperature(true), TemperatureUnit.AMBIENT), 16, 122, 0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.maxCasing") + ": " +
                                    MekanismUtils.getTemperatureDisplay(tileEntity.getReactor().getMaxCasingTemperature(true), TemperatureUnit.AMBIENT), 16, 132, 0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.passiveGeneration") + ": " +
                                    MekanismUtils.getEnergyDisplay(tileEntity.getReactor().getPassiveGeneration(true, false)) + "/t", 16, 142, 0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.steamProduction") + ": " + nf.format(tileEntity.getReactor().getSteamPerTick(false)) + "mB/t", 16, 152, 0x404040);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}