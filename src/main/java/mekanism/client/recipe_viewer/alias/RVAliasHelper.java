package mekanism.client.recipe_viewer.alias;

import java.util.Collection;
import java.util.List;
import mekanism.api.chemical.Chemical;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.providers.IFluidProvider;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.Mekanism;
import mekanism.common.content.gear.IModuleItem;
import mekanism.common.registration.impl.ItemDeferredRegister;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;

public interface RVAliasHelper<ITEM, FLUID, CHEMICAL> {

    ITEM ingredient(ItemLike itemLike);

    ITEM ingredient(ItemStack item);

    List<ITEM> itemTagContents(TagKey<Item> tag);

    FLUID ingredient(IFluidProvider fluidProvider);

    FLUID ingredient(FluidStack fluid);

    List<FLUID> fluidTagContents(TagKey<Fluid> tag);

    CHEMICAL ingredient(IChemicalProvider chemicalProvider);

    List<CHEMICAL> chemicalTagContents(TagKey<Chemical> tag);

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

    default void addItemAliases(TagKey<Item> tag, IHasTranslationKey... aliases) {
        if (aliases.length == 0) {
            Mekanism.logger.warn("Expected to have at least one alias for item tag: {}", tag.location());
        } else {
            addItemAliases(itemTagContents(tag), aliases);
        }
    }

    default void addAliases(IFluidProvider fluidProvider, IHasTranslationKey... aliases) {
        addFluidAliases(List.of(ingredient(fluidProvider)), aliases);
    }

    default void addAliases(FluidStack stack, IHasTranslationKey... aliases) {
        addFluidAliases(List.of(ingredient(stack)), aliases);
    }

    default void addFluidAliases(TagKey<Fluid> tag, IHasTranslationKey... aliases) {
        if (aliases.length == 0) {
            Mekanism.logger.warn("Expected to have at least one alias for fluid tag: {}", tag.location());
        } else {
            addFluidAliases(fluidTagContents(tag), aliases);
        }
    }

    default void addAliases(IChemicalProvider chemicalProvider, IHasTranslationKey... aliases) {
        addChemicalAliases(List.of(ingredient(chemicalProvider)), aliases);
    }

    default void addChemicalAliases(TagKey<Chemical> tag, IHasTranslationKey... aliases) {
        if (aliases.length == 0) {
            Mekanism.logger.warn("Expected to have at least one alias for chemical tag: {}", tag.location());
        } else {
            addChemicalAliases(chemicalTagContents(tag), aliases);
        }
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