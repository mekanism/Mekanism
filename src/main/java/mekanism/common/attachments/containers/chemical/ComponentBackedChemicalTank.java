package mekanism.common.attachments.containers.chemical;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.common.attachments.containers.ComponentBackedResourceContainer;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.AttachedResources;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ComponentBackedChemicalTank extends ComponentBackedResourceContainer<ChemicalResource> implements IChemicalTank {

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
    protected ChemicalResource getEmptyResource() {
        return ChemicalResource.EMPTY;
    }

    @Override
    protected Codec<ChemicalResource> getResourceCodec() {
        return ChemicalResource.CODEC;
    }

    @Override
    protected ContainerType<?, AttachedResources<ChemicalResource>, ?> containerType() {
        return ContainerType.CHEMICAL;
    }

    @Override
    public ChemicalAttributeValidator getAttributeValidator() {
        return attributeValidator == null ? IChemicalTank.super.getAttributeValidator() : attributeValidator;
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
}