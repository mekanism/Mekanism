package mekanism.client.gui.robit;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.element.GuiRightArrow;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.entity.robit.CraftingRobitContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiRobitCrafting extends GuiRobit<CraftingRobitContainer> {

    public GuiRobitCrafting(CraftingRobitContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiRightArrow(this, 90, 35).jeiCrafting());
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawString(matrix, MekanismLang.ROBIT_CRAFTING.translate(), 8, 6, titleTextColor());
        drawString(matrix, MekanismLang.INVENTORY.translate(), 8, getYSize() - 93, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.CRAFTING;
    }
}