package mekanism.generators.client.gui;

import java.util.Arrays;
import java.util.Collections;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mekanism.generators.client.gui.element.GuiReactorTab;
import mekanism.generators.client.gui.element.GuiReactorTab.ReactorTab;
import mekanism.generators.common.inventory.container.reactor.ReactorControllerContainer;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiReactorController extends GuiMekanismTile<TileEntityReactorController, ReactorControllerContainer> {

    public GuiReactorController(ReactorControllerContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        if (tileEntity.isFormed()) {
            ResourceLocation resource = getGuiLocation();
            addGuiElement(new GuiEnergyInfo(() -> tileEntity.isFormed() ? Arrays.asList(
                  TextComponentUtil.build(Translation.of("mekanism.gui.storing"), ": ", EnergyDisplay.of(tileEntity.getEnergy(), tileEntity.getMaxEnergy())),
                  TextComponentUtil.build(Translation.of("mekanism.gui.producing"), ": ",
                        EnergyDisplay.of(tileEntity.getReactor().getPassiveGeneration(false, true)), "/t")) : Collections.emptyList(), this, resource));
            addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 79, 38));
            addGuiElement(new GuiReactorTab(this, tileEntity, ReactorTab.HEAT, resource));
            addGuiElement(new GuiReactorTab(this, tileEntity, ReactorTab.FUEL, resource));
            addGuiElement(new GuiReactorTab(this, tileEntity, ReactorTab.STAT, resource));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), 46, 6, 0x404040);
        if (tileEntity.getActive()) {
            drawString(TextComponentUtil.translate("mekanism.gui.formed"), 8, 16, 0x404040);
        } else {
            drawString(TextComponentUtil.translate("mekanism.gui.incomplete"), 8, 16, 0x404040);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "blank.png");
    }
}