package mekanism.client.gui.element.scroll;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerIngredientHelper;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.ISlotClickHandler;
import mekanism.common.inventory.ISlotClickHandler.IScrollableSlot;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class GuiSlotScroll extends GuiElement implements IRecipeViewerIngredientHelper {

    private static final Component ZERO = TextComponentUtil.build(ChatFormatting.YELLOW, 0);
    private static final int SLOT_SIZE = SlotType.SLOT_SIZE;
    private static final int INNER_SIZE = SLOT_SIZE - 2;

    private final GuiScrollBar scrollBar;

    private final int xSlots, ySlots;
    private final Supplier<List<IScrollableSlot>> slotList;
    private final ISlotClickHandler clickHandler;

    public GuiSlotScroll(IGuiWrapper gui, int x, int y, int xSlots, int ySlots, Supplier<List<IScrollableSlot>> slotList, ISlotClickHandler clickHandler) {
        super(gui, x, y, SLOT_SIZE * xSlots + SLOT_SIZE, SLOT_SIZE * ySlots);
        this.xSlots = xSlots;
        this.ySlots = ySlots;
        this.slotList = slotList;
        this.clickHandler = clickHandler;
        scrollBar = addChild(new GuiScrollBar(gui, relativeX + SLOT_SIZE * xSlots + 4, y, SLOT_SIZE * ySlots,
              () -> Mth.ceil((double) getSlotList().size() / this.xSlots), () -> this.ySlots));
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        List<IScrollableSlot> list = getSlotList();
        SlotType slotType = list.isEmpty() ? SlotType.DARK : SlotType.NORMAL;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, slotType.getTexture(), relativeX, relativeY, SLOT_SIZE * xSlots, SLOT_SIZE * ySlots);
        if (!list.isEmpty()) {
            int slotStart = scrollBar.getCurrentSelection() * xSlots, max = xSlots * ySlots;
            for (int i = 0; i < max; i++) {
                int slot = slotStart + i;
                // terminate if we've exceeded max slot pos
                if (slot >= list.size()) {
                    break;
                }
                renderSlot(guiGraphics, list.get(slot), SLOT_SIZE * (i % xSlots), SLOT_SIZE * (i / xSlots));
            }
        }
    }

    @Override
    public void renderForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        int xAxis = mouseX - getGuiLeft(), yAxis = mouseY - getGuiTop();
        int slotX = (xAxis - relativeX) / SLOT_SIZE, slotY = (yAxis - relativeY) / SLOT_SIZE;
        if (slotX >= 0 && slotY >= 0 && slotX < xSlots && slotY < ySlots) {
            int slotStartX = relativeX + slotX * SLOT_SIZE + 1, slotStartY = relativeY + slotY * SLOT_SIZE + 1;
            if (xAxis >= slotStartX && xAxis < slotStartX + INNER_SIZE && yAxis >= slotStartY && yAxis < slotStartY + INNER_SIZE && checkWindows(mouseX, mouseY)) {
                guiGraphics.fill(slotStartX, slotStartY, slotStartX + INNER_SIZE, slotStartY + INNER_SIZE, GuiSlot.DEFAULT_HOVER_COLOR);
            }
        }
    }

    @Override
    public void renderToolTip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.renderToolTip(guiGraphics, mouseX, mouseY);
        IScrollableSlot slot = getSlot(mouseX, mouseY);
        if (slot != null) {
            renderSlotTooltip(guiGraphics, slot, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
        return scrollBar.adjustScroll(yDelta) || super.mouseScrolled(mouseX, mouseY, xDelta, yDelta);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (gui().currentlyQuickCrafting()) {
            //If the player is currently quick crafting don't do any special handling for as if they clicked in the screen
            return super.mouseReleased(event);
        }
        super.mouseReleased(event);
        //TODO - 26.2: Evaluate if we just want to pass the mouse button event directly?
        clickHandler.onClick(() -> getSlot(event.x(), event.y()), event.button(), event.hasShiftDown(), gui().getCarriedItem());
        return true;
    }

    @Nullable
    private IScrollableSlot getSlot(double mouseX, double mouseY) {
        List<IScrollableSlot> list = getSlotList();
        if (list.isEmpty()) {
            return null;
        }
        int slotX = (int) ((mouseX - getX()) / SLOT_SIZE), slotY = (int) ((mouseY - getY()) / SLOT_SIZE);
        // terminate if we clicked the border of a slot
        int slotStartX = getX() + slotX * SLOT_SIZE + 1, slotStartY = getY() + slotY * SLOT_SIZE + 1;
        if (mouseX < slotStartX || mouseX >= slotStartX + INNER_SIZE || mouseY < slotStartY || mouseY >= slotStartY + INNER_SIZE) {
            return null;
        }
        // terminate if we aren't looking at a slot on-screen
        if (slotX < 0 || slotY < 0 || slotX >= xSlots || slotY >= ySlots) {
            return null;
        }
        int slot = (slotY + scrollBar.getCurrentSelection()) * xSlots + slotX;
        // terminate if the slot doesn't exist
        if (slot >= list.size()) {
            return null;
        }
        return list.get(slot);
    }

    private void renderSlot(GuiGraphicsExtractor guiGraphics, IScrollableSlot slot, int slotX, int slotY) {
        ItemStack stack = slot.itemType().toStack();
        if (stack.isEmpty()) {//Sanity check
            return;
        }
        gui().renderItemWithOverlay(guiGraphics, stack, relativeX + slotX + 1, relativeY + slotY + 1, 1, "");
        long count = slot.count();
        Component text = null;
        if (count == 0) {
            //If there is no items stored, display the text in yellow, similar to what mojang does when it has to display a zero count
            // See: AbstractContainerScreen#render(GuiGraphicsExtractor, int, int, float) and rendering the dragging item
            text = ZERO;
        } else if (count > 1) {
            //Note: For cases like 9,999,999 we intentionally display as 9999.9K instead of 10M so that people
            // do not think they have more stored than they actually have just because it is rounding up
            if (count < 10_000) {
                text = TextComponentUtil.getString(Long.toString(count));
            } else {
                text = UnitDisplayUtils.getDisplay(count, 1);
            }
        }
        if (text != null) {
            renderSlotText(guiGraphics, text, slotX + 1, slotY + 1);
        }
    }

    private void renderSlotTooltip(GuiGraphicsExtractor guiGraphics, IScrollableSlot slot, int slotX, int slotY) {
        ItemStack stack = slot.itemType().toStack();
        if (stack.isEmpty()) {//Sanity check
            return;
        }
        long count = slot.count();
        if (count < 10_000) {
            guiGraphics.setTooltipForNextFrame(font(), stack, slotX, slotY);
        } else {
            //If the slot's displayed count is truncated, make sure we also add the actual amount to the tooltip
            gui().renderItemTooltipWithExtra(guiGraphics, stack, slotX, slotY, Collections.singletonList(MekanismLang.QIO_STORED_COUNT.translateColored(EnumColor.GRAY,
                  EnumColor.INDIGO, TextUtils.format(count))));
        }
    }

    private void renderSlotText(GuiGraphicsExtractor guiGraphics, Component text, int x, int y) {
        float scale = 0.6F;
        float scaledWidth = font().width(text) * scale;
        if (scaledWidth >= INNER_SIZE) {
            //If we need a lower scale slightly due to having a lot of text, calculate it
            //Note: If it would still overflow, then we just let the scrolling text handle it
            scale = 0.5F;
        }
        //TODO - 26.2: gui zindex
        //PoseStack pose = guiGraphics.pose();
        //pose.pushPose();
        //pose.translate(0, 0, 200);
        drawScaledScrollingString(guiGraphics, text, x, y + 9, TextAlignment.RIGHT, CommonColors.WHITE, INNER_SIZE, 0, true, scale);
        //pose.popPose();
    }

    private List<IScrollableSlot> getSlotList() {
        return slotList.get();
    }

    @Override
    public Optional<?> getIngredient(double mouseX, double mouseY) {
        IScrollableSlot slot = getSlot(mouseX, mouseY);
        return slot == null ? Optional.empty() : Optional.of(slot.itemType().toStack());
    }

    @Override
    public Rect2i getIngredientBounds(double mouseX, double mouseY) {
        List<IScrollableSlot> list = getSlotList();
        if (!list.isEmpty()) {
            int slotX = (int) ((mouseX - getX()) / SLOT_SIZE), slotY = (int) ((mouseY - getY()) / SLOT_SIZE);
            int slotStartX = getX() + slotX * SLOT_SIZE + 1, slotStartY = getY() + slotY * SLOT_SIZE + 1;
            if (mouseX >= slotStartX && mouseX < slotStartX + INNER_SIZE && mouseY >= slotStartY && mouseY < slotStartY + INNER_SIZE) {
                return new Rect2i(slotStartX + 1, slotStartY + 1, INNER_SIZE, INNER_SIZE);
            }
        }
        //Note: This should never be the case as we validated we had an ingredient but if it is just return the entire gui portion
        return new Rect2i(getX(), getY(), width, height);
    }
}