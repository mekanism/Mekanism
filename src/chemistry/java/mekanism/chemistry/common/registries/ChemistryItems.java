package mekanism.chemistry.common.registries;

import mekanism.chemistry.common.MekanismChemistry;
import mekanism.chemistry.common.item.ItemFertilizer;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;

public class ChemistryItems {

    private ChemistryItems() {
    }

    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(MekanismChemistry.MODID);

    public static final ItemRegistryObject<Item> AMMONIUM = ITEMS.register("ammonium");
    public static final ItemRegistryObject<ItemFertilizer> FERTILIZER = ITEMS.register("fertilizer", ItemFertilizer::new);
}
