package mekanism.common.inventory.container.slot;

import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.IGUIWindow;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public interface IVirtualSlot {

    @Nullable
    IGUIWindow getLinkedWindow();

    int getActualX();

    int getActualY();

    void updatePosition(@Nullable IGUIWindow window, IntSupplier xPositionSupplier, IntSupplier yPositionSupplier);

    void updateRenderInfo(@Nonnull ItemStack stackToRender, boolean shouldDrawOverlay, @Nullable String tooltipOverride);

    @Nonnull
    ItemStack getStackToRender();

    boolean shouldDrawOverlay();

    @Nullable
    String getTooltipOverride();

    Slot getSlot();
}