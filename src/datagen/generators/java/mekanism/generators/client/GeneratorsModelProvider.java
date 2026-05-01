package mekanism.generators.client;

import mekanism.client.model.BaseModelProvider;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.registries.GeneratorsItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class GeneratorsModelProvider extends BaseModelProvider {

    public GeneratorsModelProvider(PackOutput output, ResourceManager clientResources) {
        super(output, MekanismGenerators.MODID, clientResources);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        registerFluidBlockStates(blockModels, GeneratorsFluids.FLUIDS);
        registerBuckets(GeneratorsFluids.FLUIDS, itemModels);
        registerModules(GeneratorsItems.ITEMS, itemModels);
        registerGenerated(itemModels, GeneratorsItems.HOHLRAUM, GeneratorsItems.SOLAR_PANEL, GeneratorsItems.TURBINE_BLADE);

        registerManualStates();
    }

    private void registerManualStates() {
        markManualBlockState(GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR);
        markManualBlockState(GeneratorsBlocks.BIO_GENERATOR);
        markManualBlockState(GeneratorsBlocks.CONTROL_ROD_ASSEMBLY);
        markManualBlockState(GeneratorsBlocks.ELECTROMAGNETIC_COIL);
        markManualBlockState(GeneratorsBlocks.FISSION_FUEL_ASSEMBLY);
        markManualBlockState(GeneratorsBlocks.FISSION_REACTOR_CASING);
        markManualBlockState(GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER);
        markManualBlockState(GeneratorsBlocks.FISSION_REACTOR_PORT);
        markManualBlockState(GeneratorsBlocks.FUSION_REACTOR_CONTROLLER);
        markManualBlockState(GeneratorsBlocks.FUSION_REACTOR_FRAME);
        markManualBlockState(GeneratorsBlocks.FUSION_REACTOR_LOGIC_ADAPTER);
        markManualBlockState(GeneratorsBlocks.FUSION_REACTOR_PORT);
        markManualBlockState(GeneratorsBlocks.GAS_BURNING_GENERATOR);
        markManualBlockState(GeneratorsBlocks.HEAT_GENERATOR);
        markManualBlockState(GeneratorsBlocks.LASER_FOCUS_MATRIX);
        markManualBlockState(GeneratorsBlocks.REACTOR_GLASS);
        markManualBlockState(GeneratorsBlocks.ROTATIONAL_COMPLEX);
        markManualBlockState(GeneratorsBlocks.SATURATING_CONDENSER);
        markManualBlockState(GeneratorsBlocks.SOLAR_GENERATOR);
        //markManualBlockState(GeneratorsBlocks.SUPERHEATING_ELEMENT); old file?
        markManualBlockState(GeneratorsBlocks.TURBINE_CASING);
        markManualBlockState(GeneratorsBlocks.TURBINE_ROTOR);
        markManualBlockState(GeneratorsBlocks.TURBINE_VALVE);
        markManualBlockState(GeneratorsBlocks.TURBINE_VENT);
        markManualBlockState(GeneratorsBlocks.WIND_GENERATOR);
    }
}