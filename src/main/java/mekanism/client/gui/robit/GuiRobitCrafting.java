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
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiRightArrow(this, 90, 35).jeiCrafting());
    }

    @Override
    protected void func_230451_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawString(matrix, MekanismLang.ROBIT_CRAFTING.translate(), 8, 6, titleTextColor());
        drawString(matrix, MekanismLang.INVENTORY.translate(), 8, getYSize() - 93, titleTextColor());
        super.func_230451_b_(matrix, mouseX, mouseY);
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.CRAFTING;
    }
}