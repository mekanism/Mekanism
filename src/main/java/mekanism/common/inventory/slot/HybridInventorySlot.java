package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class HybridInventorySlot extends BasicInventorySlot implements IFluidHandlerSlot {

    public static HybridInventorySlot inputOrDrain(MergedTank mergedTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(mergedTank, "Merged tank cannot be null");
        Predicate<ItemResource> fluidInsertPredicate = FluidInventorySlot.getInputPredicate(mergedTank.getFluidTank());
        Predicate<ItemResource> chemicalInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(mergedTank.getChemicalTank());
        BiPredicate<ItemResource, AutomationType> insertPredicate = (itemType, _) -> switch (mergedTank.getCurrentType()) {
            case FLUID -> fluidInsertPredicate.test(itemType);
            case CHEMICAL -> chemicalInsertPredicate.test(itemType);
            //Tank is empty, check if any insert predicate is valid
            case EMPTY -> fluidInsertPredicate.test(itemType) || chemicalInsertPredicate.test(itemType);
        };
        //Extract predicate, always allow the player to manually extract or if the insert predicate no longer matches allow for it to be extracted
        return new HybridInventorySlot(mergedTank, (itemType, automationType) -> automationType == AutomationType.MANUAL || !insertPredicate.test(itemType, automationType),
              insertPredicate, listener, x, y);
    }

    public static HybridInventorySlot outputOrFill(MergedTank mergedTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(mergedTank, "Merged tank cannot be null");
        Predicate<ItemResource> chemicalExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(mergedTank.getChemicalTank());
        Predicate<ItemResource> chemicalInsertPredicate = itemType -> ChemicalInventorySlot.fillInsertCheck(mergedTank.getChemicalTank(), itemType);

        return new HybridInventorySlot(mergedTank, (itemType, automationType) -> {
            if (automationType == AutomationType.MANUAL) {
                //Always allow the player to manually extract
                return true;
            }
            return switch (mergedTank.getCurrentType()) {
                //Always allow extracting from a "fluid output" slot
                case FLUID -> true;
                case CHEMICAL -> chemicalExtractPredicate.test(itemType);
                //Tank is empty, check all our extraction predicates
                case EMPTY -> chemicalExtractPredicate.test(itemType);
            };
        }, (itemType, automationType) -> switch (mergedTank.getCurrentType()) {
            //Only allow inserting internally for "fluid output" slots
            case FLUID -> automationType == AutomationType.INTERNAL;
            case CHEMICAL -> chemicalInsertPredicate.test(itemType);
            case EMPTY -> {
                //Tank is empty, if the item is a fluid handler, and it is an internal check allow it
                if (automationType == AutomationType.INTERNAL && Capabilities.FLUID.hasCapability(itemType)) {
                    yield true;
                }
                //otherwise, only allow it if one of the chemical insert predicates matches
                yield chemicalInsertPredicate.test(itemType);
            }
        }, listener, x, y);
    }

    // used by IFluidHandlerSlot
    private boolean isDraining;
    private boolean isFilling;
    private final MergedTank mergedTank;

    private HybridInventorySlot(MergedTank mergedTank, BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert,
          @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, ConstantPredicates.alwaysTrue(), listener, x, y);
        this.mergedTank = mergedTank;
    }

    @Override
    public IFluidTank getFluidTank() {
        return mergedTank.getFluidTank();
    }

    @Override
    public boolean isDraining() {
        return isDraining;
    }

    @Override
    public boolean isFilling() {
        return isFilling;
    }

    @Override
    public void setDraining(boolean draining) {
        isDraining = draining;
    }

    @Override
    public void setFilling(boolean filling) {
        isFilling = filling;
    }

    @Override
    public void serialize(ValueOutput output) {
        super.serialize(output);
        if (isDraining) {
            output.putBoolean(SerializationConstants.DRAINING, true);
        }
        if (isFilling) {
            output.putBoolean(SerializationConstants.FILLING, true);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        //Grab the booleans regardless if they are present as if they aren't that means they are false
        isDraining = input.getBooleanOr(SerializationConstants.DRAINING, isDraining);
        isFilling = input.getBooleanOr(SerializationConstants.FILLING, isFilling);
        super.deserialize(input);
    }

    public void drainChemicalTank() {
        //todo - 26.1: itemaccess
        //ChemicalInventorySlot.drainChemicalTank(this, mergedTank.getChemicalTank(), Capabilities.CHEMICAL.getCapability(current));
    }

    public void fillChemicalTank() {
        //todo - 26.1: itemaccess
        //ChemicalInventorySlot.fillChemicalTank(this, mergedTank.getChemicalTank(), Capabilities.CHEMICAL.getCapability(current));
    }

}
