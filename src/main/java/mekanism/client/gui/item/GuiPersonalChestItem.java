package mekanism.client.gui.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.item.PersonalChestItemContainer;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiPersonalChestItem extends GuiMekanism<PersonalChestItemContainer> {

    public GuiPersonalChestItem(PersonalChestItemContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize += 64;
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiSecurityTab<>(this, container.getHand()));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, MekanismBlocks.PERSONAL_CHEST.getTextComponent(), 6);
        drawString(matrix, MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}