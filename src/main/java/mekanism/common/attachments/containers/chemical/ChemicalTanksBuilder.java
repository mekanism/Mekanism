package mekanism.common.attachments.containers.chemical;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.attachments.containers.AttachedResources;
import mekanism.common.attachments.containers.ResourceContainersBuilder;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;
import mekanism.common.config.MekanismConfig;
import net.minecraft.world.item.ItemStack;

public class ChemicalTanksBuilder extends ResourceContainersBuilder<ChemicalResource, ComponentBackedChemicalTank, ChemicalTanksBuilder> {

    public static ChemicalTanksBuilder builder() {
        return new ChemicalTanksBuilder();
    }

    private ChemicalTanksBuilder() {
    }

    @Override
    public BaseContainerCreator<AttachedResources<ChemicalResource>, ComponentBackedChemicalTank> build() {
        return new BaseChemicalTankBuilder(containerCreators);
    }

    @Override
    protected IntSupplier defaultRate() {
        return MekanismConfig.general.chemicalItemFillRate;
    }

    @Override
    protected ComponentBackedChemicalTank createBasicContainer(ItemStack attachedTo, int tankIndex, BiPredicate<ChemicalResource, AutomationType> canExtract,
          BiPredicate<ChemicalResource, AutomationType> canInsert, Predicate<ChemicalResource> validator, IntSupplier rate, LongSupplier capacity) {
        return new ComponentBackedChemicalTank(attachedTo, tankIndex, canExtract, canInsert, validator, rate, capacity, null);
    }

    private static class BaseChemicalTankBuilder extends BaseContainerCreator<AttachedResources<ChemicalResource>, ComponentBackedChemicalTank> {

        public BaseChemicalTankBuilder(List<IBasicContainerCreator<? extends ComponentBackedChemicalTank>> creators) {
            super(creators);
        }

        @Override
        public AttachedResources<ChemicalResource> initStorage(int containers) {
            return AttachedResources.create(containers, LargeResourceStack.EMPTY_CHEMICAL_STACK);
        }
    }
}