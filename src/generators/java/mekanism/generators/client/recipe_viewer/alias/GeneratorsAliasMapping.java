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
import net.minecraft.world.item.ItemStack;

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
        rv.addAliases(List.of(
              GeneratorsBlocks.FISSION_REACTOR_CASING,
              GeneratorsBlocks.FISSION_REACTOR_PORT,
              GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER,
              GeneratorsBlocks.FISSION_FUEL_ASSEMBLY,
              GeneratorsBlocks.CONTROL_ROD_ASSEMBLY,
              GeneratorsBlocks.REACTOR_GLASS
        ), GeneratorsAliases.FISSION_COMPONENT);
        rv.addItemAliases(List.of(
              new ItemStack(GeneratorsBlocks.FUSION_REACTOR_CONTROLLER),
              new ItemStack(GeneratorsBlocks.FUSION_REACTOR_FRAME),
              new ItemStack(GeneratorsBlocks.FUSION_REACTOR_PORT),
              new ItemStack(GeneratorsBlocks.FUSION_REACTOR_LOGIC_ADAPTER),
              new ItemStack(GeneratorsBlocks.LASER_FOCUS_MATRIX),
              new ItemStack(GeneratorsBlocks.REACTOR_GLASS),
              ChemicalUtil.getFilledVariant(GeneratorsItems.HOHLRAUM, GeneratorsChemicals.FUSION_FUEL)
        ), GeneratorsAliases.FUSION_COMPONENT);
        rv.addAliases(List.of(
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
        ), GeneratorsAliases.TURBINE_COMPONENT);
    }
}