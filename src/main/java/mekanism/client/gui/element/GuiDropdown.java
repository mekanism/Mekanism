package mekanism.client.gui.element;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.inventory.GuiComponents.IDropdownEnum;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class GuiDropdown<TYPE extends Enum<TYPE> & IDropdownEnum<TYPE>> extends GuiTexturedElement {

    private static final int ELEMENT_HEIGHT = 12;
    private static final int ICON_SIZE = 6;
    //one for the screen border, one for the hover border, and one more to have a spot between the hover border and the icon
    private static final int ICON_OFFSET = ICON_SIZE + 3;

    private final Map<TYPE, Tooltip> typeTooltips;
    private final Consumer<TYPE> handler;
    private final Supplier<TYPE> curType;
    private final TYPE[] options;

    @Nullable
    private ScreenRectangle cachedTooltipRect;

    private long msOpened;
    private boolean isOpen;

    public GuiDropdown(IGuiWrapper gui, int x, int y, int width, Class<TYPE> enumClass, Supplier<TYPE> curType, Consumer<TYPE> handler) {
        super(GuiInnerScreen.SCREEN, gui, x, y, width, ELEMENT_HEIGHT);
        this.curType = curType;
        this.handler = handler;
        this.options = enumClass.getEnumConstants();
        this.typeTooltips = new EnumMap<>(enumClass);
        this.active = true;
        this.clickSound = MekanismSounds.BEEP_ON;
        this.clickVolume = 1.0F;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        super.onClick(event, isDoubleClick);
        setDragging(true);
        setOpen(!isOpen || event.y() > getY() + 11);
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        boolean wasDragging = isDragging();
        super.onRelease(event);
        if (wasDragging && isOpen && event.y() > getY() + 11) {
            int hoveredIndex = getHoveredIndex(event.x(), event.y());
            if (hoveredIndex != -1) {
                handler.accept(options[hoveredIndex]);
            }
            setOpen(false);
        }
    }

    @Override
    public void renderForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        drawOptionName(guiGraphics, curType.get(), 2, true);
        if (isOpen) {
            for (int i = 0; i < options.length; i++) {
                drawOptionName(guiGraphics, options[i], ELEMENT_HEIGHT + 1 + 10 * i, false);
            }
        }
    }

    private void drawOptionName(GuiGraphicsExtractor guiGraphics, TYPE option, int y, boolean alwaysDisplayed) {
        //Note: We add one for if we are rendering with an icon so that we allow going closer to the icon
        int maxWidth = option.getIcon() == null ? getWidth() : getWidth() - ICON_OFFSET + 1;
        drawScaledScrollingString(guiGraphics, option.getShortName(), 0, y, TextAlignment.LEFT, screenTextColor(), maxWidth, 10, 3, false,
              0.8F, alwaysDisplayed ? getTimeOpened() : msOpened);
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);

        int index = getHoveredIndex(mouseX, mouseY);
        if (index != -1) {
            guiGraphics.outline(relativeX + 1, relativeY + ELEMENT_HEIGHT + index * 10, width - 2, 10, screenTextColor());
        }

        TYPE current = curType.get();
        if (current.getIcon() != null) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, current.getIcon(), relativeX + width - ICON_OFFSET, relativeY + 3, ICON_SIZE, ICON_SIZE);
        }

        if (isOpen) {
            for (int i = 0; i < options.length; i++) {
                Identifier icon = options[i].getIcon();
                if (icon != null) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, relativeX + width - ICON_OFFSET, relativeY + ELEMENT_HEIGHT + 2 + 10 * i, ICON_SIZE, ICON_SIZE);
                }
            }
        }
    }

    @Override
    protected ScreenRectangle getTooltipRectangle(int mouseX, int mouseY) {
        return cachedTooltipRect == null ? super.getTooltipRectangle(mouseX, mouseY) : cachedTooltipRect;
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        int index = getHoveredIndex(mouseX, mouseY);
        if (index != -1) {
            Tooltip text = typeTooltips.computeIfAbsent(options[index], t -> TooltipUtils.create(t.getTooltip()));
            cachedTooltipRect = new ScreenRectangle(getX() + 1, getY() + ELEMENT_HEIGHT + index * 10, width - 2, 10);
            setTooltip(text);
        } else {
            clearTooltip();
            cachedTooltipRect = null;
        }
    }

    private int getHoveredIndex(double mouseX, double mouseY) {
        if (isOpen && mouseX >= getX() && mouseX < getRight() && mouseY >= getY() + 11 && mouseY < getBottom()) {
            return Math.clamp((int) ((mouseY - getY() - 11) / 10), 0, options.length - 1);
        }
        return -1;
    }

    private void setOpen(boolean open) {
        if (isOpen != open) {
            isOpen = open;
            if (isOpen) {
                height += options.length * 10 + 1;
                msOpened = Util.getMillis();
            } else {
                height = ELEMENT_HEIGHT;
            }
        }
    }

    @Override
    public void syncFrom(GuiElement element) {
        super.syncFrom(element);
        GuiDropdown<?> old = (GuiDropdown<?>) element;
        if (old.isOpen) {
            //Sync the fact it is open, and how long it has been open for
            isOpen = true;
            height += options.length * 10 + 1;
            msOpened = old.msOpened;
        }
    }
}
