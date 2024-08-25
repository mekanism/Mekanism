package mekanism.client.recipe_viewer.alias;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.providers.IFluidProvider;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.content.gear.IModuleItem;
import mekanism.common.registration.impl.ItemDeferredRegister;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;

public interface RVAliasHelper<ITEM, FLUID, CHEMICAL> {

    ITEM ingredient(ItemLike itemLike);

    ITEM ingredient(ItemStack item);

    FLUID ingredient(IFluidProvider fluidProvider);

    FLUID ingredient(FluidStack fluid);

    CHEMICAL ingredient(IChemicalProvider chemicalProvider);

    default void addAlias(IHasTranslationKey alias, ItemLike... items) {
        if (items.length == 0) {
            throw new IllegalArgumentException("Expected to have at least one item");
        }
        addItemAliases(Arrays.stream(items).map(this::ingredient).toList(), alias);
    }

    default void addAliases(IFluidProvider fluidProvider, IChemicalProvider chemicalProvider, IHasTranslationKey... aliases) {
        addAliases(fluidProvider, aliases);
        addAliases(chemicalProvider, aliases);
    }

    default void addAliases(ItemLike item, IHasTranslationKey... aliases) {
        addAliases(new ItemStack(item), aliases);
    }

    default void addAliases(ItemStack stack, IHasTranslationKey... aliases) {
        addItemAliases(List.of(ingredient(stack)), aliases);
    }

    default void addAliases(Collection<? extends ItemLike> stacks, IHasTranslationKey... aliases) {
        addItemAliases(stacks.stream().map(this::ingredient).toList(), aliases);
    }

    default void addItemAliases(Collection<ItemStack> stacks, IHasTranslationKey... aliases) {
        addItemAliases(stacks.stream().map(this::ingredient).toList(), aliases);
    }

    default void addAliases(IFluidProvider fluidProvider, IHasTranslationKey... aliases) {
        addFluidAliases(List.of(ingredient(fluidProvider)), aliases);
    }

    default void addAliases(FluidStack stack, IHasTranslationKey... aliases) {
        addFluidAliases(List.of(ingredient(stack)), aliases);
    }

    default void addAliases(IChemicalProvider chemicalProvider, IHasTranslationKey... aliases) {
        addChemicalAliases(List.of(ingredient(chemicalProvider)), aliases);
    }

    default void addItemAlias(ITEM item, IHasTranslationKey... aliases) {
        addItemAliases(List.of(item), aliases);
    }

    void addItemAliases(List<ITEM> stacks, IHasTranslationKey... aliases);

    default void addFluidAlias(FLUID fluid, IHasTranslationKey... aliases) {
        addFluidAliases(List.of(fluid), aliases);
    }

    void addFluidAliases(List<FLUID> stacks, IHasTranslationKey... aliases);

    default void addChemicalAlias(CHEMICAL chemical, IHasTranslationKey... aliases) {
        addChemicalAliases(List.of(chemical), aliases);
    }

    void addChemicalAliases(List<CHEMICAL> stacks, IHasTranslationKey... aliases);

    default void addModuleAliases(ItemDeferredRegister items) {
        for (DeferredHolder<Item, ? extends Item> entry : items.getEntries()) {
            if (entry.get() instanceof IModuleItem module) {
                addAliases(entry.get(), IModuleHelper.INSTANCE.getSupported(module.getModuleData())
                      .stream()
                      .map(item -> (IHasTranslationKey) item::getDescriptionId)
                      .toArray(IHasTranslationKey[]::new)
                );
            }
        }
    }
}