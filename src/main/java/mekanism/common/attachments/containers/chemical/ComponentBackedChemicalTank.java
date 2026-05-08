package mekanism.common.attachments.containers.chemical;

import com.google.common.primitives.Ints;
import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.common.attachments.containers.ComponentBackedResourceContainer;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ComponentBackedChemicalTank extends ComponentBackedResourceContainer<ChemicalResource, ChemicalStack, AttachedChemicals> implements IChemicalTank {

    @Nullable
    private final ChemicalAttributeValidator attributeValidator;
    private final LongSupplier capacity;
    private final LongSupplier rate;

    public ComponentBackedChemicalTank(ItemStack attachedTo, int tankIndex, BiPredicate<ChemicalResource, AutomationType> canExtract,
          BiPredicate<ChemicalResource, AutomationType> canInsert, Predicate<ChemicalResource> validator, LongSupplier rate, LongSupplier capacity,
          @Nullable ChemicalAttributeValidator attributeValidator) {
        super(attachedTo, tankIndex, capacity.getAsLong(), canExtract, canInsert, validator);
        this.capacity = capacity;
        //TODO - 26.1: Make rate be an int supplier?
        this.rate = rate;
        this.attributeValidator = attributeValidator;
    }

    @Override
    protected ChemicalStack copy(ChemicalStack toCopy) {
        return toCopy.copy();
    }

    @Override
    protected boolean isEmpty(ChemicalStack value) {
        return value.isEmpty();
    }

    @Override
    protected ContainerType<?, AttachedChemicals, ?> containerType() {
        return ContainerType.CHEMICAL;
    }

    /**
     * @apiNote Try to minimize the number of calls to this method so that we don't have to look up the data component multiple times.
     */
    @Override
    public ChemicalStack getStack() {
        return getContents(getAttached());
    }

    @Override
    public void setStack(ChemicalStack stack) {
        setStackUnchecked(stack);
    }

    @Override
    public void setStackUnchecked(ChemicalStack stack) {
        setContents(getAttached(), stack);
    }

    @Override
    public ChemicalAttributeValidator getAttributeValidator() {
        return attributeValidator == null ? IChemicalTank.super.getAttributeValidator() : attributeValidator;
    }

    @Override
    protected ChemicalResource asResource(ChemicalStack stack) {
        return ChemicalResource.of(stack);
    }

    @Override
    protected long getAmountAsLong(ChemicalStack stack) {
        return stack.amount();
    }

    @Override
    protected void setContents(AttachedChemicals attachedChemicals, ChemicalResource type, long storedAmount) {
        setContents(attachedChemicals, type.toStack(storedAmount));
    }

    @Override
    public boolean isValid(ChemicalResource chemicalType) {
        return getAttributeValidator().process(chemicalType) && super.isValid(chemicalType);
    }

    @Override
    public long getLimitAsLong(ChemicalResource resource) {
        return capacity.getAsLong();
    }

    @Override
    protected int getInsertionRate(@Nullable AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? super.getInsertionRate(automationType) : Ints.saturatedCast(rate.getAsLong());
    }

    @Override
    protected int getExtractionRate(@Nullable AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? super.getExtractionRate(automationType) : Ints.saturatedCast(rate.getAsLong());
    }

    @Override
    public void serialize(ValueOutput output) {
        ChemicalStack stored = getStack();
        if (!stored.isEmpty()) {
            output.store(SerializationConstants.STORED, ChemicalStack.CODEC, stored);
        }
    }
}