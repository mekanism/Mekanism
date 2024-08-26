package mekanism.client.recipe_viewer.jei;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.providers.IFluidProvider;
import mekanism.api.text.IHasTranslationKey;
import mekanism.client.recipe_viewer.alias.RVAliasHelper;
import mekanism.common.Mekanism;
import mekanism.common.util.RegistryUtils;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class JEIAliasHelper implements RVAliasHelper<ItemStack, FluidStack, ChemicalStack> {

    private static final Function<ItemStack, String> ITEM_TO_STRING = stack -> stack.getItem().toString();
    private static final Function<FluidStack, String> FLUID_TO_STRING = stack -> RegistryUtils.getName(stack.getFluid()).toString();
    private static final Function<ChemicalStack, String> CHEMICAL_TO_STRING = stack -> stack.getChemical().getRegistryName().toString();

    private final IIngredientAliasRegistration registration;

    public JEIAliasHelper(IIngredientAliasRegistration registration) {
        this.registration = registration;
    }

    @Override
    public ItemStack ingredient(ItemLike itemLike) {
        return new ItemStack(itemLike);
    }

    @Override
    public ItemStack ingredient(ItemStack item) {
        return item;
    }

    @Override
    public FluidStack ingredient(IFluidProvider fluidProvider) {
        return fluidProvider.getFluidStack(FluidType.BUCKET_VOLUME);
    }

    @Override
    public FluidStack ingredient(FluidStack fluid) {
        return fluid;
    }

    @Override
    public ChemicalStack ingredient(IChemicalProvider chemicalProvider) {
        return chemicalProvider.getStack(FluidType.BUCKET_VOLUME);
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