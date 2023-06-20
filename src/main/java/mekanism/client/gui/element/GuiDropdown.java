package mekanism.client.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.inventory.GuiComponents.IDropdownEnum;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

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
        this.clickSound = MekanismSounds.BEEP;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        super.onClick(mouseX, mouseY, button);
        isHolding = true;
        setOpen(!isOpen || mouseY > getY() + 11);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        if (isHolding) {
            isHolding = false;
            if (isOpen && mouseY > getY() + 11) {
                handler.accept(options[getHoveredIndex(mouseX, mouseY)]);
                setOpen(false);
            }
        }
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        int maxWidth = width - 11;
        TYPE current = curType.get();
        drawScaledTextScaledBound(guiGraphics, current.getShortName(), relativeX + 4, relativeY + 2, screenTextColor(), maxWidth, 0.8F);
        if (isOpen) {
            for (int i = 0; i < options.length; i++) {
                drawScaledTextScaledBound(guiGraphics, options[i].getShortName(), relativeX + 4, relativeY + 11 + 2 + 10 * i, screenTextColor(), maxWidth, 0.8F);
            }
        }
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        //TODO - 1.20: Evaluate if we still need the translations here
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        //TODO: Figure out why we need a translation of 1 to fix the text intersecting for the dictionary but it works just fine
        // for the QIO item viewer
        pose.translate(0, 0, 1);
        renderBackgroundTexture(guiGraphics, getResource(), GuiInnerScreen.SCREEN_SIZE, GuiInnerScreen.SCREEN_SIZE);

        int index = getHoveredIndex(mouseX, mouseY);
        if (index != -1) {
            GuiUtils.drawOutline(guiGraphics, relativeX + 1, relativeY + 12 + index * 10, width - 2, 10, screenTextColor());
        }

        TYPE current = curType.get();
        if (current.getIcon() != null) {
            guiGraphics.blit(current.getIcon(), relativeX + width - 9, relativeY + 3, 0, 0, 6, 6, 6, 6);
        }

        if (isOpen) {
            for (int i = 0; i < options.length; i++) {
                ResourceLocation icon = options[i].getIcon();
                if (icon != null) {
                    guiGraphics.blit(icon, relativeX + width - 9, relativeY + 12 + 2 + 10 * i, 0, 0, 6, 6, 6, 6);
                }
            }
        }
        pose.popPose();
    }

    @Override
    public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderToolTip(guiGraphics, mouseX, mouseY);
        int index = getHoveredIndex(mouseX, mouseY);
        if (index != -1) {
            Component text = options[index].getTooltip();
            if (text != null) {
                displayTooltips(guiGraphics, mouseX, mouseY, options[index].getTooltip());
            }
        }
    }

    private int getHoveredIndex(double mouseX, double mouseY) {
        if (isOpen && mouseX >= getX() && mouseX < getX() + width && mouseY >= getY() + 11 && mouseY < getY() + height) {
            return Math.max(0, Math.min(options.length - 1, (int) ((mouseY - getY() - 11) / 10)));
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
