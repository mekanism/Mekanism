package mekanism.client.gui.robit;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.entity.robit.SmeltingRobitContainer;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiRobitSmelting extends GuiRobit<SmeltingRobitContainer> {

    public GuiRobitSmelting(SmeltingRobitContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiProgress(robit::getScaledProgress, ProgressType.BAR, this, 78, 38).jeiCategories(MekanismBlocks.ENERGIZED_SMELTER.getRegistryName()));
    }

    @Override
    protected void func_230451_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawString(matrix, MekanismLang.ROBIT_SMELTING.translate(), 8, 6, titleTextColor());
        drawString(matrix, MekanismLang.INVENTORY.translate(), 8, getYSize() - 93, titleTextColor());
        super.func_230451_b_(matrix, mouseX, mouseY);
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.SMELTING;
    }
}