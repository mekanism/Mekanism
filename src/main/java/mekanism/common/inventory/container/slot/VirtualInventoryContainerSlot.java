package mekanism.common.inventory.container.slot;

import java.util.function.Consumer;
import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.IGUIWindow;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class VirtualInventoryContainerSlot extends InventoryContainerSlot implements IVirtualSlot {

    private final SelectedWindowData windowData;
    private IntSupplier xPositionSupplier = () -> x;
    private IntSupplier yPositionSupplier = () -> y;
    private ItemStack stackToRender = ItemStack.EMPTY;
    @Nullable
    private String tooltipOverride;
    private boolean shouldDrawOverlay;
    @Nullable
    private IGUIWindow window;

    public VirtualInventoryContainerSlot(BasicInventorySlot slot, SelectedWindowData windowData, @Nullable SlotOverlay slotOverlay, Consumer<ItemStack> uncheckedSetter) {
        super(slot, 0, 0, ContainerSlotType.IGNORED, slotOverlay, null, uncheckedSetter);
        this.windowData = windowData;
    }

    @Override
    public IGUIWindow getLinkedWindow() {
        return window;
    }

    @Override
    public int getActualX() {
        return xPositionSupplier.getAsInt();
    }

    @Override
    public int getActualY() {
        return yPositionSupplier.getAsInt();
    }

    @Override
    public void updatePosition(@Nullable IGUIWindow window, IntSupplier xPositionSupplier, IntSupplier yPositionSupplier) {
        this.xPositionSupplier = xPositionSupplier;
        this.yPositionSupplier = yPositionSupplier;
        this.window = window;
    }

    @Override
    public void updateRenderInfo(@Nonnull ItemStack stackToRender, boolean shouldDrawOverlay, @Nullable String tooltipOverride) {
        this.stackToRender = stackToRender;
        this.shouldDrawOverlay = shouldDrawOverlay;
        this.tooltipOverride = tooltipOverride;
    }

    @Nonnull
    @Override
    public ItemStack getStackToRender() {
        return stackToRender;
    }

    @Override
    public boolean shouldDrawOverlay() {
        return shouldDrawOverlay;
    }

    @Nullable
    @Override
    public String getTooltipOverride() {
        return tooltipOverride;
    }

    @Override
    public Slot getSlot() {
        return this;
    }

    @Override
    public boolean exists(@Nullable SelectedWindowData windowData) {
        return this.windowData.equals(windowData);
    }
}