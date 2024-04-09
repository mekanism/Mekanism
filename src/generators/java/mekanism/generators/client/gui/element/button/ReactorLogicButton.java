package mekanism.generators.client.gui.element.button;

import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.base.IReactorLogic;
import mekanism.generators.common.base.IReactorLogicMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReactorLogicButton<TYPE extends Enum<TYPE> & IReactorLogicMode<TYPE>> extends MekanismButton {

    private static final ResourceLocation TEXTURE = MekanismGenerators.rl(ResourceType.GUI_BUTTON.getPrefix() + "reactor_logic.png");
    @NotNull
    private final IReactorLogic<TYPE> tile;
    private final int typeOffset;
    private final Supplier<@Nullable TYPE> modeSupplier;
    private final Consumer<TYPE> onPress;


    public ReactorLogicButton(IGuiWrapper gui, int x, int y, int index, @NotNull IReactorLogic<TYPE> tile, IntSupplier indexSupplier, Supplier<TYPE[]> modeList,
          Consumer<TYPE> onPress) {
        this(gui, x, y, index, tile, onPress, () -> {
            int i = indexSupplier.getAsInt() + index;
            TYPE[] modes = modeList.get();
            return i >= 0 && i < modes.length ? modes[i] : null;
        });
    }

    private ReactorLogicButton(IGuiWrapper gui, int x, int y, int index, @NotNull IReactorLogic<TYPE> tile, Consumer<TYPE> onPress, Supplier<@Nullable TYPE> modeSupplier) {
        super(gui, x, y, 128, 22, Component.empty(), (element, mouseX, mouseY) -> ((ReactorLogicButton<?>) element).click(), (element, guiGraphics, mouseX, mouseY) -> {
            IReactorLogicMode<?> mode = ((ReactorLogicButton<?>) element).modeSupplier.get();
            if (mode != null) {
                element.displayTooltips(guiGraphics, mouseX, mouseY, mode.getDescription());
            }
        });
        this.onPress = onPress;
        this.typeOffset = 22 * index;
        this.modeSupplier = modeSupplier;
        this.tile = tile;
    }

    private boolean click() {
        TYPE mode = modeSupplier.get();
        if (mode != null) {
            onPress.accept(mode);
        }
        return true;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        TYPE mode = modeSupplier.get();
        if (mode != null) {
            MekanismRenderer.color(guiGraphics, mode.getColor());
            guiGraphics.blit(TEXTURE, getButtonX(), getButtonY(), 0, mode == tile.getMode() ? 22 : 0, getButtonWidth(), getButtonHeight(), 128, 44);
            MekanismRenderer.resetColor(guiGraphics);
        }
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        TYPE mode = modeSupplier.get();
        if (mode != null) {
            gui().renderItem(guiGraphics, mode.getRenderStack(), 20, 35 + typeOffset);
            drawString(guiGraphics, TextComponentUtil.build(EnumColor.WHITE, mode), 39, 34 + typeOffset, titleTextColor());
            super.renderForeground(guiGraphics, mouseX, mouseY);
        }
    }
}