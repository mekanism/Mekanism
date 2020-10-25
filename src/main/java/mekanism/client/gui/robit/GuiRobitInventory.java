package mekanism.client.gui.robit;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.entity.robit.InventoryRobitContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiRobitInventory extends GuiRobit<InventoryRobitContainer> {

    public GuiRobitInventory(InventoryRobitContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        playerInventoryTitleY = ySize - 93;
        dynamicSlots = true;
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawString(matrix, MekanismLang.ROBIT_INVENTORY.translate(), titleX, titleY, titleTextColor());
        drawString(matrix, playerInventory.getDisplayName(), playerInventoryTitleX, playerInventoryTitleY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.INVENTORY;
    }
}