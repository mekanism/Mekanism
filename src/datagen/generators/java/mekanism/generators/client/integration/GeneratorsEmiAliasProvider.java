package mekanism.generators.client.integration;

import dev.emi.emi.api.stack.EmiStack;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.integration.MekanismAliases;
import mekanism.client.integration.emi.BaseEmiAliasProvider;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.ChemicalUtil;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.registries.GeneratorsGases;
import mekanism.generators.common.registries.GeneratorsItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

@NothingNullByDefault
public class GeneratorsEmiAliasProvider extends BaseEmiAliasProvider {

    public GeneratorsEmiAliasProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, MekanismGenerators.MODID);
    }

    @Override
    protected void addAliases(HolderLookup.Provider lookupProvider) {
        addChemicalAliases();
        addMultiblockAliases();
        addAliases(GeneratorsBlocks.GAS_BURNING_GENERATOR, GeneratorsAliases.GBG_ETHENE, GeneratorsAliases.GBG_ETHYLENE);
    }

    private void addChemicalAliases() {
        addAliases(List.of(
              ingredient(GeneratorsFluids.FUSION_FUEL),
              ingredient(GeneratorsGases.FUSION_FUEL)
        ), GeneratorsAliases.FUSION_FUEL);
    }

    private void addMultiblockAliases() {
        addAliases(GeneratorsBlocks.REACTOR_GLASS,
              MekanismAliases.BOILER_COMPONENT,
              MekanismAliases.EVAPORATION_COMPONENT,
              MekanismAliases.MATRIX_COMPONENT,
              MekanismAliases.SPS_COMPONENT,
              MekanismAliases.SPS_FULL_COMPONENT,
              MekanismAliases.TANK_COMPONENT
        );
        addAliases(GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR, MekanismAliases.EVAPORATION_COMPONENT);
        addAlias(GeneratorsAliases.FISSION_COMPONENT,
              GeneratorsBlocks.FISSION_REACTOR_CASING,
              GeneratorsBlocks.FISSION_REACTOR_PORT,
              GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER,
              GeneratorsBlocks.FISSION_FUEL_ASSEMBLY,
              GeneratorsBlocks.CONTROL_ROD_ASSEMBLY,
              GeneratorsBlocks.REACTOR_GLASS
        );
        addAliases(List.of(
              EmiStack.of(GeneratorsBlocks.FUSION_REACTOR_CONTROLLER),
              EmiStack.of(GeneratorsBlocks.FUSION_REACTOR_FRAME),
              EmiStack.of(GeneratorsBlocks.FUSION_REACTOR_PORT),
              EmiStack.of(GeneratorsBlocks.FUSION_REACTOR_LOGIC_ADAPTER),
              EmiStack.of(GeneratorsBlocks.LASER_FOCUS_MATRIX),
              EmiStack.of(GeneratorsBlocks.REACTOR_GLASS),
              EmiStack.of(ChemicalUtil.getFilledVariant(GeneratorsItems.HOHLRAUM, GeneratorsGases.FUSION_FUEL))
        ), GeneratorsAliases.FUSION_COMPONENT);
        addAlias(GeneratorsAliases.TURBINE_COMPONENT,
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