package mekanism.common.attachments.containers.chemical;

import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.common.attachments.containers.AttachedResources;
import mekanism.common.attachments.containers.ComponentBackedResourceContainer;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ComponentBackedChemicalTank extends ComponentBackedResourceContainer<ChemicalResource> implements IChemicalTank {

    @Nullable
    private final ChemicalAttributeValidator attributeValidator;

    public ComponentBackedChemicalTank(ItemStack attachedTo, int tankIndex, BiPredicate<ChemicalResource, AutomationType> canExtract,
          BiPredicate<ChemicalResource, AutomationType> canInsert, Predicate<ChemicalResource> validator, IntSupplier rate, LongSupplier capacity) {
        this(attachedTo, tankIndex, canExtract, canInsert, validator, rate, capacity, null);
    }

    public ComponentBackedChemicalTank(ItemStack attachedTo, int tankIndex, BiPredicate<ChemicalResource, AutomationType> canExtract,
          BiPredicate<ChemicalResource, AutomationType> canInsert, Predicate<ChemicalResource> validator, IntSupplier rate, LongSupplier capacity,
          @Nullable ChemicalAttributeValidator attributeValidator) {
        super(attachedTo, tankIndex, canExtract, canInsert, validator, rate, capacity);
        this.attributeValidator = attributeValidator;
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
        TransferPreconditions.checkNonEmpty(chemicalType);
        return getAttributeValidator().process(chemicalType) && super.isValid(chemicalType);
    }
}