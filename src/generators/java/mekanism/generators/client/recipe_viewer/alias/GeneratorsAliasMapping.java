package mekanism.generators.client.recipe_viewer.alias;

import java.util.List;
import mekanism.client.recipe_viewer.alias.IAliasMapping;
import mekanism.client.recipe_viewer.alias.MekanismAliases;
import mekanism.client.recipe_viewer.alias.RVAliasHelper;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.ChemicalUtil;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsChemicals;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.registries.GeneratorsItems;

public class GeneratorsAliasMapping implements IAliasMapping {

    @Override
    public <ITEM, FLUID, CHEMICAL> void addAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        addChemicalAliases(rv);
        addMultiblockAliases(rv);
        rv.addAliases(GeneratorsBlocks.GAS_BURNING_GENERATOR, GeneratorsAliases.GBG_ETHENE, GeneratorsAliases.GBG_ETHYLENE);
        rv.addModuleAliases(GeneratorsItems.ITEMS);
    }

    private <ITEM, FLUID, CHEMICAL> void addChemicalAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        rv.addAliases(GeneratorsFluids.FUSION_FUEL, GeneratorsChemicals.FUSION_FUEL, GeneratorsAliases.FUSION_FUEL);
    }

    private <ITEM, FLUID, CHEMICAL> void addMultiblockAliases(RVAliasHelper<ITEM, FLUID, CHEMICAL> rv) {
        rv.addAliases(GeneratorsBlocks.REACTOR_GLASS,
              MekanismAliases.BOILER_COMPONENT,
              MekanismAliases.EVAPORATION_COMPONENT,
              MekanismAliases.MATRIX_COMPONENT,
              MekanismAliases.SPS_COMPONENT,
              MekanismAliases.SPS_FULL_COMPONENT,
              MekanismAliases.TANK_COMPONENT
        );
        rv.addAliases(GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR, MekanismAliases.EVAPORATION_COMPONENT);
        rv.addAlias(GeneratorsAliases.FISSION_COMPONENT,
              GeneratorsBlocks.FISSION_REACTOR_CASING,
              GeneratorsBlocks.FISSION_REACTOR_PORT,
              GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER,
              GeneratorsBlocks.FISSION_FUEL_ASSEMBLY,
              GeneratorsBlocks.CONTROL_ROD_ASSEMBLY,
              GeneratorsBlocks.REACTOR_GLASS
        );
        rv.addItemAliases(List.of(
              GeneratorsBlocks.FUSION_REACTOR_CONTROLLER.getItemStack(),
              GeneratorsBlocks.FUSION_REACTOR_FRAME.getItemStack(),
              GeneratorsBlocks.FUSION_REACTOR_PORT.getItemStack(),
              GeneratorsBlocks.FUSION_REACTOR_LOGIC_ADAPTER.getItemStack(),
              GeneratorsBlocks.LASER_FOCUS_MATRIX.getItemStack(),
              GeneratorsBlocks.REACTOR_GLASS.getItemStack(),
              ChemicalUtil.getFilledVariant(GeneratorsItems.HOHLRAUM, GeneratorsChemicals.FUSION_FUEL)
        ), GeneratorsAliases.FUSION_COMPONENT);
        rv.addAlias(GeneratorsAliases.TURBINE_COMPONENT,
              GeneratorsBlocks.TURBINE_CASING,
              GeneratorsBlocks.TURBINE_VENT,
              GeneratorsBlocks.TURBINE_VALVE,
              GeneratorsBlocks.TURBINE_ROTOR,
              GeneratorsItems.TURBINE_BLADE,
              GeneratorsBlocks.SATURATING_CONDENSER,
              GeneratorsBlocks.ELECTROMAGNETIC_COIL,
              GeneratorsBlocks.ROTATIONAL_COMPLEX,
              GeneratorsBlocks.REACTOR_GLASS,
              MekanismBlocks.PRESSURE_DISPERSER,
              MekanismBlocks.STRUCTURAL_GLASS
        );
    }
}