package mekanism.generators.client.gui.element.button;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.scroll.GuiInstallableScrollList;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.generators.common.base.IReactorLogic;
import mekanism.generators.common.base.IReactorLogicMode;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import org.jspecify.annotations.Nullable;

public class ReactorLogicButton<TYPE extends Enum<TYPE> & IReactorLogicMode<TYPE>> extends MekanismButton {

    private final IReactorLogic<TYPE> tile;
    private final Supplier<@Nullable TYPE> modeSupplier;
    private final Map<TYPE, Tooltip> typeTooltips;
    private final Consumer<TYPE> onPress;


    public ReactorLogicButton(IGuiWrapper gui, int x, int y, int index, IReactorLogic<TYPE> tile, Class<TYPE> clazz, IntSupplier indexSupplier, Supplier<TYPE[]> modeList,
          Consumer<TYPE> onPress) {
        this(gui, x, y, tile, clazz, onPress, () -> {
            int i = indexSupplier.getAsInt() + index;
            TYPE[] modes = modeList.get();
            return i >= 0 && i < modes.length ? modes[i] : null;
        });
    }

    private ReactorLogicButton(IGuiWrapper gui, int x, int y, IReactorLogic<TYPE> tile, Class<TYPE> clazz, Consumer<TYPE> onPress, Supplier<@Nullable TYPE> modeSupplier) {
        super(gui, x, y, 128, 22, CommonComponents.EMPTY, (element, _, _) -> ((ReactorLogicButton<?>) element).click());
        this.onPress = onPress;
        this.modeSupplier = modeSupplier;
        this.tile = tile;
        this.typeTooltips = new EnumMap<>(clazz);
    }

    private boolean click() {
        TYPE mode = modeSupplier.get();
        if (mode != null) {
            onPress.accept(mode);
        }
        return true;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        TYPE mode = modeSupplier.get();
        if (mode != null) {
            int color = mode.getColor().getPackedColor();
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, mode == tile.getMode() ? GuiInstallableScrollList.SELECTED : GuiInstallableScrollList.BASE,
                  relativeX, relativeY, getButtonWidth(), getButtonHeight(), color);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SlotType.NORMAL.getTexture(), relativeX + 2, relativeY + 2, SlotType.SLOT_SIZE, SlotType.SLOT_SIZE, color);
        }
    }

    @Override
    public void renderForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        TYPE mode = modeSupplier.get();
        if (mode != null) {
            guiGraphics.item(mode.getRenderStack(), relativeX + 3, relativeY + 3);
            drawScrollingString(guiGraphics, TextComponentUtil.build(EnumColor.WHITE, mode), 20, 2, TextAlignment.LEFT, titleTextColor(), width - 20, 2, false);
            super.renderForeground(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        TYPE mode = modeSupplier.get();
        if (mode == null) {
            clearTooltip();
        } else {
            setTooltip(typeTooltips.computeIfAbsent(mode, m -> TooltipUtils.create(m.getDescription())));
        }
    }
}