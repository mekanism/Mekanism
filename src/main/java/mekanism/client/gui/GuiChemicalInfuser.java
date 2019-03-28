package mekanism.client.gui;

import mekanism.client.gui.element.GuiEnergyInfo;
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
import mekanism.common.inventory.container.ContainerChemicalInfuser;
import mekanism.common.tile.TileEntityChemicalInfuser;
import mekanism.common.util.LangUtils;
import mekanism.common.util.ListUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiChemicalInfuser extends GuiMekanism {

    public TileEntityChemicalInfuser tileEntity;

    public GuiChemicalInfuser(InventoryPlayer inventory, TileEntityChemicalInfuser tentity) {
        super(tentity, new ContainerChemicalInfuser(inventory, tentity));
        tileEntity = tentity;

        guiElements.add(new GuiSecurityTab(this, tileEntity,
              MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalInfuser.png")));
        guiElements.add(new GuiRedstoneControl(this, tileEntity,
              MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalInfuser.png")));
        guiElements.add(new GuiUpgradeTab(this, tileEntity,
              MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalInfuser.png")));
        guiElements.add(new GuiEnergyInfo(() ->
        {
            String usage = MekanismUtils.getEnergyDisplay(tileEntity.clientEnergyUsed);
            return ListUtils.asList(LangUtils.localize("gui.using") + ": " + usage + "/t",
                  LangUtils.localize("gui.needed") + ": " + MekanismUtils
                        .getEnergyDisplay(tileEntity.getMaxEnergy() - tileEntity.getEnergy()));
        }, this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalInfuser.png")));
        guiElements.add(new GuiGasGauge(() -> tileEntity.leftTank, GuiGauge.Type.STANDARD, this,
              MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalInfuser.png"), 25, 13));
        guiElements.add(new GuiGasGauge(() -> tileEntity.centerTank, GuiGauge.Type.STANDARD, this,
              MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalInfuser.png"), 79, 4));
        guiElements.add(new GuiGasGauge(() -> tileEntity.rightTank, GuiGauge.Type.STANDARD, this,
              MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalInfuser.png"), 133, 13));

        guiElements.add(new GuiSlot(SlotType.NORMAL, this,
              MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalInfuser.png"), 154, 4).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.NORMAL, this,
              MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalInfuser.png"), 154, 55).with(SlotOverlay.MINUS));
        guiElements.add(new GuiSlot(SlotType.NORMAL, this,
              MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalInfuser.png"), 4, 55).with(SlotOverlay.MINUS));
        guiElements.add(new GuiSlot(SlotType.NORMAL, this,
              MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalInfuser.png"), 79, 64).with(SlotOverlay.PLUS));

        guiElements.add(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.isActive ? 1 : 0;
            }
        }, ProgressBar.SMALL_RIGHT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalInfuser.png"), 45,
              38));
        guiElements.add(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.isActive ? 1 : 0;
            }
        }, ProgressBar.SMALL_LEFT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalInfuser.png"), 99,
              38));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);

        fontRenderer.drawString(LangUtils.localize("gui.chemicalInfuser.short"), 5, 5, 0x404040);
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 4, 0x404040);

        if (xAxis >= 116 && xAxis <= 168 && yAxis >= 76 && yAxis <= 80) {
            drawHoveringText(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()), xAxis,
                  yAxis);
        }

        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalInfuser.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

        int displayInt;

        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 116, guiHeight + 76, 176, 0, displayInt, 4);

        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    }
}
