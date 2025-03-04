package mekanism.client.recipe_viewer.jei;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.text.IHasTranslationKey;
import mekanism.client.recipe_viewer.alias.RVAliasHelper;
import mekanism.common.Mekanism;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class JEIAliasHelper implements RVAliasHelper<ItemStack, FluidStack, ChemicalStack> {

    private static final Function<ItemStack, String> ITEM_TO_STRING = stack -> stack.getItem().toString();
    private static final Function<FluidStack, String> FLUID_TO_STRING = stack -> stack.getFluid().toString();
    private static final Function<ChemicalStack, String> CHEMICAL_TO_STRING = stack -> stack.getChemical().toString();

    private final IIngredientAliasRegistration registration;

    public JEIAliasHelper(IIngredientAliasRegistration registration) {
        this.registration = registration;
    }

    @Override
    public ItemStack ingredient(ItemStack item) {
        return item;
    }

    @Override
    public ItemStack itemIngredient(Holder<Item> item) {
        return new ItemStack(item);
    }

    @Override
    public List<ItemStack> itemTagContents(TagKey<Item> tag) {
        return tagContents(BuiltInRegistries.ITEM, tag, ItemStack::new);
    }

    @Override
    public FluidStack fluidIngredient(Holder<Fluid> fluid) {
        return new FluidStack(fluid, FluidType.BUCKET_VOLUME);
    }

    @Override
    public FluidStack ingredient(FluidStack fluid) {
        return fluid;
    }

    @Override
    public List<FluidStack> fluidTagContents(TagKey<Fluid> tag) {
        return tagContents(BuiltInRegistries.FLUID, tag, holder -> new FluidStack(holder, FluidType.BUCKET_VOLUME));
    }

    @Override
    public ChemicalStack chemicalIngredient(Holder<Chemical> chemical) {
        return new ChemicalStack(chemical, FluidType.BUCKET_VOLUME);
    }

    @Override
    public List<ChemicalStack> chemicalTagContents(TagKey<Chemical> tag) {
        return tagContents(MekanismAPI.CHEMICAL_REGISTRY, tag, holder -> new ChemicalStack(holder, FluidType.BUCKET_VOLUME));
    }

    private <TYPE, STACK> List<STACK> tagContents(Registry<TYPE> registry, TagKey<TYPE> tag, Function<Holder<TYPE>, STACK> stackFunction) {
        return registry.getTag(tag)
              .stream()
              .flatMap(HolderSet::stream)
              .map(stackFunction)
              .toList();
    }

    @Override
    public void addItemAliases(List<ItemStack> stacks, IHasTranslationKey... aliases) {
        addAliases(VanillaTypes.ITEM_STACK, stacks, ITEM_TO_STRING, aliases);
    }

    @Override
    public void addFluidAliases(List<FluidStack> stacks, IHasTranslationKey... aliases) {
        addAliases(NeoForgeTypes.FLUID_STACK, stacks, FLUID_TO_STRING, aliases);
    }

    @Override
    public void addChemicalAliases(List<ChemicalStack> stacks, IHasTranslationKey... aliases) {
        addAliases(MekanismJEI.TYPE_CHEMICAL, stacks, CHEMICAL_TO_STRING, aliases);
    }

    private <INGREDIENT> void addAliases(IIngredientType<INGREDIENT> type, List<INGREDIENT> stacks, Function<INGREDIENT, String> ingredientToString,
          IHasTranslationKey... aliases) {
        if (aliases.length == 0) {
            Mekanism.logger.warn("Expected to have at least one alias for ingredients of type: {}. Ingredients: {}", type.getUid(), stacks.stream()
                  .map(ingredientToString)
                  .collect(Collectors.joining(", "))
            );
        } else {
            List<String> aliasesAsString = Arrays.stream(aliases)
                  .map(IHasTranslationKey::getTranslationKey)
                  .toList();
            registration.addAliases(type, stacks, aliasesAsString);
        }
    }
}