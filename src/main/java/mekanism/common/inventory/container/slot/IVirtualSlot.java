package mekanism.common.inventory.container.slot;

import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public interface IVirtualSlot {

    int getActualX();

    int getActualY();

    void updatePosition(IntSupplier xPositionSupplier, IntSupplier yPositionSupplier);

    void updateRenderInfo(@Nonnull ItemStack stackToRender, boolean shouldDrawOverlay, @Nullable String tooltipOverride);

    @Nonnull
    ItemStack getStackToRender();

    boolean shouldDrawOverlay();

    @Nullable
    String getTooltipOverride();

    Slot getSlot();
}