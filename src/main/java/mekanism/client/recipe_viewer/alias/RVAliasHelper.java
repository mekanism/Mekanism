package mekanism.client.recipe_viewer.alias;

import java.util.Collection;
import java.util.List;
import mekanism.api.chemical.Chemical;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.Mekanism;
import mekanism.common.content.gear.IModuleItem;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ItemDeferredRegister;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public interface RVAliasHelper<ITEM, FLUID, CHEMICAL> {

    ITEM ingredient(ItemStack item);

    ITEM itemIngredient(Holder<Item> item);

    List<ITEM> itemTagContents(TagKey<Item> tag);

    FLUID fluidIngredient(Holder<Fluid> fluid);

    FLUID ingredient(FluidStack fluid);

    List<FLUID> fluidTagContents(TagKey<Fluid> tag);

    CHEMICAL chemicalIngredient(Holder<Chemical> chemical);

    List<CHEMICAL> chemicalTagContents(TagKey<Chemical> tag);

    default void addAliases(Holder<Fluid> fluidProvider, Holder<Chemical> chemicalProvider, IHasTranslationKey... aliases) {
        addFluidAliases(fluidProvider, aliases);
        addChemicalAliases(chemicalProvider, aliases);
    }

    default void addAliases(BlockRegistryObject<?, ?> block, IHasTranslationKey... aliases) {
        addItemAliases(block.getItemHolder(), aliases);
    }

    default void addAliases(ItemStack stack, IHasTranslationKey... aliases) {
        addItemAliases(List.of(ingredient(stack)), aliases);
    }

    default void addAliases(Collection<? extends ItemLike> stacks, IHasTranslationKey... aliases) {
        addItemAliases(stacks.stream().map(ItemStack::new).toList(), aliases);
    }

    default void addItemHolderAliases(Collection<? extends Holder<Item>> items, IHasTranslationKey... aliases) {
        addItemAliases(items.stream().map(ItemStack::new).toList(), aliases);
    }

    default void addItemAliases(Holder<Item> item, IHasTranslationKey... aliases) {
        addItemAliases(List.of(itemIngredient(item)), aliases);
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

    default void addFluidAliases(Holder<Fluid> fluid, IHasTranslationKey... aliases) {
        addFluidAlias(fluidIngredient(fluid), aliases);
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

    default void addChemicalAliases(Holder<Chemical> chemical, IHasTranslationKey... aliases) {
        addChemicalAliases(List.of(chemicalIngredient(chemical)), aliases);
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
        for (Holder<Item> entry : items.getEntries()) {
            if (entry.value() instanceof IModuleItem module) {
                addItemAliases(entry, IModuleHelper.INSTANCE.getSupportedItems(module.getModuleData())
                      .stream()
                      .map(item -> (IHasTranslationKey) item::getDescriptionId)
                      .toArray(IHasTranslationKey[]::new)
                );
            }
        }
    }
}