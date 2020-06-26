package mekanism.client.gui.element.scroll;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiRelativeElement;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.ISlotClickHandler;
import mekanism.common.inventory.ISlotClickHandler.IScrollableSlot;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
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
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getSlotList() == null ? SLOTS_DARK : SLOTS);
        blit(field_230690_l_, field_230691_m_, 0, 0, xSlots * 18, ySlots * 18, 288, 288);

        List<IScrollableSlot> list = getSlotList();
        if (list == null) {
            return;
        }
        int slotStart = scrollBar.getCurrentSelection() * xSlots, max = xSlots * ySlots;
        for (int i = 0; i < max; i++) {
            int slot = slotStart + i;
            // terminate if we've exceeded max slot pos
            if (slot >= list.size()) {
                break;
            }
            int slotX = field_230690_l_ + (i % xSlots) * 18, slotY = field_230691_m_ + (i / xSlots) * 18;
            renderSlot(list.get(slot), slotX, slotY);
        }
    }

    @Override
    public void renderForeground(int mouseX, int mouseY) {
        super.renderForeground(mouseX, mouseY);
        int xAxis = mouseX - guiObj.getLeft(), yAxis = mouseY - guiObj.getTop();
        int slotX = (xAxis - relativeX) / 18, slotY = (yAxis - relativeY) / 18;
        if (slotX >= 0 && slotY >= 0 && slotX < xSlots && slotY < ySlots) {
            int slotStartX = relativeX + slotX * 18 + 1, slotStartY = relativeY + slotY * 18 + 1;
            if (xAxis >= slotStartX && xAxis < slotStartX + 16 && yAxis >= slotStartY && yAxis < slotStartY + 16) {
                fill(slotStartX, slotStartY, slotStartX + 16, slotStartY + 16, GuiSlot.DEFAULT_HOVER_COLOR);
                MekanismRenderer.resetColor();
            }
        }
    }

    @Override
    public void renderToolTip(int xAxis, int yAxis) {
        IScrollableSlot slot = getSlot(xAxis, yAxis, relativeX, relativeY);
        if (slot == null) {
            return;
        }
        renderSlotTooltip(slot, xAxis, yAxis);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return scrollBar.adjustScroll(delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        IScrollableSlot slot = getSlot(mouseX, mouseY, field_230690_l_, field_230691_m_);
        clickHandler.onClick(slot, button, Screen.func_231173_s_(), minecraft.player.inventory.getItemStack());
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

    private void renderSlot(IScrollableSlot slot, int slotX, int slotY) {
        // sanity checks
        if (slot.getItem() == null || slot.getItem().getStack() == null || slot.getItem().getStack().isEmpty()) {
            return;
        }
        guiObj.renderItemWithOverlay(slot.getItem().getStack(), slotX + 1, slotY + 1, 1.0F, "");
        if (slot.getCount() > 1) {
            renderSlotText(getCountText(slot.getCount()), slotX + 1, slotY + 1);
        }
    }

    private void renderSlotTooltip(IScrollableSlot slot, int slotX, int slotY) {
        // sanity checks
        if (slot.getItem() == null || slot.getItem().getStack() == null || slot.getItem().getStack().isEmpty()) {
            return;
        }
        guiObj.renderItemTooltip(slot.getItem().getStack(), slotX, slotY);
    }

    private void renderSlotText(String text, int x, int y) {
        RenderSystem.pushMatrix();
        MekanismRenderer.resetColor();
        float scale = 0.6F;
        MatrixStack matrix = new MatrixStack();
        float yAdd = 4 - (scale * 8) / 2F;
        matrix.translate(x + 16 - getFont().getStringWidth(text) * scale, y + 9 + yAdd, 200F);
        matrix.scale(scale, scale, scale);

        IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
        getFont().renderString(text, 0, 0, 0xFFFFFF, true, matrix.getLast().getMatrix(), buffer, false, 0, 15728880);
        buffer.finish();

        RenderSystem.popMatrix();
    }

    private String getCountText(long count) {
        if (count <= 1) {
            return null;
        }
        if (count < 10_000) {
            return Long.toString(count);
        }
        if (count < 10_000_000) {
            return Double.toString(Math.round(count / 1_000D)) + "K";
        }
        if (count < 10_000_000_000L) {
            return Double.toString(Math.round(count / 1_000_000D)) + "M";
        }
        if (count < 10_000_000_000_000L) {
            return Double.toString(Math.round(count / 1_000_000_000D)) + "B";
        }
        return ">10T";
    }

    private List<IScrollableSlot> getSlotList() {
        return slotList.get();
    }
}
