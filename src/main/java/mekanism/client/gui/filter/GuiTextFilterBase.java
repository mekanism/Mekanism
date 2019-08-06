package mekanism.client.gui.filter;

import java.io.IOException;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.input.Keyboard;

@OnlyIn(Dist.CLIENT)
public abstract class GuiTextFilterBase<FILTER extends IFilter, TILE extends TileEntityMekanism> extends GuiFilterBase<FILTER, TILE> {

    protected ItemStack renderStack = ItemStack.EMPTY;
    protected GuiTextField text;

    protected GuiTextFilterBase(TILE tile, Container container) {
        super(tile, container);
    }

    protected GuiTextFilterBase(PlayerEntity player, TILE tile) {
        super(player, tile);
    }

    protected abstract void setText();

    protected abstract GuiTextField createTextField();

    protected boolean wasTextboxKey(char c, int i) {
        return Character.isLetter(c) || Character.isDigit(c) || isTextboxKey(c, i);
    }

    @Override
    public void initGui() {
        super.initGui();
        text = createTextField();
        text.setMaxStringLength(TransporterFilter.MAX_LENGTH);
        text.setFocused(true);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        text.updateCursorCounter();
    }

    @Override
    public void keyTyped(char c, int i) throws IOException {
        if (!text.isFocused() || i == Keyboard.KEY_ESCAPE) {
            super.keyTyped(c, i);
        }
        if (text.isFocused() && i == Keyboard.KEY_RETURN) {
            setText();
            return;
        }
        if (wasTextboxKey(c, i)) {
            text.textboxKeyTyped(c, i);
        }
    }
}