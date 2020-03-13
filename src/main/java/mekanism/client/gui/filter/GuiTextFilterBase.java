package mekanism.client.gui.filter;

import javax.annotation.Nonnull;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public abstract class GuiTextFilterBase<FILTER extends IFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>, CONTAINER extends
      FilterContainer<FILTER, TILE>> extends GuiFilterBase<FILTER, TILE, CONTAINER> {

    protected ItemStack renderStack = ItemStack.EMPTY;
    protected TextFieldWidget text;

    protected GuiTextFilterBase(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    protected abstract void setText();

    protected abstract TextFieldWidget createTextField();

    protected boolean wasTextboxKey(char c, int i) {
        return Character.isLetter(c) || Character.isDigit(c);
    }

    @Override
    public void init() {
        super.init();
        addButton(text = createTextField());
        text.setMaxStringLength(TransporterFilter.MAX_LENGTH);
        text.changeFocus(true);
    }

    @Override
    public void resize(@Nonnull Minecraft minecraft, int scaledWidth, int scaledHeight) {
        String s = text.getText();
        super.resize(minecraft, scaledWidth, scaledHeight);
        text.setText(s);
    }

    @Override
    public void tick() {
        super.tick();
        text.tick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (text.canWrite()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape making the field lose focus
                text.setFocused2(false);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                setText();
                return true;
            }
            return text.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        if (text.canWrite()) {
            if (wasTextboxKey(c, keyCode)) {
                //Only allow a subset of characters to be entered into the frequency text box
                return text.charTyped(c, keyCode);
            }
            return false;
        }
        return super.charTyped(c, keyCode);
    }
}