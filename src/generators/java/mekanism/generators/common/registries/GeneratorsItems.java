package mekanism.generators.common.registries;

import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.item.ItemHohlraum;
import mekanism.generators.common.item.ItemTurbineBlade;
import net.minecraft.item.Item;

public class GeneratorsItems {

    private GeneratorsItems() {
    }

    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(MekanismGenerators.MODID);

    public static final ItemRegistryObject<Item> SOLAR_PANEL = ITEMS.register("solar_panel");
    public static final ItemRegistryObject<ItemHohlraum> HOHLRAUM = ITEMS.register("hohlraum", ItemHohlraum::new);
    public static final ItemRegistryObject<ItemTurbineBlade> TURBINE_BLADE = ITEMS.register("turbine_blade", ItemTurbineBlade::new);
}