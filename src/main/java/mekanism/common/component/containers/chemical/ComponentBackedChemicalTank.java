package mekanism.common.component.containers.chemical;

import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.ChemicalAttributeValidator;
import mekanism.common.component.containers.resource.ComponentBackedResourceContainer;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.ResourceContainerType;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jspecify.annotations.Nullable;

public class ComponentBackedChemicalTank extends ComponentBackedResourceContainer<ChemicalResource> implements IChemicalTank {

    @Nullable
    private final ChemicalAttributeValidator attributeValidator;

    public ComponentBackedChemicalTank(ItemAccess attachedAccess, int tankIndex, BiPredicate<ChemicalResource, AutomationType> canExtract,
          BiPredicate<ChemicalResource, AutomationType> canInsert, Predicate<ChemicalResource> validator, LongSupplier capacity, IntSupplier rate) {
        this(attachedAccess, tankIndex, canExtract, canInsert, validator, capacity, rate, null);
    }

    public ComponentBackedChemicalTank(ItemAccess attachedAccess, int tankIndex, BiPredicate<ChemicalResource, AutomationType> canExtract,
          BiPredicate<ChemicalResource, AutomationType> canInsert, Predicate<ChemicalResource> validator, LongSupplier capacity, IntSupplier rate,
          @Nullable ChemicalAttributeValidator attributeValidator) {
        super(attachedAccess, tankIndex, canExtract, canInsert, validator, capacity, rate);
        this.attributeValidator = attributeValidator;
    }

    @Override
    protected ResourceContainerType<ChemicalResource, IChemicalTank> containerType() {
        return ContainerType.CHEMICAL;
    }

    @Override
    public ChemicalAttributeValidator getAttributeValidator() {
        return attributeValidator == null ? IChemicalTank.super.getAttributeValidator() : attributeValidator;
    }

    @Override
    public boolean isValid(ChemicalResource chemicalType) {
        TransferPreconditions.checkNonEmpty(chemicalType);
        return getAttributeValidator().process(chemicalType) && super.isValid(chemicalType);
    }
}