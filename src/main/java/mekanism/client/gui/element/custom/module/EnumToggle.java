package mekanism.client.gui.element.custom.module;

import java.util.List;
import mekanism.api.gear.config.IHasModeIcon;
import mekanism.api.gear.config.ModuleEnumConfig;
import mekanism.api.text.IHasTextComponent;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.element.scroll.GuiScrollList;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

class EnumToggle<TYPE extends Enum<TYPE> & IHasTextComponent> extends MiniElement<TYPE> {

    private static final ResourceLocation SLIDER = MekanismUtils.getResource(ResourceType.GUI, "slider.png");
    private static final float TEXT_SCALE = 0.7F;
    private static final int BAR_START = 10;

    private final List<TYPE> enumConstants;
    private final int BAR_LENGTH;
    private final int optionDistance;
    private final boolean usesIcons;
    boolean dragging = false;

    EnumToggle(GuiModuleScreen parent, ModuleEnumConfig<TYPE> data, Component description, int xPos, int yPos) {
        super(parent, data, description, xPos, yPos);
        BAR_LENGTH = this.parent.getScreenWidth() - 24;
        enumConstants = data.getEnumConstants();
        this.optionDistance = (BAR_LENGTH / (enumConstants.size() - 1));
        this.usesIcons = enumConstants.stream().findFirst().filter(option -> option instanceof IHasModeIcon).isPresent();
    }

    @Override
    protected int getNeededHeight() {
        return usesIcons ? 31 : 28;
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int center = optionDistance * data.get().ordinal();
        guiGraphics.blit(SLIDER, getRelativeX() + BAR_START + center - 2, getRelativeY() + 11, 0, 0, 5, 6, 8, 8);
        guiGraphics.blit(SLIDER, getRelativeX() + BAR_START, getRelativeY() + 17, 0, 6, BAR_LENGTH, 2, 8, 8);
    }

    @Override
    protected void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int textColor = parent.screenTextColor();
        Component description = this.description;
        if (usesIcons) {
            description = MekanismLang.GENERIC_STORED.translate(description, data.get());
        }
        parent.drawScaledTextScaledBound(guiGraphics, description, getRelativeX() + 3, getRelativeY(), textColor, this.parent.getScreenWidth() - 3 - GuiScrollList.TEXTURE_WIDTH, 0.8F);
        for (TYPE option : enumConstants) {
            Component text = option.getTextComponent();
            //Similar to logic for drawScaledCenteredText except shifts values slightly if they go past the max length
            int textWidth = parent.getStringWidth(text);
            float widthScaling = usesIcons ? 2.5F : (textWidth / 2F) * TEXT_SCALE;
            int optionCenter = BAR_START + optionDistance * option.ordinal();
            float left = optionCenter - widthScaling;
            if (left < 0) {
                left = 0;
            } else {
                int max = parent.getScreenWidth() - 1;
                float objectWidth = usesIcons ? 5 : textWidth * TEXT_SCALE;
                int end = xPos + Mth.ceil(left + objectWidth);
                if (end > max) {
                    left -= end - max;
                }
            }
            int color = textColor;
            if (text.getStyle().getColor() != null) {
                color = 0xFF000000 | text.getStyle().getColor().getValue();
            }
            GuiUtils.fill(guiGraphics, getRelativeX() + optionCenter, getRelativeY() + 17, 1, 3, color);
            if (usesIcons) {
                IHasModeIcon hasModeIcon = (IHasModeIcon) option;
                guiGraphics.blit(hasModeIcon.getModeIcon(), getRelativeX() + optionCenter - 8, getRelativeY() + 19, 0, 0, 16, 16, 16, 16);
            } else {
                parent.drawTextWithScale(guiGraphics, text, getRelativeX() + left, getRelativeY() + 20, textColor, TEXT_SCALE);
            }
        }
    }

    @Override
    protected void click(double mouseX, double mouseY) {
        if (!dragging) {
            int center = optionDistance * data.get().ordinal();
            if (mouseOver(mouseX, mouseY, BAR_START + center - 2, 11, 5, 6)) {
                dragging = true;
            } else if (mouseOver(mouseX, mouseY, BAR_START, 10, BAR_LENGTH, 12)) {
                setDataFromPosition(mouseX);
            }
        }
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (dragging) {
            setDataFromPosition(mouseX);
        }
    }

    private void setDataFromPosition(double mouseX) {
        List<TYPE> options = enumConstants;
        int size = options.size() - 1;
        int cur = (int) Math.round(((mouseX - getX() - BAR_START) / BAR_LENGTH) * size);
        cur = Mth.clamp(cur, 0, size);
        if (cur != data.get().ordinal()) {
            setData(options.get(cur));
        }
    }

    @Override
    protected void release(double mouseX, double mouseY) {
        dragging = false;
    }
}