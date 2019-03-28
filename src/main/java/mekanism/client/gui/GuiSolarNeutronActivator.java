package mekanism.client.gui;

import mekanism.client.gui.element.GuiGasGauge;
import mekanism.client.gui.element.GuiGauge;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSecurityTab;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.GuiUpgradeTab;
import mekanism.common.inventory.container.ContainerSolarNeutronActivator;
import mekanism.common.tile.TileEntitySolarNeutronActivator;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiSolarNeutronActivator extends GuiMekanism {

    public TileEntitySolarNeutronActivator tileEntity;

    public GuiSolarNeutronActivator(InventoryPlayer inventory, TileEntitySolarNeutronActivator tentity) {
        super(tentity, new ContainerSolarNeutronActivator(inventory, tentity));
        tileEntity = tentity;

        guiElements
              .add(new GuiSecurityTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png")));
        guiElements.add(new GuiRedstoneControl(this, tileEntity,
              MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png")));
        guiElements
              .add(new GuiUpgradeTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png")));
        guiElements
              .add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 4,
                    55).with(SlotOverlay.MINUS));
        guiElements
              .add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 154,
                    55).with(SlotOverlay.PLUS));

        guiElements.add(new GuiGasGauge(() -> tileEntity.inputTank, GuiGauge.Type.STANDARD, this,
              MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 25, 13));
        guiElements.add(new GuiGasGauge(() -> tileEntity.outputTank, GuiGauge.Type.STANDARD, this,
              MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 133, 13));

        guiElements.add(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.isActive ? 1 : 0;
            }
        }, ProgressBar.LARGE_RIGHT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 62, 38));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);

        fontRenderer.drawString(tileEntity.getName(), 26, 4, 0x404040);
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 4, 0x404040);

        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int displayInt;

        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);

        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    }
}
