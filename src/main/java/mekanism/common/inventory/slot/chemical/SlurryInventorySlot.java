package mekanism.common.inventory.slot.chemical;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class SlurryInventorySlot extends ChemicalInventorySlot<Slurry, SlurryStack> {

    @Nullable
    public static ISlurryHandler getCapability(ItemStack stack) {
        return getCapability(stack, Capabilities.SLURRY_HANDLER);
    }

    /**
     * Accepts any items that can be filled with the current contents of the slurry tank, or if it is a slurry tank container and the tank is currently empty
     *
     * Drains the tank into this item.
     */
    public static SlurryInventorySlot drain(ISlurryTank slurryTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(slurryTank, "Slurry tank cannot be null");
        Predicate<@NotNull ItemStack> insertPredicate = getDrainInsertPredicate(slurryTank, SlurryInventorySlot::getCapability);
        return new SlurryInventorySlot(slurryTank, insertPredicate.negate(), insertPredicate, stack -> stack.getCapability(Capabilities.SLURRY_HANDLER).isPresent(),
              listener, x, y);
    }

    //TODO: Implement creators as needed
    private SlurryInventorySlot(ISlurryTank slurryTank, Predicate<@NotNull ItemStack> canExtract, Predicate<@NotNull ItemStack> canInsert,
          Predicate<@NotNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        this(slurryTank, () -> null, canExtract, canInsert, validator, listener, x, y);
    }

    private SlurryInventorySlot(ISlurryTank slurryTank, Supplier<Level> worldSupplier, Predicate<@NotNull ItemStack> canExtract,
          Predicate<@NotNull ItemStack> canInsert, Predicate<@NotNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        super(slurryTank, worldSupplier, canExtract, canInsert, validator, listener, x, y);
    }

    @Nullable
    @Override
    protected IChemicalHandler<Slurry, SlurryStack> getCapability() {
        return getCapability(current);
    }
}