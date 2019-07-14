package mekanism.generators.client.gui;

import java.text.DecimalFormat;
import java.util.Arrays;
import mekanism.api.EnumColor;
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
import mekanism.generators.common.inventory.container.ContainerWindGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiWindGenerator extends GuiMekanismTile<TileEntityWindGenerator> {

    private final DecimalFormat powerFormat = new DecimalFormat("0.##");

    public GuiWindGenerator(InventoryPlayer inventory, TileEntityWindGenerator tile) {
        super(tile, new ContainerWindGenerator(inventory, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiSecurityTab(this, tileEntity, resource));
        addGuiElement(new GuiEnergyInfo(() -> Arrays.asList(
              LangUtils.localize("gui.producing") + ": " +
              MekanismUtils.getEnergyDisplay(tileEntity.getActive() ? MekanismConfig.current().generators.windGenerationMin.val() * tileEntity.getCurrentMultiplier() : 0) + "/t",
              LangUtils.localize("gui.maxOutput") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxOutput()) + "/t"), this, resource));
        addGuiElement(new GuiPowerBar(this, tileEntity, resource, 164, 15));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 142, 34).with(SlotOverlay.POWER));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(tileEntity.getName(), 45, 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        fontRenderer.drawString(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()), 51, 26, 0x00CD00);
        fontRenderer.drawString(LangUtils.localize("gui.power") + ": " + powerFormat.format(MekanismUtils.convertToDisplay(
              MekanismConfig.current().generators.windGenerationMin.val() * tileEntity.getCurrentMultiplier())), 51, 35, 0x00CD00);
        fontRenderer.drawString(LangUtils.localize("gui.out") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxOutput()) + "/t", 51, 44, 0x00CD00);
        int size = 44;
        if (!tileEntity.getActive()) {
            size += 9;
            String reason = "gui.skyBlocked";
            if (tileEntity.isBlacklistDimension()) {
                reason = "gui.noWind";
            }
            fontRenderer.drawString(EnumColor.DARK_RED + LangUtils.localize(reason), 51, size, 0x00CD00);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        drawTexturedModalRect(guiLeft + 20, guiTop + 37, 176, tileEntity.getActive() ? 52 : 64, 12, 12);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiWindTurbine.png");
    }
}