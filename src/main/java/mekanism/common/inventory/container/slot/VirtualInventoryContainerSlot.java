package mekanism.common.inventory.container.slot;

import java.util.function.IntSupplier;
import mekanism.common.inventory.container.IGUIWindow;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class VirtualInventoryContainerSlot extends InventoryContainerSlot implements IVirtualSlot {

    private final SelectedWindowData windowData;
    private IntSupplier xPositionSupplier = () -> x;
    private IntSupplier yPositionSupplier = () -> y;
    private ItemStack stackToRender = ItemStack.EMPTY;
    @Nullable
    private String tooltipOverride;
    private boolean isHovering;
    private boolean quickCraftStack;
    @Nullable
    private IGUIWindow window;

    public VirtualInventoryContainerSlot(BasicInventorySlot slot, SelectedWindowData windowData, @Nullable SlotOverlay slotOverlay) {
        super(slot, 0, 0, ContainerSlotType.IGNORED, slotOverlay, null);
        this.windowData = windowData;
    }

    @Nullable
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
    public void updateRenderInfo(ItemStack stackToRender, boolean isHovering, boolean quickCraftStack, @Nullable String tooltipOverride) {
        this.stackToRender = stackToRender;
        this.isHovering = isHovering;
        this.quickCraftStack = quickCraftStack;
        this.tooltipOverride = tooltipOverride;
    }

    @Override
    public ItemStack getStackToRender() {
        return stackToRender;
    }

    @Override
    public boolean quickCraftStack() {
        return quickCraftStack;
    }

    @Override
    public boolean isHovering() {
        return isHovering;
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