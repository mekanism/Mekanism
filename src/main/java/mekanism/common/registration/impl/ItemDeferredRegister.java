package mekanism.common.registration.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.providers.IItemProvider;
import mekanism.common.Mekanism;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemDeferredRegister extends WrappedDeferredRegister<Item> {

    private final List<IItemProvider> allItems = new ArrayList<>();

    public ItemDeferredRegister(String modid) {
        super(modid, ForgeRegistries.ITEMS);
    }

    public static Item.Properties getMekBaseProperties() {
        return new Item.Properties().group(Mekanism.tabMekanism);
    }

    public ItemRegistryObject<Item> register(String name) {
        return register(name, () -> new Item(getMekBaseProperties()));
    }

    public <ITEM extends Item> ItemRegistryObject<ITEM> register(String name, Function<Item.Properties, ITEM> sup) {
        return register(name, () -> sup.apply(getMekBaseProperties()));
    }

    public <ITEM extends Item> ItemRegistryObject<ITEM> register(String name, Supplier<? extends ITEM> sup) {
        ItemRegistryObject<ITEM> registeredItem = register(name, sup, ItemRegistryObject::new);
        allItems.add(registeredItem);
        return registeredItem;
    }

    public List<IItemProvider> getAllItems() {
        return allItems;
    }
}