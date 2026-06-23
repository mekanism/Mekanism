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
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerGhostTarget;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class GuiSlot extends GuiElement implements IRecipeViewerGhostTarget, ISupportsWarning<GuiSlot> {

    private static final int INVALID_SLOT_COLOR = MekanismRenderer.getColorARGB(EnumColor.DARK_RED, 0.8F);
    public static final int DEFAULT_HOVER_COLOR = 0x80FFFFFF;
    private static final Identifier WARNING = Mekanism.rl("slot/warning");
    @Nullable
    private final SlotType slotType;
    @Nullable
    private Supplier<ItemStack> validityCheck;
    @Nullable
    private Supplier<ItemStack> storedStackSupplier;
    @Nullable
    private Supplier<@Nullable SlotOverlay> overlaySupplier;
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

    public GuiSlot(IGuiWrapper gui, int x, int y) {
        super(gui, x, y, SlotType.SLOT_SIZE, SlotType.SLOT_SIZE);
        this.slotType = null;
        active = false;
    }

    public GuiSlot(SlotType type, IGuiWrapper gui, int x, int y) {
        this(type, gui, x, y, SlotType.SLOT_SIZE, SlotType.SLOT_SIZE);
    }

    public GuiSlot(SlotType type, IGuiWrapper gui, int x, int y, int width, int height) {
        super(gui, x, y, width, height);
        this.slotType = type;
        active = false;
    }

    public GuiSlot validity(Supplier<ItemStack> validityCheck) {
        //TODO - 1.18: Evaluate if any of these validity things should be moved to the warning system
        this.validityCheck = validityCheck;
        return this;
    }

    @Override
    public GuiSlot warning(WarningType type, BooleanSupplier warningSupplier) {
        this.warningSupplier = ISupportsWarning.compound(this.warningSupplier, gui().trackWarning(type, warningSupplier));
        return this;
    }

    /// @apiNote For use when there is no validity check and this is a "fake" slot in that the container screen doesn't render the item by default.
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

    public GuiSlot overlayColor(@Nullable IntSupplier colorSupplier) {
        overlayColorSupplier = colorSupplier;
        return this;
    }

    public GuiSlot with(Supplier<@Nullable SlotOverlay> overlaySupplier) {
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
    public void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!renderAboveSlots) {
            draw(guiGraphics);
        }
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (renderAboveSlots) {
            draw(guiGraphics);
        }
    }

    private void draw(GuiGraphicsExtractor guiGraphics) {
        if (slotType != null) {
            Identifier texture;
            if (warningSupplier != null && warningSupplier.getAsBoolean()) {
                texture = WARNING;
            } else {
                texture = slotType.getTexture();
            }
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, relativeX, relativeY, width, height);
        }
        if (overlaySupplier != null) {
            overlay = overlaySupplier.get();
        }
        if (overlay != null) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, overlay.getTexture(), relativeX, relativeY, width, height);
        }
        drawContents(guiGraphics);
    }

    protected void drawContents(GuiGraphicsExtractor guiGraphics) {
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
    public void renderForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        boolean hovered = checkWindows(mouseX, mouseY, isHovered());
        if (renderHover && hovered) {
            int xPos = relativeX + 1;
            int yPos = relativeY + 1;
            guiGraphics.fill(xPos, yPos, xPos + 16, yPos + 16, DEFAULT_HOVER_COLOR);
        }
        if (overlayColorSupplier != null) {
            int xPos = relativeX + 1;
            int yPos = relativeY + 1;
            guiGraphics.fill(xPos, yPos, xPos + 16, yPos + 16, overlayColorSupplier.getAsInt());
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
            gui.getTileEntity() instanceof ISideConfiguration config && gui.getHoveredSlot() instanceof InventoryContainerSlot slot) {
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
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (onClick != null && isValidClickButton(event.buttonInfo())) {
            if (event.x() >= getX() + borderSize() && event.y() >= getY() + borderSize() && event.x() < getRight() - borderSize() && event.y() < getBottom() - borderSize()) {
                if (onClick.onClick(this, event, isDoubleClick)) {
                    playDownSound(minecraft.getSoundManager());
                    return true;
                }
                //If clicking the slot fails check super as maybe it has children that can handle clicks
            }
        }
        return super.mouseClicked(event, isDoubleClick);
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