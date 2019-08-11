package mekanism.generators.client.gui;

import java.util.Arrays;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge.Type;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TextComponentUtil;
import mekanism.common.util.TextComponentUtil.EnergyDisplay;
import mekanism.common.util.TextComponentUtil.Translation;
import mekanism.generators.common.inventory.container.ContainerGasGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiGasGenerator extends GuiMekanismTile<TileEntityGasGenerator> {

    public GuiGasGenerator(PlayerInventory inventory, TileEntityGasGenerator tile) {
        super(tile, new ContainerGasGenerator(inventory, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiSecurityTab<>(this, tileEntity, resource));
        addGuiElement(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("mekanism.gui.producing"), ": ", EnergyDisplay.of(tileEntity.generationRate * tileEntity.clientUsed), "/t"),
              TextComponentUtil.build(Translation.of("mekanism.gui.maxOutput"), ": ", EnergyDisplay.of(tileEntity.getMaxOutput()), "/t"))
              , this, resource));
        addGuiElement(new GuiGasGauge(() -> tileEntity.fuelTank, Type.WIDE, this, resource, 55, 18));
        addGuiElement(new GuiPowerBar(this, tileEntity, resource, 164, 15));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 16, 34).with(SlotOverlay.MINUS));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 142, 34).with(SlotOverlay.POWER));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        font.drawString(tileEntity.getName(), (xSize / 2) - (font.getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        font.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        String s = LangUtils.localize("gui.burnRate") + ": " + tileEntity.clientUsed;
        font.drawString(s, xSize - 8 - font.getStringWidth(s), (ySize - 96) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiGasGenerator.png");
    }
}