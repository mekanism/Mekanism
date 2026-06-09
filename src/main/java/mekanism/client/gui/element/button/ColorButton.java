package mekanism.client.gui.element.button;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.MekanismLang;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import org.jspecify.annotations.Nullable;

public class ColorButton extends MekanismButton {

    @Nullable
    private static final Tooltip NONE = TooltipUtils.create(MekanismLang.NONE);

    private final Map<EnumColor, Tooltip> tooltips = new EnumMap<>(EnumColor.class);
    private final Supplier<@Nullable EnumColor> colorSupplier;

    public ColorButton(IGuiWrapper gui, int x, int y, int width, int height, Supplier<@Nullable EnumColor> colorSupplier, IClickable onPress, IClickable onRightClick) {
        super(gui, x, y, width, height, CommonComponents.EMPTY, onPress, onRightClick);
        this.colorSupplier = colorSupplier;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        EnumColor color = colorSupplier.get();
        if (color != null) {
            guiGraphics.fill(getButtonX(), getButtonY(), getButtonX() + getButtonWidth(), getButtonY() + getButtonHeight(), color.getPackedColor());
        }
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        EnumColor color = colorSupplier.get();
        if (color != null) {
            setTooltip(tooltips.computeIfAbsent(color, c -> TooltipUtils.create(c.getColoredName())));
        } else {
            setTooltip(NONE);
        }
    }
}