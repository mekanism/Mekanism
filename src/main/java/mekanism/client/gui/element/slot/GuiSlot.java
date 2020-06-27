package mekanism.client.gui.element.slot;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.jei.interfaces.IJEIGhostTarget;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class GuiSlot extends GuiTexturedElement implements IJEIGhostTarget {

    private static final int INVALID_SLOT_COLOR = MekanismRenderer.getColorARGB(EnumColor.DARK_RED, 0.8F);
    public static final int DEFAULT_HOVER_COLOR = 0x80FFFFFF;
    private boolean hasValidityCheck;
    private Supplier<ItemStack> validityCheck = () -> ItemStack.EMPTY;
    private Supplier<SlotOverlay> overlaySupplier;
    private IntSupplier overlayColorSupplier;
    private SlotOverlay overlay;
    private IHoverable onHover;
    private IClickable onClick;
    private boolean renderHover;

    @Nullable
    private IGhostIngredientConsumer ghostHandler;

    public GuiSlot(SlotType type, IGuiWrapper gui, int x, int y) {
        super(type.getTexture(), gui, x, y, type.getWidth(), type.getHeight());
        field_230693_o_ = false;
    }

    public GuiSlot validity(Supplier<ItemStack> validityCheck) {
        hasValidityCheck = true;
        this.validityCheck = validityCheck;
        return this;
    }

    public GuiSlot hover(IHoverable onHover) {
        this.onHover = onHover;
        return this;
    }

    public GuiSlot click(IClickable onClick) {
        this.onClick = onClick;
        return this;
    }

    public GuiSlot with(SlotOverlay overlay) {
        this.overlay = overlay;
        return this;
    }

    public GuiSlot overlayColor(IntSupplier colorSupplier) {
        overlayColorSupplier = colorSupplier;
        return this;
    }

    public GuiSlot with(Supplier<SlotOverlay> overlaySupplier) {
        this.overlaySupplier = overlaySupplier;
        return this;
    }

    public GuiSlot setRenderHover(boolean renderHover) {
        this.renderHover = renderHover;
        return this;
    }

    public GuiSlot setGhostHandler(@Nullable IGhostIngredientConsumer ghostHandler) {
        this.ghostHandler = ghostHandler;
        return this;
    }

    @Override
    public void func_230431_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        func_238463_a_(matrix, field_230690_l_, field_230691_m_, 0, 0, field_230688_j_, field_230689_k_, field_230688_j_, field_230689_k_);
        if (hasValidityCheck) {
            ItemStack invalid = validityCheck.get();
            if (!invalid.isEmpty()) {
                int xPos = field_230690_l_ + 1;
                int yPos = field_230691_m_ + 1;
                func_238467_a_(matrix, xPos, yPos, xPos + 16, yPos + 16, INVALID_SLOT_COLOR);
                MekanismRenderer.resetColor();
                guiObj.renderItem(matrix, invalid, xPos, yPos);
            }
        }
        if (overlaySupplier != null) {
            overlay = overlaySupplier.get();
        }
        if (overlay != null) {
            minecraft.textureManager.bindTexture(overlay.getTexture());
            func_238463_a_(matrix, field_230690_l_, field_230691_m_, 0, 0, overlay.getWidth(), overlay.getHeight(), overlay.getWidth(), overlay.getHeight());
        }
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        if (renderHover && func_230449_g_()) {
            int xPos = relativeX + 1;
            int yPos = relativeY + 1;
            func_238467_a_(matrix, xPos, yPos, xPos + 16, yPos + 16, DEFAULT_HOVER_COLOR);
            MekanismRenderer.resetColor();
        }
        if (overlayColorSupplier != null) {
            matrix.push();
            matrix.translate(0, 0, 10);
            int xPos = relativeX + 1;
            int yPos = relativeY + 1;
            func_238467_a_(matrix, xPos, yPos, xPos + 16, yPos + 16, overlayColorSupplier.getAsInt());
            matrix.translate(0, 0, -10);
            matrix.pop();
            MekanismRenderer.resetColor();
        }
        if (func_230449_g_()) {
            //TODO: Should it pass it the proper mouseX and mouseY. Probably, though buttons may have to be redone slightly then
            func_230443_a_(matrix, mouseX - guiObj.getLeft(), mouseY - guiObj.getTop());
        }
    }

    @Override
    public void func_230443_a_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        if (onHover != null) {
            onHover.onHover(this, matrix, mouseX, mouseY);
        }
    }

    @Override
    public boolean func_231044_a_(double mouseX, double mouseY, int button) {
        if (onClick != null && func_230987_a_(button)) {
            if (mouseX >= field_230690_l_ && mouseY >= field_230691_m_ && mouseX < field_230690_l_ + field_230688_j_ && mouseY < field_230691_m_ + field_230689_k_) {
                onClick.onClick(this, (int) mouseX, (int) mouseY);
                func_230988_a_(Minecraft.getInstance().getSoundHandler());
                return true;
            }
        }

        return false;
    }

    @Nullable
    @Override
    public IGhostIngredientConsumer getGhostHandler() {
        return ghostHandler;
    }

    @Override
    public int borderSize() {
        return 1;
    }
}