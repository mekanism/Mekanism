package mekanism.client.gui.robit;

import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.entity.robit.CraftingRobitContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiRobitCrafting extends GuiRobit<CraftingRobitContainer> {

    public GuiRobitCrafting(CraftingRobitContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismLang.ROBIT_CRAFTING.translate(), 8, 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, getYSize() - 93, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "robit_crafting.png");
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.CRAFTING;
    }
}