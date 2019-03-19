package mekanism.generators.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import mekanism.api.Coord4D;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyGauge;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiFluidGauge;
import mekanism.client.gui.element.GuiGauge.Type;
import mekanism.client.gui.element.GuiNumberGauge;
import mekanism.client.gui.element.GuiNumberGauge.INumberInfoHandler;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.generators.client.gui.element.GuiFuelTab;
import mekanism.generators.client.gui.element.GuiStatTab;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiReactorHeat extends GuiMekanismTile<TileEntityReactorController> {

    public GuiReactorHeat(InventoryPlayer inventory, TileEntityReactorController tile) {
        super(tile, new ContainerNull(inventory.player, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiEnergyInfo(() -> tileEntity.isFormed() ? Arrays.asList(
              LangUtils.localize("gui.storing") + ": " + MekanismUtils
                    .getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()),
              LangUtils.localize("gui.producing") + ": " + MekanismUtils
                    .getEnergyDisplay(tileEntity.getReactor().getPassiveGeneration(false, true)) + "/t")
              : new ArrayList<>(), this, resource));
        addGuiElement(new GuiNumberGauge(new INumberInfoHandler() {
            @Override
            public TextureAtlasSprite getIcon() {
                return MekanismRenderer.getFluidTexture(FluidRegistry.LAVA, FluidType.STILL);
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
                return MekanismRenderer.getFluidTexture(FluidRegistry.LAVA, FluidType.STILL);
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
                return (tileEntity.getCaseTemp() > 0 && tileEntity.waterTank.getFluidAmount() > 0
                      && tileEntity.steamTank.getFluidAmount() < tileEntity.steamTank.getCapacity()) ? 1 : 0;
            }
        }, ProgressBar.SMALL_RIGHT, this, resource, 81, 90));
        addGuiElement(new GuiFluidGauge(() -> tileEntity.waterTank, Type.SMALL, this, resource, 115, 84));
        addGuiElement(new GuiFluidGauge(() -> tileEntity.steamTank, Type.SMALL, this, resource, 151, 84));
        addGuiElement(new GuiEnergyGauge(() -> tileEntity, Type.SMALL, this, resource, 115, 46));
        addGuiElement(new GuiFuelTab(this, tileEntity, resource));
        addGuiElement(new GuiStatTab(this, tileEntity, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        fontRenderer.drawString(tileEntity.getName(), 46, 6, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(getGuiLocation());
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        if (xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20) {
            drawTexturedModalRect(guiWidth + 6, guiHeight + 6, 176, 0, 14, 14);
        } else {
            drawTexturedModalRect(guiWidth + 6, guiHeight + 6, 176, 14, 14, 14);
        }
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        if (button == 0) {
            if (xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tileEntity), 1, 10));
            }
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png");
    }
}