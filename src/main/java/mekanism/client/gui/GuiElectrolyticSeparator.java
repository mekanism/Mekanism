package mekanism.client.gui;

import java.util.Arrays;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.tile.ElectrolyticSeparatorContainer;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiElectrolyticSeparator extends GuiMekanismTile<TileEntityElectrolyticSeparator, ElectrolyticSeparatorContainer> {

    public GuiElectrolyticSeparator(ElectrolyticSeparatorContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiRedstoneControl(this, tileEntity, resource));
        addButton(new GuiUpgradeTab(this, tileEntity, resource));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("mekanism.gui.using"), ": ", EnergyDisplay.of(tileEntity.clientEnergyUsed), "/t"),
              TextComponentUtil.build(Translation.of("mekanism.gui.needed"), ": ", EnergyDisplay.of(tileEntity.getNeededEnergy()))
        ), this, resource));
        addButton(new GuiFluidGauge(() -> tileEntity.fluidTank, GuiGauge.Type.STANDARD, this, resource, 5, 10));
        addButton(new GuiGasGauge(() -> tileEntity.leftTank, GuiGauge.Type.SMALL, this, resource, 58, 18));
        addButton(new GuiGasGauge(() -> tileEntity.rightTank, GuiGauge.Type.SMALL, this, resource, 100, 18));
        addButton(new GuiPowerBar(this, tileEntity, resource, 164, 15));
        addButton(new GuiSecurityTab<>(this, tileEntity, resource));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 25, 34));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 58, 51));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 100, 51));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 142, 34).with(SlotOverlay.POWER));
        addButton(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.getActive() ? 1 : 0;
            }
        }, ProgressBar.BI, this, resource, 78, 29));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        double xAxis = mouseX - guiLeft;
        double yAxis = mouseY - guiTop;
        if (xAxis > 8 && xAxis < 17 && yAxis > 73 && yAxis < 82) {
            TileNetworkList data = TileNetworkList.withContents((byte) 0);
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, data));
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
        } else if (xAxis > 160 && xAxis < 169 && yAxis > 73 && yAxis < 82) {
            TileNetworkList data = TileNetworkList.withContents((byte) 1);
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, data));
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
        }
        return true;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "electrolytic_separator.png");
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), 45, 6, 0x404040);
        renderScaledText(TextComponentUtil.build(tileEntity.dumpLeft), 21, 73, 0x404040, 66);
        ITextComponent component = TextComponentUtil.build(tileEntity.dumpRight);
        renderScaledText(component, 156 - (int) (getStringWidth(component) * getNeededScale(component, 66)), 73, 0x404040, 66);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        //Left
        drawTexturedRect(guiLeft + 8, guiTop + 73, 176, GasMode.chooseByMode(tileEntity.dumpLeft, 52, 60, 68), 8, 8);
        //Right
        drawTexturedRect(guiLeft + 160, guiTop + 73, 176, GasMode.chooseByMode(tileEntity.dumpRight, 52, 60, 68), 8, 8);
    }
}