package mekanism.common.component.containers.chemical;

import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.component.containers.resource.AttachedResources;
import mekanism.common.component.containers.resource.ResourceContainersBuilder;
import mekanism.common.component.containers.creator.BaseContainerCreator;
import mekanism.common.config.MekanismConfig;
import net.neoforged.neoforge.transfer.access.ItemAccess;

public class ChemicalTanksBuilder extends ResourceContainersBuilder<ChemicalResource, IChemicalTank, ChemicalTanksBuilder> {

    public static ChemicalTanksBuilder builder() {
        return new ChemicalTanksBuilder();
    }

    private ChemicalTanksBuilder() {
    }

    @Override
    public BaseContainerCreator<AttachedResources<ChemicalResource>, IChemicalTank> build() {
        return new BaseContainerBuilder<>(containerCreators, LargeResourceStack.CHEMICAL_HELPER);
    }

    @Override
    protected IntSupplier defaultRate() {
        return MekanismConfig.general.chemicalItemFillRate;
    }

    @Override
    protected IChemicalTank createBasicContainer(ItemAccess attachedAccess, int tankIndex, BiPredicate<ChemicalResource, AutomationType> canExtract,
          BiPredicate<ChemicalResource, AutomationType> canInsert, Predicate<ChemicalResource> validator, IntSupplier rate, LongSupplier capacity) {
        return new ComponentBackedChemicalTank(attachedAccess, tankIndex, canExtract, canInsert, validator, capacity, rate);
    }
}