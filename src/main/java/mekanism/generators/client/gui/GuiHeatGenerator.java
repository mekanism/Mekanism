package mekanism.generators.client.gui;

import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiFluidGauge;
import mekanism.client.gui.element.GuiGauge.Type;
import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSecurityTab;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.util.LangUtils;
import mekanism.common.util.ListUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.generators.common.inventory.container.ContainerHeatGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiHeatGenerator extends GuiMekanism {

    public TileEntityHeatGenerator tileEntity;

    public GuiHeatGenerator(InventoryPlayer inventory, TileEntityHeatGenerator tentity) {
        super(new ContainerHeatGenerator(inventory, tentity));
        tileEntity = tentity;
        guiElements.add(new GuiRedstoneControl(this, tileEntity,
              MekanismUtils.getResource(ResourceType.GUI, "GuiHeatGenerator.png")));
        guiElements.add(new GuiSecurityTab(this, tileEntity,
              MekanismUtils.getResource(ResourceType.GUI, "GuiHeatGenerator.png")));
        guiElements.add(new GuiEnergyInfo(() -> ListUtils.asList(
              LangUtils.localize("gui.producing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.producingEnergy)
                    + "/t",
              LangUtils.localize("gui.maxOutput") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxOutput())
                    + "/t"), this, MekanismUtils.getResource(ResourceType.GUI, "GuiHeatGenerator.png")));
        guiElements.add(new GuiFluidGauge(() -> tileEntity.lavaTank, Type.WIDE, this,
              MekanismUtils.getResource(ResourceType.GUI, "GuiHeatGenerator.png"), 55, 18));
        guiElements.add(new GuiPowerBar(this, tileEntity,
              MekanismUtils.getResource(ResourceType.GUI, "GuiHeatGenerator.png"), 164, 15));
        guiElements.add(new GuiSlot(SlotType.NORMAL, this,
              MekanismUtils.getResource(ResourceType.GUI, "GuiHeatGenerator.png"), 16, 34));
        guiElements.add(new GuiSlot(SlotType.NORMAL, this,
              MekanismUtils.getResource(ResourceType.GUI, "GuiHeatGenerator.png"), 142, 34).with(SlotOverlay.POWER));
        guiElements.add(new GuiHeatInfo(() ->
        {
            TemperatureUnit unit = TemperatureUnit.values()[general.tempUnit.ordinal()];
            String transfer = UnitDisplayUtils.getDisplayShort(tileEntity.lastTransferLoss, false, unit);
            String environment = UnitDisplayUtils.getDisplayShort(tileEntity.lastEnvironmentLoss, false, unit);
            return ListUtils.asList(LangUtils.localize("gui.transferred") + ": " + transfer + "/t",
                  LangUtils.localize("gui.dissipated") + ": " + environment + "/t");
        }, this, MekanismUtils.getResource(ResourceType.GUI, "GuiHeatGenerator.png")));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(tileEntity.getName(), 45, 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);

        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiHeatGenerator.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    }
}
