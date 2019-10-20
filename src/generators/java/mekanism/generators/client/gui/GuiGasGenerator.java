package mekanism.generators.client.gui;

import java.util.Arrays;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge.Type;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mekanism.generators.common.inventory.container.GasBurningGeneratorContainer;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiGasGenerator extends GuiMekanismTile<TileEntityGasGenerator, GasBurningGeneratorContainer> {

    public GuiGasGenerator(GasBurningGeneratorContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiRedstoneControl(this, tileEntity, resource));
        addButton(new GuiSecurityTab<>(this, tileEntity, resource));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("gui.mekanism.producing"), ": ", EnergyDisplay.of(tileEntity.generationRate * tileEntity.clientUsed), "/t"),
              TextComponentUtil.build(Translation.of("gui.mekanism.maxOutput"), ": ", EnergyDisplay.of(tileEntity.getMaxOutput()), "/t"))
              , this, resource));
        addButton(new GuiGasGauge(() -> tileEntity.fuelTank, Type.WIDE, this, resource, 55, 18));
        addButton(new GuiVerticalPowerBar(this, tileEntity, resource, 164, 15));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 16, 34).with(SlotOverlay.MINUS));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 142, 34).with(SlotOverlay.POWER));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), (xSize / 2) - (getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        //TODO
        ITextComponent component = TextComponentUtil.build(Translation.of("gui.mekanism.burn_rate"), ": " + tileEntity.clientUsed);
        drawString(component, xSize - 8 - getStringWidth(component), (ySize - 96) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "blank.png");
    }
}