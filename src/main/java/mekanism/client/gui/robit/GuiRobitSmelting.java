package mekanism.client.gui.robit;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.entity.robit.RobitContainer;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiRobitSmelting extends GuiRobit<RobitContainer> {

    public GuiRobitSmelting(RobitContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        inventoryLabelY += 1;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addButton(new GuiProgress(robit::getScaledProgress, ProgressType.BAR, this, 78, 38).jeiCategories(MekanismBlocks.ENERGIZED_SMELTER.getRegistryName()));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawString(matrix, MekanismLang.ROBIT_SMELTING.translate(), titleLabelX, titleLabelY, titleTextColor());
        drawString(matrix, inventory.getDisplayName(), inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.SMELTING;
    }
}