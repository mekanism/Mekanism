package mekanism.client.gui.qio;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.custom.GuiQIOFrequencyDataScreen;
import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.tile.qio.TileEntityQIODriveArray;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiQIODriveArray extends GuiMekanismTile<TileEntityQIODriveArray, MekanismTileContainer<TileEntityQIODriveArray>> {

    public GuiQIODriveArray(MekanismTileContainer<TileEntityQIODriveArray> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        ySize += 40;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiQIOFrequencyTab(this, tile));
        func_230480_a_(new GuiSecurityTab<>(this, tile));
        func_230480_a_(new GuiQIOFrequencyDataScreen(this, 15, 19, xSize - 32, 46, () -> tile.getFrequency(FrequencyType.QIO)));
    }

    @Override
    protected void func_230451_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, titleTextColor());
        super.func_230451_b_(matrix, mouseX, mouseY);
    }
}