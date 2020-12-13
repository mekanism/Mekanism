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
        this.active = true;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        isHolding = true;
        setOpen(!isOpen || mouseY > y + 11);
        minecraft.getSoundHandler().play(SimpleSound.master(MekanismSounds.BEEP.get(), 1.0F));
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        if (isHolding) {
            isHolding = false;
            if (isOpen && mouseY > y + 11) {
                handler.accept(options[getHoveredIndex(mouseX, mouseY)]);
                setOpen(false);
            }
        }
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        int maxWidth = width - 11;
        TYPE current = curType.get();
        drawScaledTextScaledBound(matrix, current.getShortName(), relativeX + 4, relativeY + 2, screenTextColor(), maxWidth, 0.8F);
        if (isOpen) {
            for (int i = 0; i < options.length; i++) {
                drawScaledTextScaledBound(matrix, options[i].getShortName(), relativeX + 4, relativeY + 11 + 2 + 10 * i, screenTextColor(), maxWidth, 0.8F);
            }
        }
    }

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        matrix.push();
        //TODO: Figure out why we need a translation of 1 to fix the text intersecting for the dictionary but it works just fine
        // for the QIO item viewer
        matrix.translate(0, 0, 1);
        renderBackgroundTexture(matrix, getResource(), 4, 4);

        int index = getHoveredIndex(mouseX, mouseY);
        if (index != -1) {
            GuiUtils.drawOutline(matrix, x + 1, y + 12 + index * 10, width - 2, 10, screenTextColor());
        }

        TYPE current = curType.get();
        if (current.getIcon() != null) {
            minecraft.textureManager.bindTexture(current.getIcon());
            blit(matrix, x + width - 9, y + 3, 0, 0, 6, 6, 6, 6);
        }

        if (isOpen) {
            for (int i = 0; i < options.length; i++) {
                ResourceLocation icon = options[i].getIcon();
                if (icon != null) {
                    minecraft.textureManager.bindTexture(icon);
                    blit(matrix, x + width - 9, y + 12 + 2 + 10 * i, 0, 0, 6, 6, 6, 6);
                }
            }
        }
        matrix.pop();
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        int index = getHoveredIndex(mouseX + getGuiLeft(), mouseY + getGuiTop());
        if (index != -1) {
            ITextComponent text = options[index].getTooltip();
            if (text != null) {
                displayTooltip(matrix, options[index].getTooltip(), mouseX, mouseY);
            }
        }
    }

    private int getHoveredIndex(double mouseX, double mouseY) {
        if (isOpen && mouseX >= x && mouseX < x + width && mouseY >= y + 11 && mouseY < y + height) {
            return Math.max(0, Math.min(options.length - 1, (int) ((mouseY - y - 11) / 10)));
        }
        return -1;
    }

    private void setOpen(boolean open) {
        if (isOpen != open) {
            if (open) {
                height += options.length * 10 + 1;
            } else {
                height -= options.length * 10 + 1;
            }
        }
        isOpen = open;
    }
}
