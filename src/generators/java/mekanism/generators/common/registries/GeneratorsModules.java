package mekanism.generators.common.registries;

import mekanism.common.registration.impl.ModuleDeferredRegister;
import mekanism.common.registration.impl.ModuleRegistryObject;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.gear.mekasuit.ModuleGeothermalGeneratorUnit;
import net.minecraft.item.Rarity;

//Note: We need to declare our item providers like we do so that they don't end up being null due to us referencing these objects from the items
@SuppressWarnings({"Convert2MethodRef", "FunctionalExpressionCanBeFolded"})
public class GeneratorsModules {

    private GeneratorsModules() {
    }

    public static final ModuleDeferredRegister MODULES = new ModuleDeferredRegister(MekanismGenerators.MODID);

    //Pants
    public static final ModuleRegistryObject<ModuleGeothermalGeneratorUnit> GEOTHERMAL_GENERATOR_UNIT = MODULES.register("geothermal_generator_unit",
          ModuleGeothermalGeneratorUnit::new, () -> GeneratorsItems.MODULE_GEOTHERMAL_GENERATOR.getItem(), builder -> builder.maxStackSize(8).rarity(Rarity.RARE));
}