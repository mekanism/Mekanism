package mekanism.generators.common.registries;

import mekanism.common.component.containers.chemical.ChemicalTanksBuilder;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.item.ItemHohlraum;
import mekanism.generators.common.item.ItemTurbineBlade;
import net.minecraft.world.item.Item;

public class GeneratorsItems {

    private GeneratorsItems() {
    }

    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(MekanismGenerators.MODID);

    public static final ItemRegistryObject<Item> SOLAR_PANEL = ITEMS.register("solar_panel");
    public static final ItemRegistryObject<ItemHohlraum> HOHLRAUM = ITEMS.registerItem("hohlraum", ItemHohlraum::new)
          .addAttachedContainerCapabilities(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                .addInternalStorage(MekanismGeneratorsConfig.generators.hohlraumFillRate, MekanismGeneratorsConfig.generators.hohlraumMaxGas,
                      gas -> gas.is(GeneratorTags.Chemicals.FUSION_FUEL)
                ).build(), MekanismGeneratorsConfig.generators);
    public static final ItemRegistryObject<ItemTurbineBlade> TURBINE_BLADE = ITEMS.registerItem("turbine_blade", ItemTurbineBlade::new);
}