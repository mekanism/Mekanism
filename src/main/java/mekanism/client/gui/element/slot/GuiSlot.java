package mekanism.client.gui.element.slot;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerGhostTarget;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.warning.ISupportsWarning;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiSlot extends GuiTexturedElement implements IRecipeViewerGhostTarget, ISupportsWarning<GuiSlot> {

    private static final int INVALID_SLOT_COLOR = MekanismRenderer.getColorARGB(EnumColor.DARK_RED, 0.8F);
    public static final int DEFAULT_HOVER_COLOR = 0x80FFFFFF;
    private final SlotType slotType;
    private Supplier<ItemStack> validityCheck;
    private Supplier<ItemStack> storedStackSupplier;
    private Supplier<SlotOverlay> overlaySupplier;
    @Nullable
    private BooleanSupplier warningSupplier;
    @Nullable
    private IntSupplier overlayColorSupplier;
    @Nullable
    private SlotOverlay overlay;
    @Nullable
    private IHoverable onHover;
    @Nullable
    private IClickable onClick;
    private boolean renderHover;
    private boolean renderAboveSlots;

    @Nullable
    private IGhostIngredientConsumer ghostHandler;

    public GuiSlot(SlotType type, IGuiWrapper gui, int x, int y) {
        super(type.getTexture(), gui, x, y, type.getWidth(), type.getHeight());
        this.slotType = type;
        active = false;
    }

    public GuiSlot validity(Supplier<ItemStack> validityCheck) {
        //TODO - 1.18: Evaluate if any of these validity things should be moved to the warning system
        this.validityCheck = validityCheck;
        return this;
    }

    @Override
    public GuiSlot warning(@NotNull WarningType type, @NotNull BooleanSupplier warningSupplier) {
        this.warningSupplier = ISupportsWarning.compound(this.warningSupplier, gui().trackWarning(type, warningSupplier));
        return this;
    }

    /**
     * @apiNote For use when there is no validity check and this is a "fake" slot in that the container screen doesn't render the item by default.
     */
    public GuiSlot stored(Supplier<ItemStack> storedStackSupplier) {
        this.storedStackSupplier = storedStackSupplier;
        return this;
    }

    public GuiSlot hover(IHoverable onHover) {
        this.onHover = onHover;
        return this;
    }

    public GuiSlot click(IClickable onClick) {
        //Use default click sound
        return click(onClick, SoundEvents.UI_BUTTON_CLICK);
    }

    public GuiSlot click(IClickable onClick, @Nullable Holder<SoundEvent> clickSound) {
        this.clickSound = clickSound;
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

    public GuiSlot setRenderAboveSlots() {
        this.renderAboveSlots = true;
        return this;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!renderAboveSlots) {
            draw(guiGraphics);
        }
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (renderAboveSlots) {
            draw(guiGraphics);
        }
    }

    private void draw(@NotNull GuiGraphics guiGraphics) {
        ResourceLocation texture;
        if (warningSupplier != null && warningSupplier.getAsBoolean()) {
            texture = slotType.getWarningTexture();
        } else {
            texture = getResource();
        }
        guiGraphics.blit(texture, relativeX, relativeY, 0, 0, width, height, width, height);
        if (overlaySupplier != null) {
            overlay = overlaySupplier.get();
        }
        if (overlay != null) {
            guiGraphics.blit(overlay.getTexture(), relativeX, relativeY, 0, 0, overlay.getWidth(), overlay.getHeight(), overlay.getWidth(), overlay.getHeight());
        }
        drawContents(guiGraphics);
    }

    protected void drawContents(@NotNull GuiGraphics guiGraphics) {
        if (validityCheck != null) {
            ItemStack invalid = validityCheck.get();
            if (!invalid.isEmpty()) {
                int xPos = relativeX + 1;
                int yPos = relativeY + 1;
                guiGraphics.fill(xPos, yPos, xPos + 16, yPos + 16, INVALID_SLOT_COLOR);
                gui().renderItem(guiGraphics, invalid, xPos, yPos);
            }
        } else if (storedStackSupplier != null) {
            ItemStack stored = storedStackSupplier.get();
            if (!stored.isEmpty()) {
                gui().renderItem(guiGraphics, stored, relativeX + 1, relativeY + 1);
            }
        }
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        boolean hovered = checkWindows(mouseX, mouseY, isHovered());
        if (renderHover && hovered) {
            int xPos = relativeX + 1;
            int yPos = relativeY + 1;
            guiGraphics.fill(RenderType.guiOverlay(), xPos, yPos, xPos + 16, yPos + 16, DEFAULT_HOVER_COLOR);
        }
        if (overlayColorSupplier != null) {
            int xPos = relativeX + 1;
            int yPos = relativeY + 1;
            guiGraphics.fill(RenderType.guiOverlay(), xPos, yPos, xPos + 16, yPos + 16, overlayColorSupplier.getAsInt());
        }
        if (hovered) {
            //TODO: Should it pass it the proper mouseX and mouseY. Probably, though buttons may have to be redone slightly then
            renderToolTip(guiGraphics, mouseX - getGuiLeft(), mouseY - getGuiTop());
        }
    }

    @Override
    public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderToolTip(guiGraphics, mouseX, mouseY);
        if (onHover != null) {
            onHover.onHover(this, guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (onClick != null && isValidClickButton(button)) {
            if (mouseX >= getX() + borderSize() && mouseY >= getY() + borderSize() && mouseX < getRight() - borderSize() && mouseY < getBottom() - borderSize()) {
                if (onClick.onClick(this, mouseX, mouseY)) {
                    playDownSound(minecraft.getSoundManager());
                    return true;
                }
                //If clicking the slot fails check super as maybe it has children that can handle clicks
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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