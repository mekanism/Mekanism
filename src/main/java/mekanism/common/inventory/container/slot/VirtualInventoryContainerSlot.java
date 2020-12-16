package mekanism.common.inventory.container.slot;

import java.util.function.Consumer;
import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class VirtualInventoryContainerSlot extends InventoryContainerSlot implements IVirtualSlot {

    private IntSupplier xPositionSupplier = () -> xPos;
    private IntSupplier yPositionSupplier = () -> yPos;
    private ItemStack stackToRender = ItemStack.EMPTY;
    @Nullable
    private String tooltipOverride;
    private boolean shouldDrawOverlay;

    public VirtualInventoryContainerSlot(BasicInventorySlot slot, @Nullable SlotOverlay slotOverlay, Consumer<ItemStack> uncheckedSetter) {
        super(slot, 0, 0, ContainerSlotType.IGNORED, slotOverlay, uncheckedSetter);
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
    public void updatePosition(IntSupplier xPositionSupplier, IntSupplier yPositionSupplier) {
        this.xPositionSupplier = xPositionSupplier;
        this.yPositionSupplier = yPositionSupplier;
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
}