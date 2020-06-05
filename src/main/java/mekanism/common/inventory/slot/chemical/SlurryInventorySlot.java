package mekanism.common.inventory.slot.chemical;

import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SlurryInventorySlot extends ChemicalInventorySlot<Slurry, SlurryStack> {

    @Nullable
    public static ISlurryHandler getCapability(ItemStack stack) {
        return getCapability(stack, Capabilities.SLURRY_HANDLER_CAPABILITY);
    }

    /**
     * Accepts any items that can be filled with the current contents of the slurry tank, or if it is a slurry tank container and the tank is currently empty
     *
     * Drains the tank into this item.
     */
    public static SlurryInventorySlot drain(ISlurryTank slurryTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(slurryTank, "Slurry tank cannot be null");
        Predicate<@NonNull ItemStack> insertPredicate = getDrainInsertPredicate(slurryTank, SlurryInventorySlot::getCapability);
        return new SlurryInventorySlot(slurryTank, insertPredicate.negate(), insertPredicate, stack -> stack.getCapability(Capabilities.SLURRY_HANDLER_CAPABILITY).isPresent(),
              listener, x, y);
    }

    //TODO: Implement creators as needed
    private SlurryInventorySlot(ISlurryTank slurryTank, Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert,
          Predicate<@NonNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        this(slurryTank, () -> null, canExtract, canInsert, validator, listener, x, y);
    }

    private SlurryInventorySlot(ISlurryTank slurryTank, Supplier<World> worldSupplier, Predicate<@NonNull ItemStack> canExtract,
          Predicate<@NonNull ItemStack> canInsert, Predicate<@NonNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        super(slurryTank, worldSupplier, canExtract, canInsert, validator, listener, x, y);
    }

    @Nullable
    @Override
    protected IChemicalHandler<Slurry, SlurryStack> getCapability() {
        return getCapability(current);
    }

    @Nullable
    @Override
    protected Pair<ItemStack, SlurryStack> getConversion() {
        return null;
    }
}