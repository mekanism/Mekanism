package mekanism.generators.client.gui;

import java.util.Arrays;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.GuiEmbeddedGaugeTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiPowerBar;
import mekanism.client.gui.element.bar.GuiRateBar;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mekanism.generators.client.gui.element.GuiTurbineTab;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.TurbineUpdateProtocol;
import mekanism.generators.common.inventory.container.turbine.TurbineContainer;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiIndustrialTurbine extends GuiEmbeddedGaugeTile<TileEntityTurbineCasing, TurbineContainer> {

    public GuiIndustrialTurbine(TurbineContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiTurbineTab(this, tileEntity, TurbineTab.STAT, resource));
        addButton(new GuiPowerBar(this, tileEntity, resource, 164, 16));
        addButton(new GuiRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return TextComponentUtil.build(Translation.of("gui.mekanism.steamInput"),
                      ": " + (tileEntity.structure == null ? 0 : tileEntity.structure.lastSteamInput) + " mB/t");
            }

            @Override
            public double getLevel() {
                if (tileEntity.structure == null) {
                    return 0;
                }
                double rate = Math.min(tileEntity.structure.lowerVolume * tileEntity.structure.clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get(),
                      tileEntity.structure.vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
                if (rate == 0) {
                    return 0;
                }
                return (double) tileEntity.structure.lastSteamInput / rate;
            }
        }, resource, 40, 13));
        addButton(new GuiEnergyInfo(() -> {
            double producing = tileEntity.structure == null ? 0 : tileEntity.structure.clientFlow * (MekanismConfig.general.maxEnergyPerSteam.get() / TurbineUpdateProtocol.MAX_BLADES) *
                                                                  Math.min(tileEntity.structure.blades, tileEntity.structure.coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get());
            return Arrays.asList(TextComponentUtil.build(Translation.of("gui.mekanism.storing"), ": ", EnergyDisplay.of(tileEntity.getEnergy(), tileEntity.getMaxEnergy())),
                  TextComponentUtil.build(Translation.of("gui.mekanism.producing"), ": ", EnergyDisplay.of(producing), "/t"));
        }, this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 96) + 4, 0x404040);
        drawString(tileEntity.getName(), (xSize / 2) - (getStringWidth(tileEntity.getName()) / 2), 5, 0x404040);
        if (tileEntity.structure != null) {
            double energyMultiplier = (MekanismConfig.general.maxEnergyPerSteam.get() / TurbineUpdateProtocol.MAX_BLADES) *
                                      Math.min(tileEntity.structure.blades, tileEntity.structure.coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get());
            double rate = tileEntity.structure.lowerVolume * (tileEntity.structure.clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get());
            rate = Math.min(rate, tileEntity.structure.vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
            renderScaledText(TextComponentUtil.build(Translation.of("gui.mekanism.production"), ": ",
                  EnergyDisplay.of(tileEntity.structure.clientFlow * energyMultiplier)), 53, 26, 0x00CD00, 106);
            renderScaledText(TextComponentUtil.build(Translation.of("gui.mekanism.flow_rate"), ": " + tileEntity.structure.clientFlow + " mB/t"), 53, 35, 0x00CD00, 106);
            renderScaledText(TextComponentUtil.build(Translation.of("gui.mekanism.capacity"), ": " + tileEntity.structure.getFluidCapacity() + " mB"), 53, 44, 0x00CD00, 106);
            renderScaledText(TextComponentUtil.build(Translation.of("gui.mekanism.max_flow"), ": " + rate + " mB/t"), 53, 53, 0x00CD00, 106);
            ITextComponent component = TextComponentUtil.build(tileEntity.structure.dumpMode);
            renderScaledText(component, 156 - (int) (getStringWidth(component) * getNeededScale(component, 66)), 73, 0x404040, 66);
            int xAxis = mouseX - guiLeft;
            int yAxis = mouseY - guiTop;
            //TODO: 1.14 Convert to GuiElement
            if (xAxis >= 7 && xAxis <= 39 && yAxis >= 14 && yAxis <= 72) {
                if (tileEntity.structure.fluidStored.isEmpty()) {
                    displayTooltip(TextComponentUtil.translate("gui.mekanism.empty"), xAxis, yAxis);
                } else {
                    displayTooltip(TextComponentUtil.build(tileEntity.structure.fluidStored, ": " + tileEntity.structure.fluidStored.getAmount() + "mB"), xAxis, yAxis);
                }
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        if (tileEntity.structure != null) {
            int displayInt = GasMode.chooseByMode(tileEntity.structure.dumpMode, 142, 150, 158);
            drawTexturedRect(guiLeft + 160, guiTop + 73, 176, displayInt, 8, 8);
            int scaledFluidLevel = tileEntity.getScaledFluidLevel(58);
            if (scaledFluidLevel > 0) {
                displayGauge(7, 14, scaledFluidLevel, tileEntity.structure.fluidStored, 0);
                displayGauge(23, 14, scaledFluidLevel, tileEntity.structure.fluidStored, 1);
            }
        }
    }

    @Override
    protected ResourceLocation getGaugeResource() {
        return getGuiLocation();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        double xAxis = mouseX - guiLeft;
        double yAxis = mouseY - guiTop;
        if (xAxis > 160 && xAxis < 169 && yAxis > 73 && yAxis < 82) {
            //TODO: 1.14 Convert to GuiElement/Button
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents((byte) 0)));
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            return true;
        }
        return false;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "industrial_turbine.png");
    }
}