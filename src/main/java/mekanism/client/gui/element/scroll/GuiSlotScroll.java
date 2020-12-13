package mekanism.client.gui.element.scroll;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiRelativeElement;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.ISlotClickHandler;
import mekanism.common.inventory.ISlotClickHandler.IScrollableSlot;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiSlotScroll extends GuiRelativeElement {

    private static final ResourceLocation SLOTS = MekanismUtils.getResource(ResourceType.GUI_SLOT, "slots.png");
    private static final ResourceLocation SLOTS_DARK = MekanismUtils.getResource(ResourceType.GUI_SLOT, "slots_dark.png");

    private final GuiScrollBar scrollBar;

    private final int xSlots, ySlots;
    private final Supplier<List<IScrollableSlot>> slotList;
    private final ISlotClickHandler clickHandler;

    public GuiSlotScroll(IGuiWrapper gui, int x, int y, int xSlots, int ySlots, Supplier<List<IScrollableSlot>> slotList, ISlotClickHandler clickHandler) {
        super(gui, x, y, xSlots * 18 + 18, ySlots * 18);
        this.xSlots = xSlots;
        this.ySlots = ySlots;
        this.slotList = slotList;
        this.clickHandler = clickHandler;
        addChild(scrollBar = new GuiScrollBar(gui, relativeX + xSlots * 18 + 4, y, ySlots * 18,
              () -> getSlotList() == null ? 0 : (int) Math.ceil((double) getSlotList().size() / xSlots), () -> ySlots));
    }

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        minecraft.textureManager.bindTexture(getSlotList() == null ? SLOTS_DARK : SLOTS);
        blit(matrix, x, y, 0, 0, xSlots * 18, ySlots * 18, 288, 288);

        List<IScrollableSlot> list = getSlotList();
        if (list != null) {
            int slotStart = scrollBar.getCurrentSelection() * xSlots, max = xSlots * ySlots;
            for (int i = 0; i < max; i++) {
                int slot = slotStart + i;
                // terminate if we've exceeded max slot pos
                if (slot >= list.size()) {
                    break;
                }
                int slotX = x + (i % xSlots) * 18, slotY = y + (i / xSlots) * 18;
                renderSlot(matrix, list.get(slot), slotX, slotY);
            }
        }
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        int xAxis = mouseX - getGuiLeft(), yAxis = mouseY - getGuiTop();
        int slotX = (xAxis - relativeX) / 18, slotY = (yAxis - relativeY) / 18;
        if (slotX >= 0 && slotY >= 0 && slotX < xSlots && slotY < ySlots) {
            int slotStartX = relativeX + slotX * 18 + 1, slotStartY = relativeY + slotY * 18 + 1;
            if (xAxis >= slotStartX && xAxis < slotStartX + 16 && yAxis >= slotStartY && yAxis < slotStartY + 16) {
                fill(matrix, slotStartX, slotStartY, slotStartX + 16, slotStartY + 16, GuiSlot.DEFAULT_HOVER_COLOR);
                MekanismRenderer.resetColor();
            }
        }
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrix, int xAxis, int yAxis) {
        IScrollableSlot slot = getSlot(xAxis, yAxis, relativeX, relativeY);
        if (slot != null) {
            renderSlotTooltip(matrix, slot, xAxis, yAxis);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return scrollBar.adjustScroll(delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        IScrollableSlot slot = getSlot(mouseX, mouseY, x, y);
        clickHandler.onClick(slot, button, Screen.hasShiftDown(), minecraft.player.inventory.getItemStack());
        return true;
    }

    private IScrollableSlot getSlot(double mouseX, double mouseY, int relativeX, int relativeY) {
        List<IScrollableSlot> list = getSlotList();
        if (list == null) {
            return null;
        }
        int slotX = (int) ((mouseX - relativeX) / 18), slotY = (int) ((mouseY - relativeY) / 18);
        // terminate if we clicked the border of a slot
        int slotStartX = relativeX + slotX * 18 + 1, slotStartY = relativeY + slotY * 18 + 1;
        if (mouseX < slotStartX || mouseX >= slotStartX + 16 || mouseY < slotStartY || mouseY >= slotStartY + 16) {
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

    private void renderSlot(MatrixStack matrix, IScrollableSlot slot, int slotX, int slotY) {
        // sanity checks
        if (isSlotEmpty(slot)) {
            return;
        }
        gui().renderItemWithOverlay(matrix, slot.getItem().getStack(), slotX + 1, slotY + 1, 1.0F, "");
        if (slot.getCount() > 1) {
            renderSlotText(matrix, getCountText(slot.getCount()), slotX + 1, slotY + 1);
        }
    }

    private void renderSlotTooltip(MatrixStack matrix, IScrollableSlot slot, int slotX, int slotY) {
        // sanity checks
        if (isSlotEmpty(slot)) {
            return;
        }
        ItemStack stack = slot.getItem().getStack();
        long count = slot.getCount();
        if (count < 10_000) {
            gui().renderItemTooltip(matrix, stack, slotX, slotY);
        } else {
            //If the slot's displayed count is truncated, make sure we also add the the actual amount to the tooltip
            gui().renderItemTooltipWithExtra(matrix, stack, slotX, slotY, Collections.singletonList(MekanismLang.QIO_STORED_COUNT.translateColored(EnumColor.GRAY,
                  EnumColor.INDIGO, TextUtils.format(count))));
        }
    }

    private boolean isSlotEmpty(IScrollableSlot slot) {
        return slot.getItem() == null || slot.getItem().getStack().isEmpty();
    }

    private void renderSlotText(MatrixStack matrix, String text, int x, int y) {
        matrix.push();
        MekanismRenderer.resetColor();
        float scale = 0.6F;
        int width = getFont().getStringWidth(text);
        //If we need a lower scale due to having a lot of text, calculate it
        scale = Math.min(1, 16F / (width * scale)) * scale;
        float yAdd = 4 - (scale * 8) / 2F;
        matrix.translate(x + 16 - width * scale, y + 9 + yAdd, 200F);
        matrix.scale(scale, scale, scale);

        getFont().drawStringWithShadow(matrix, text, 0, 0, 0xFFFFFF);
        matrix.pop();
    }

    private String getCountText(long count) {
        if (count <= 1) {
            return null;
        } else if (count < 10_000) {
            return Long.toString(count);
        } else if (count < 10_000_000) {
            return Double.toString(Math.round(count / 1_000D)) + "K";
        } else if (count < 10_000_000_000L) {
            return Double.toString(Math.round(count / 1_000_000D)) + "M";
        } else if (count < 10_000_000_000_000L) {
            return Double.toString(Math.round(count / 1_000_000_000D)) + "B";
        }
        return ">10T";
    }

    private List<IScrollableSlot> getSlotList() {
        return slotList.get();
    }
}
