package mekanism.client.gui.element.slot;

import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerGhostTarget;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.warning.ISupportsWarning;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
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
    private Function<GuiSlot, List<Component>> onHover;
    @Nullable
    private IClickable onClick;
    private boolean renderHover;
    private boolean renderAboveSlots;

    private List<Component> lastInfo = Collections.emptyList();
    @Nullable
    private Tooltip lastTooltip;

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

    public GuiSlot hover(Function<GuiSlot, List<Component>> onHover) {
        this.onHover = onHover;
        return this;
    }

    public GuiSlot click(IClickable onClick) {
        //Use default click sound and default volume from SimpleSoundInstance.forUI
        return click(onClick, 0.25F, BUTTON_CLICK_SOUND);
    }

    public GuiSlot click(IClickable onClick, float clickVolume, @Nullable Supplier<SoundEvent> clickSound) {
        this.clickSound = clickSound;
        this.clickVolume = clickVolume;
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
    public void updateTooltip(int mouseX, int mouseY) {
        ItemStack stack = gui().getCarriedItem();
        List<Component> list = Collections.emptyList();
        if (onHover != null) {
            list = onHover.apply(this);
        }
        if (list.isEmpty() && !stack.isEmpty() && stack.getItem() instanceof ItemConfigurator && gui() instanceof GuiMekanismTile<?, ?> gui &&
            gui.getTileEntity() instanceof ISideConfiguration config && gui.getSlotUnderMouse() instanceof InventoryContainerSlot slot) {
            ConfigInfo info = config.getConfig().getConfig(TransmissionType.ITEM);
            if (info != null) {
                IInventorySlot inventorySlot = slot.getInventorySlot();
                for (DataType type : info.getSupportedDataTypes()) {
                    if (info.getSlotInfo(type) instanceof InventorySlotInfo slotInfo && slotInfo.hasSlot(inventorySlot)) {
                        EnumColor color = type.getColor();
                        list = List.of(MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(color, type, color.getName()));
                        break;
                    }
                }
            }
        }
        if (!list.equals(lastInfo)) {
            lastInfo = list;
            lastTooltip = TooltipUtils.create(list);
        }
        setTooltip(lastTooltip);
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