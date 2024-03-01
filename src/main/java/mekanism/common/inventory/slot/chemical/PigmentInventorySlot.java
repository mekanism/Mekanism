package mekanism.common.inventory.slot.chemical;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.MultiTypeCapability;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class PigmentInventorySlot extends ChemicalInventorySlot<Pigment, PigmentStack> {

    /**
     * Fills the tank from this item
     */
    public static PigmentInventorySlot fill(IPigmentTank pigmentTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(pigmentTank, "Pigment tank cannot be null");
        return new PigmentInventorySlot(pigmentTank, getFillExtractPredicate(pigmentTank, Capabilities.PIGMENT),
              stack -> fillInsertCheck(pigmentTank, Capabilities.PIGMENT, stack), listener, x, y);
    }

    /**
     * Accepts any items that can be filled with the current contents of the pigment tank, or if it is a pigment tank container and the tank is currently empty
     * <p>
     * Drains the tank into this item.
     */
    public static PigmentInventorySlot drain(IPigmentTank pigmentTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(pigmentTank, "Pigment tank cannot be null");
        Predicate<@NotNull ItemStack> insertPredicate = getDrainInsertPredicate(pigmentTank, Capabilities.PIGMENT);
        return new PigmentInventorySlot(pigmentTank, insertPredicate.negate(), insertPredicate, listener, x, y);
    }

    private PigmentInventorySlot(IPigmentTank pigmentTank, Predicate<@NotNull ItemStack> canExtract, Predicate<@NotNull ItemStack> canInsert,
          @Nullable IContentsListener listener, int x, int y) {
        this(pigmentTank, () -> null, canExtract, canInsert, listener, x, y);
    }

    private PigmentInventorySlot(IPigmentTank pigmentTank, Supplier<Level> worldSupplier, Predicate<@NotNull ItemStack> canExtract,
          Predicate<@NotNull ItemStack> canInsert, @Nullable IContentsListener listener, int x, int y) {
        super(pigmentTank, worldSupplier, canExtract, canInsert, listener, x, y);
    }

    @Override
    protected MultiTypeCapability<IPigmentHandler> getChemicalCapability() {
        return Capabilities.PIGMENT;
    }
}