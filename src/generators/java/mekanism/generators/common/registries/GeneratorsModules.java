package mekanism.generators.common.registries;

import mekanism.common.registration.impl.ModuleDeferredRegister;
import mekanism.common.registration.impl.ModuleRegistryObject;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.gear.mekasuit.ModuleGeothermalGeneratorUnit;
import mekanism.generators.common.content.gear.mekasuit.ModuleSolarRechargingUnit;

public class GeneratorsModules {

    private GeneratorsModules() {
    }

    public static final ModuleDeferredRegister MODULES = new ModuleDeferredRegister(MekanismGenerators.MODID);

    //Helmet
    public static final ModuleRegistryObject<ModuleSolarRechargingUnit> SOLAR_RECHARGING_UNIT = MODULES.registerInstanced("solar_recharging_unit",
          ModuleSolarRechargingUnit::new, () -> GeneratorsItems.MODULE_SOLAR_RECHARGING, builder -> builder.maxStackSize(8));

    //Pants
    public static final ModuleRegistryObject<ModuleGeothermalGeneratorUnit> GEOTHERMAL_GENERATOR_UNIT = MODULES.registerInstanced("geothermal_generator_unit",
          ModuleGeothermalGeneratorUnit::new, () -> GeneratorsItems.MODULE_GEOTHERMAL_GENERATOR, builder -> builder.maxStackSize(8));
}