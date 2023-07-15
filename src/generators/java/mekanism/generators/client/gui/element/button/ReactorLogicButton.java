package mekanism.generators.client.gui.element.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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


    public ReactorLogicButton(IGuiWrapper gui, int x, int y, int index, @NotNull IReactorLogic<TYPE> tile, IntSupplier indexSupplier, Supplier<TYPE[]> modeList,
          Consumer<TYPE> onPress) {
        this(gui, x, y, index, tile, onPress, () -> {
            int i = indexSupplier.getAsInt() + index;
            TYPE[] modes = modeList.get();
            return i >= 0 && i < modes.length ? modes[i] : null;
        });
    }

    private ReactorLogicButton(IGuiWrapper gui, int x, int y, int index, @NotNull IReactorLogic<TYPE> tile, Consumer<TYPE> onPress, Supplier<@Nullable TYPE> modeSupplier) {
        super(gui, x, y, 128, 22, Component.empty(), () -> {
            TYPE mode = modeSupplier.get();
            if (mode != null) {
                onPress.accept(mode);
            }
        }, (onHover, matrix, mouseX, mouseY) -> {
            TYPE mode = modeSupplier.get();
            if (mode != null) {
                gui.displayTooltips(matrix, mouseX, mouseY, mode.getDescription());
            }
        });
        this.typeOffset = 22 * index;
        this.modeSupplier = modeSupplier;
        this.tile = tile;
    }

    @Override
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        TYPE mode = modeSupplier.get();
        if (mode != null) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            MekanismRenderer.color(mode.getColor());
            blit(matrix, x, y, 0, mode == tile.getMode() ? 22 : 0, width, height, 128, 44);
            MekanismRenderer.resetColor();
        }
    }

    @Override
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        TYPE mode = modeSupplier.get();
        if (mode != null) {
            gui().renderItem(matrix, mode.getRenderStack(), 20, 35 + typeOffset);
            drawString(matrix, TextComponentUtil.build(EnumColor.WHITE, mode), 39, 34 + typeOffset, titleTextColor());
            super.renderForeground(matrix, mouseX, mouseY);
        }
    }
}