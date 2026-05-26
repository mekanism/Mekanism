package mekanism.api.recipes;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.SingleInputRecipe.ChemicalInputRecipe;
import mekanism.api.recipes.vanilla_input.SingleChemicalRecipeInput;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Input: Chemical
 * <br>
 * Output: ItemStack
 *
 * @apiNote Chemical Crystallizers can process this recipe type.
 */
@NothingNullByDefault
public abstract class ChemicalCrystallizerRecipe extends ChemicalInputRecipe<ItemStackTemplate> {

    private static final Holder<Item> CHEMICAL_CRYSTALLIZER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_crystallizer"));

    @Override
    public ItemStack assemble(SingleChemicalRecipeInput input) {
        if (!isIncomplete() && test(input.chemical())) {
            return getOutput(input.chemical()).create();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public final RecipeType<ChemicalCrystallizerRecipe> getType() {
        return MekanismRecipeTypes.TYPE_CRYSTALLIZING.value();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(CHEMICAL_CRYSTALLIZER);
    }
}