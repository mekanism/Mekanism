package mekanism.generators.client.recipe_viewer.alias;

import java.util.Optional;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.client.recipe_viewer.alias.IAliasMapping;
import mekanism.client.recipe_viewer.alias.MekanismAliases;
import mekanism.client.recipe_viewer.alias.RVAliasHelper;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.registries.GeneratorsItems;
import mekanism.generators.common.registries.GeneratorsModules;
import net.minecraft.core.Holder.Reference;

public class GeneratorsAliasMapping implements IAliasMapping {

    @Override
    public <ITEM, FLUID, CHEMICAL> void addAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        addChemicalAliases(rv);
        addMultiblockAliases(rv);
        rv.addAliases(GeneratorsBlocks.GAS_BURNING_GENERATOR, GeneratorsAliases.GBG_ETHENE, GeneratorsAliases.GBG_ETHYLENE);
        rv.addModuleAliases(GeneratorsModules.MODULES);
    }

    private <ITEM, FLUID, CHEMICAL> void addChemicalAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        rv.addAliases(GeneratorsFluids.FUSION_FUEL, ChemicalIds.FUSION_FUEL, GeneratorsAliases.FUSION_FUEL);
    }

    private <ITEM, FLUID, CHEMICAL> void addMultiblockAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        rv.addAliases(GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR, MekanismAliases.EVAPORATION_COMPONENT);
        rv.addItemAliases(GeneratorTags.BlockItems.STRUCTURES_FISSION, GeneratorsAliases.FISSION_COMPONENT);
        rv.addItemAliases(GeneratorTags.BlockItems.STRUCTURES_FUSION, GeneratorsAliases.FUSION_COMPONENT);
        Optional<Reference<Chemical>> fusionFuel = rv.registries().get(ChemicalIds.FUSION_FUEL);
        rv.addAliases(fusionFuel.map(chemicalReference -> ContainerType.CHEMICAL.getFilledVariant(GeneratorsItems.HOHLRAUM, chemicalReference, null))
              .orElseGet(GeneratorsItems.HOHLRAUM::asStack), GeneratorsAliases.FUSION_COMPONENT);
        rv.addItemAliases(GeneratorTags.BlockItems.STRUCTURES_TURBINE, GeneratorsAliases.TURBINE_COMPONENT);
    }
}