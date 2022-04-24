package mekanism.generators.client.gui.element.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.base.IReactorLogic;
import mekanism.generators.common.base.IReactorLogicMode;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class ReactorLogicButton<TYPE extends Enum<TYPE> & IReactorLogicMode<TYPE>> extends MekanismButton {

    private static final ResourceLocation TEXTURE = MekanismGenerators.rl(ResourceType.GUI_BUTTON.getPrefix() + "reactor_logic.png");
    @Nonnull
    private final IReactorLogic<TYPE> tile;
    private final int index;
    private final IntSupplier indexSupplier;
    private final Supplier<TYPE[]> modeList;
    private final Consumer<TYPE> onPress;

    public ReactorLogicButton(IGuiWrapper gui, int x, int y, int index, @Nonnull IReactorLogic<TYPE> tile, IntSupplier indexSupplier,
          Supplier<TYPE[]> listSupplier, Consumer<TYPE> onPress) {
        super(gui, x, y, 128, 22, TextComponent.EMPTY, null, null);
        this.index = index;
        this.indexSupplier = indexSupplier;
        this.modeList = listSupplier;
        this.tile = tile;
        this.onPress = onPress;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        TYPE mode = getMode();
        if (mode != null) {
            onPress.accept(mode);
        }
    }

    @Override
    public void renderToolTip(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        TYPE mode = getMode();
        if (mode != null) {
            displayTooltips(matrix, mouseX, mouseY, mode.getDescription());
        }
    }

    @Override
    public void drawBackground(@Nonnull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        TYPE mode = getMode();
        if (mode == null) {
            return;
        }
        RenderSystem.setShaderTexture(0, TEXTURE);
        MekanismRenderer.color(mode.getColor());
        blit(matrix, x, y, 0, mode == tile.getMode() ? 22 : 0, width, height, 128, 44);
        MekanismRenderer.resetColor();
    }

    private TYPE getMode() {
        int i = indexSupplier.getAsInt() + index;
        return i >= 0 && i < modeList.get().length ? modeList.get()[i] : null;
    }

    @Override
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        TYPE mode = getMode();
        if (mode != null) {
            int typeOffset = 22 * index;
            gui().renderItem(matrix, mode.getRenderStack(), 20, 35 + typeOffset);
            drawString(matrix, TextComponentUtil.build(EnumColor.WHITE, mode), 39, 34 + typeOffset, titleTextColor());
            super.renderForeground(matrix, mouseX, mouseY);
        }
    }
}