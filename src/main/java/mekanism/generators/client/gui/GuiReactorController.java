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
import mekanism.generators.client.gui.element.GuiReactorTab;
import mekanism.generators.client.gui.element.GuiReactorTab.ReactorTab;
import mekanism.generators.common.inventory.container.ContainerReactorController;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiReactorController extends GuiMekanismTile<TileEntityReactorController> {

    public GuiReactorController(PlayerInventory inventory, final TileEntityReactorController tile) {
        super(tile, new ContainerReactorController(inventory, tile));
        if (tileEntity.isFormed()) {
            ResourceLocation resource = getGuiLocation();
            addGuiElement(new GuiEnergyInfo(() -> tileEntity.isFormed() ? Arrays.asList(
                  LangUtils.localize("gui.storing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()),
                  LangUtils.localize("gui.producing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getReactor().getPassiveGeneration(false, true)) + "/t")
                                                                        : new ArrayList<>(), this, resource));
            addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 79, 38));
            addGuiElement(new GuiReactorTab(this, tileEntity, ReactorTab.HEAT, resource));
            addGuiElement(new GuiReactorTab(this, tileEntity, ReactorTab.FUEL, resource));
            addGuiElement(new GuiReactorTab(this, tileEntity, ReactorTab.STAT, resource));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        font.drawString(tileEntity.getName(), 46, 6, 0x404040);
        if (tileEntity.getActive()) {
            font.drawString(LangUtils.localize("gui.formed"), 8, 16, 0x404040);
        } else {
            font.drawString(LangUtils.localize("gui.incomplete"), 8, 16, 0x404040);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png");
    }
}