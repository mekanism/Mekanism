package mekanism.client.gui.filter;

import mekanism.common.content.filter.IFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
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
        return Character.isLetter(c) || Character.isDigit(c) || isTextboxKey(c, i);
    }

    @Override
    public void init() {
        super.init();
        addButton(text = createTextField());
        text.setMaxStringLength(TransporterFilter.MAX_LENGTH);
        text.setFocused2(true);
    }

    @Override
    public void tick() {
        super.tick();
        text.tick();
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (!text.isFocused() || i == GLFW.GLFW_KEY_ESCAPE) {
            return super.charTyped(c, i);
        }
        if (text.isFocused() && i == GLFW.GLFW_KEY_ENTER) {
            setText();
            return true;
        }
        if (wasTextboxKey(c, i)) {
            return text.charTyped(c, i);
        }
        return false;
    }
}