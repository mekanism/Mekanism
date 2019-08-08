package mekanism.client.gui.filter;

import java.io.IOException;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public abstract class GuiTextFilterBase<FILTER extends IFilter, TILE extends TileEntityMekanism> extends GuiFilterBase<FILTER, TILE> {

    protected ItemStack renderStack = ItemStack.EMPTY;
    protected TextFieldWidget text;

    protected GuiTextFilterBase(TILE tile, Container container) {
        super(tile, container);
    }

    protected GuiTextFilterBase(PlayerEntity player, TILE tile) {
        super(player, tile);
    }

    protected abstract void setText();

    protected abstract TextFieldWidget createTextField();

    protected boolean wasTextboxKey(char c, int i) {
        return Character.isLetter(c) || Character.isDigit(c) || isTextboxKey(c, i);
    }

    @Override
    public void init() {
        super.init();
        text = createTextField();
        text.setMaxStringLength(TransporterFilter.MAX_LENGTH);
        text.setFocused(true);
    }

    @Override
    public void tick() {
        super.tick();
        text.tick();
    }

    @Override
    public void keyTyped(char c, int i) throws IOException {
        if (!text.isFocused() || i == GLFW.GLFW_KEY_ESCAPE) {
            super.keyTyped(c, i);
        }
        if (text.isFocused() && i == GLFW.GLFW_KEY_ENTER) {
            setText();
            return;
        }
        if (wasTextboxKey(c, i)) {
            text.textboxKeyTyped(c, i);
        }
    }
}