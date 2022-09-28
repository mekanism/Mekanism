package mekanism.common.registration.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.providers.IItemProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.CreativeTabMekanism;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.item.ItemModule;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class ItemDeferredRegister extends WrappedDeferredRegister<Item> {

    private static final CreativeTabMekanism tabMekanism = new CreativeTabMekanism();

    private final List<IItemProvider> allItems = new ArrayList<>();

    public ItemDeferredRegister(String modid) {
        super(modid, ForgeRegistries.ITEMS);
    }

    public static Item.Properties getMekBaseProperties() {
        return new Item.Properties().tab(tabMekanism);
    }

    public ItemRegistryObject<Item> register(String name) {
        return register(name, Item::new);
    }

    public ItemRegistryObject<Item> registerUnburnable(String name) {
        return registerUnburnable(name, Item::new);
    }

    public ItemRegistryObject<Item> register(String name, Rarity rarity) {
        return register(name, properties -> new Item(properties.rarity(rarity)));
    }

    public ItemRegistryObject<Item> register(String name, EnumColor color) {
        return register(name, properties -> new Item(properties) {
            @NotNull
            @Override
            public Component getName(@NotNull ItemStack stack) {
                return TextComponentUtil.build(color, super.getName(stack));
            }
        });
    }

    public ItemRegistryObject<ItemModule> registerModule(ModuleRegistryObject<?> moduleDataSupplier) {
        //Note: We use the internal helper just in case we end up needing to know it is an ItemModule instead of just an Item somewhere
        return register("module_" + moduleDataSupplier.getInternalRegistryName(),
              () -> ModuleHelper.INSTANCE.createModuleItem(moduleDataSupplier, getMekBaseProperties()));
    }

    public <ITEM extends Item> ItemRegistryObject<ITEM> register(String name, Function<Item.Properties, ITEM> sup) {
        return register(name, () -> sup.apply(getMekBaseProperties()));
    }

    public <ITEM extends Item> ItemRegistryObject<ITEM> registerUnburnable(String name, Function<Item.Properties, ITEM> sup) {
        return register(name, () -> sup.apply(getMekBaseProperties().fireResistant()));
    }

    public <ITEM extends Item> ItemRegistryObject<ITEM> register(String name, Supplier<? extends ITEM> sup) {
        ItemRegistryObject<ITEM> registeredItem = register(name, sup, ItemRegistryObject::new);
        allItems.add(registeredItem);
        return registeredItem;
    }

    public <ENTITY extends Mob> ItemRegistryObject<ForgeSpawnEggItem> registerSpawnEgg(EntityTypeRegistryObject<ENTITY> entityTypeProvider,
          int primaryColor, int secondaryColor) {
        return register(entityTypeProvider.getInternalRegistryName() + "_spawn_egg", props -> new ForgeSpawnEggItem(entityTypeProvider, primaryColor,
              secondaryColor, props));
    }

    public List<IItemProvider> getAllItems() {
        return Collections.unmodifiableList(allItems);
    }
}