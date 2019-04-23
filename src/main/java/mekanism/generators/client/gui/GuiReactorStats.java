package mekanism.generators.client.gui;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.generators.client.gui.element.GuiFuelTab;
import mekanism.generators.client.gui.element.GuiHeatTab;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiReactorStats extends GuiMekanismTile<TileEntityReactorController> {

    private static NumberFormat nf = NumberFormat.getIntegerInstance();

    public GuiReactorStats(InventoryPlayer inventory, final TileEntityReactorController tile) {
        super(tile, new ContainerNull(inventory.player, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiEnergyInfo(() -> tileEntity.isFormed() ? Arrays.asList(
              LangUtils.localize("gui.storing") + ": " + MekanismUtils
                    .getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()),
              LangUtils.localize("gui.producing") + ": " + MekanismUtils
                    .getEnergyDisplay(tileEntity.getReactor().getPassiveGeneration(false, true)) + "/t")
              : new ArrayList<>(), this, resource));
        addGuiElement(new GuiHeatTab(this, tileEntity, resource));
        addGuiElement(new GuiFuelTab(this, tileEntity, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(tileEntity.getName(), 46, 6, 0x404040);
        if (tileEntity.isFormed()) {
            fontRenderer.drawString(EnumColor.DARK_GREEN + LangUtils.localize("gui.passive"), 6, 26, 0x404040);
            fontRenderer.drawString(
                  LangUtils.localize("gui.minInject") + ": " + (tileEntity.getReactor().getMinInjectionRate(false)), 16,
                  36, 0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.ignition") + ": " + (MekanismUtils
                  .getTemperatureDisplay(tileEntity.getReactor().getIgnitionTemperature(false),
                        TemperatureUnit.AMBIENT)), 16, 46, 0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.maxPlasma") + ": " + (MekanismUtils
                  .getTemperatureDisplay(tileEntity.getReactor().getMaxPlasmaTemperature(false),
                        TemperatureUnit.AMBIENT)), 16, 56, 0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.maxCasing") + ": " + (MekanismUtils
                  .getTemperatureDisplay(tileEntity.getReactor().getMaxCasingTemperature(false),
                        TemperatureUnit.AMBIENT)), 16, 66, 0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.passiveGeneration") + ": " + MekanismUtils
                        .getEnergyDisplay(tileEntity.getReactor().getPassiveGeneration(false, false)) + "/t", 16, 76,
                  0x404040);
            fontRenderer.drawString(EnumColor.DARK_BLUE + LangUtils.localize("gui.active"), 6, 92, 0x404040);
            fontRenderer.drawString(
                  LangUtils.localize("gui.minInject") + ": " + (tileEntity.getReactor().getMinInjectionRate(true)), 16,
                  102, 0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.ignition") + ": " + (MekanismUtils
                  .getTemperatureDisplay(tileEntity.getReactor().getIgnitionTemperature(true),
                        TemperatureUnit.AMBIENT)), 16, 112, 0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.maxPlasma") + ": " + (MekanismUtils
                  .getTemperatureDisplay(tileEntity.getReactor().getMaxPlasmaTemperature(true),
                        TemperatureUnit.AMBIENT)), 16, 122, 0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.maxCasing") + ": " + (MekanismUtils
                  .getTemperatureDisplay(tileEntity.getReactor().getMaxCasingTemperature(true),
                        TemperatureUnit.AMBIENT)), 16, 132, 0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.passiveGeneration") + ": " + MekanismUtils
                        .getEnergyDisplay(tileEntity.getReactor().getPassiveGeneration(true, false)) + "/t", 16, 142,
                  0x404040);
            fontRenderer.drawString(LangUtils.localize("gui.steamProduction") + ": " + nf
                  .format(tileEntity.getReactor().getSteamPerTick(false)) + "mB/t", 16, 152, 0x404040);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
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