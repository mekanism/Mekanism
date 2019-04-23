package mekanism.generators.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.gui.element.GuiFuelTab;
import mekanism.generators.client.gui.element.GuiHeatTab;
import mekanism.generators.client.gui.element.GuiStatTab;
import mekanism.generators.common.inventory.container.ContainerReactorController;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiReactorController extends GuiMekanismTile<TileEntityReactorController> {

    public GuiReactorController(InventoryPlayer inventory, final TileEntityReactorController tile) {
        super(tile, new ContainerReactorController(inventory, tile));
        if (tileEntity.isFormed()) {
            ResourceLocation resource = getGuiLocation();
            addGuiElement(new GuiEnergyInfo(() -> tileEntity.isFormed() ? Arrays.asList(
                  LangUtils.localize("gui.storing") + ": " + MekanismUtils
                        .getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()),
                  LangUtils.localize("gui.producing") + ": " + MekanismUtils
                        .getEnergyDisplay(tileEntity.getReactor().getPassiveGeneration(false, true)) + "/t")
                  : new ArrayList<>(), this, resource));
            addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource,  79, 38));
            addGuiElement(new GuiHeatTab(this, tileEntity, resource));
            addGuiElement(new GuiFuelTab(this, tileEntity, resource));
            addGuiElement(new GuiStatTab(this, tileEntity, resource));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(tileEntity.getName(), 46, 6, 0x404040);
        if (tileEntity.getActive()) {
            fontRenderer.drawString(LangUtils.localize("gui.formed"), 8, 16, 0x404040);
        } else {
            fontRenderer.drawString(LangUtils.localize("gui.incomplete"), 8, 16, 0x404040);
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
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png");
    }
}