package mekanism.client.gui.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.inventory.GuiComponents.IDropdownEnum;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiDropdown<TYPE extends Enum<TYPE> & IDropdownEnum<TYPE>> extends GuiTexturedElement {

    private final Consumer<TYPE> handler;
    private final Supplier<TYPE> curType;
    private final TYPE[] options;

    private boolean isOpen;
    private boolean isHolding;

    public GuiDropdown(IGuiWrapper gui, int x, int y, int width, Class<TYPE> enumClass, Supplier<TYPE> curType, Consumer<TYPE> handler) {
        super(GuiInnerScreen.SCREEN, gui, x, y, width, 12);
        this.curType = curType;
        this.handler = handler;
        this.options = enumClass.getEnumConstants();
        this.field_230693_o_ = true;
    }

    @Override
    public void func_230982_a_(double mouseX, double mouseY) {
        super.func_230982_a_(mouseX, mouseY);
        isHolding = true;
        setOpen(!isOpen || !(mouseY <= field_230691_m_ + 11));
        minecraft.getSoundHandler().play(SimpleSound.master(MekanismSounds.BEEP.get(), 1.0F));
    }

    @Override
    public void func_231000_a__(double mouseX, double mouseY) {
        super.func_231000_a__(mouseX, mouseY);

        if (isHolding) {
            isHolding = false;
            if (isOpen && mouseY > field_230691_m_ + 11) {
                handler.accept(options[getHoveredIndex(mouseX, mouseY)]);
                setOpen(false);
            }
        }
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);

        TYPE current = curType.get();
        drawScaledTextScaledBound(matrix, current.getShortName(), relativeX + 4, relativeY + 2, screenTextColor(), 30, 0.8F);

        if (isOpen) {
            for (int i = 0; i < options.length; i++) {
                drawScaledTextScaledBound(matrix, options[i].getShortName(), relativeX + 4, relativeY + 11 + 2 + 10 * i, screenTextColor(), 30, 0.8F);
            }
        }
    }

    @Override
    public void drawButton(MatrixStack matrix, int mouseX, int mouseY) {
        renderBackgroundTexture(matrix, getResource(), 4, 4);

        int index = getHoveredIndex(mouseX, mouseY);
        if (index != -1) {
            GuiUtils.drawOutline(matrix, field_230690_l_ + 1, field_230691_m_ + 12 + index * 10, field_230688_j_ - 2, 10, screenTextColor());
        }

        TYPE current = curType.get();
        if (current.getIcon() != null) {
            minecraft.textureManager.bindTexture(current.getIcon());
            func_238463_a_(matrix, field_230690_l_ + field_230688_j_ - 9, field_230691_m_ + 3, 0, 0, 6, 6, 6, 6);
        }

        if (isOpen) {
            for (int i = 0; i < options.length; i++) {
                ResourceLocation icon = options[i].getIcon();
                if (icon != null) {
                    minecraft.textureManager.bindTexture(options[i].getIcon());
                    func_238463_a_(matrix, field_230690_l_ + field_230688_j_ - 9, field_230691_m_ + 12 + 2 + 10 * i, 0, 0, 6, 6, 6, 6);
                }
            }
        }
    }

    @Override
    public void func_230443_a_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        int index = getHoveredIndex(mouseX + guiObj.getLeft(), mouseY + guiObj.getTop());
        if (index != -1) {
            ITextComponent text = options[index].getTooltip();
            if (text != null) {
                displayTooltip(matrix, options[index].getTooltip(), mouseX, mouseY);
            }
        }
    }

    private int getHoveredIndex(double mouseX, double mouseY) {
        if (isOpen && mouseX >= field_230690_l_ && mouseX < field_230690_l_ + field_230688_j_ && mouseY >= field_230691_m_ + 11 && mouseY < field_230691_m_ + field_230689_k_) {
            return Math.max(0, Math.min(options.length - 1, (int) ((mouseY - field_230691_m_ - 11) / 10)));
        }
        return -1;
    }

    private void setOpen(boolean open) {
        if (isOpen != open) {
            if (open) {
                field_230689_k_ += options.length * 10 + 1;
            } else {
                field_230689_k_ -= options.length * 10 + 1;
            }
        }
        isOpen = open;
    }
}
