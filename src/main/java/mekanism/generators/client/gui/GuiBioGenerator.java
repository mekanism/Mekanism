package mekanism.generators.client.gui;

import java.util.Arrays;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TextComponentUtil;
import mekanism.common.util.TextComponentUtil.EnergyDisplay;
import mekanism.common.util.TextComponentUtil.Translation;
import mekanism.generators.common.inventory.container.ContainerBioGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiBioGenerator extends GuiMekanismTile<TileEntityBioGenerator> {

    public GuiBioGenerator(PlayerInventory inventory, TileEntityBioGenerator tile) {
        super(tile, new ContainerBioGenerator(inventory, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiSecurityTab<>(this, tileEntity, resource));
        addGuiElement(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("mekanism.gui.producing"), ": ",
                    EnergyDisplay.of(tileEntity.getActive() ? MekanismConfig.current().generators.bioGeneration.val() : 0), "/t"),
              TextComponentUtil.build(Translation.of("mekanism.gui.maxOutput"), ": ", EnergyDisplay.of(tileEntity.getMaxOutput()), "/t"))
              , this, resource));
        addGuiElement(new GuiPowerBar(this, tileEntity, resource, 164, 15));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 16, 34));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 142, 34).with(SlotOverlay.POWER));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        font.drawString(tileEntity.getName(), 45, 6, 0x404040);
        font.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        font.drawString(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()), 51, 26, 0x00CD00);
        font.drawString(LangUtils.localize("gui.bioGenerator.bioFuel") + ": " + tileEntity.bioFuelSlot.fluidStored, 51, 35, 0x00CD00);
        font.drawString(LangUtils.localize("gui.out") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxOutput()) + "/t", 51, 44, 0x00CD00);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        int displayInt = tileEntity.getScaledFuelLevel(52);
        drawTexturedRect(guiLeft + 7, guiTop + 17 + 52 - displayInt, 176, 52 + 52 - displayInt, 4, displayInt);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiBioGenerator.png");
    }
}