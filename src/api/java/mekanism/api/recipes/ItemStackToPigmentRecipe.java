package mekanism.api.recipes;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Contract;

/**
 * Input: ItemStack
 * <br>
 * Output: PigmentStack
 *
 * @apiNote Pigment Extractors can process this recipe type.
 */
@NothingNullByDefault
public abstract class ItemStackToPigmentRecipe extends ItemStackToChemicalRecipe {

    private static final Holder<Item> PIGMENT_EXTRACTOR = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "pigment_extractor"));

    @Override
    public abstract boolean test(ItemStack itemStack);

    @Override
    public abstract ItemStackIngredient getInput();

    @Override
    @Contract(value = "_ -> new", pure = true)
    public abstract ChemicalStack getOutput(ItemStack input);

    @Override
    public abstract List<ChemicalStack> getOutputDefinition();

    @Override
    public final RecipeType<ItemStackToPigmentRecipe> getType() {
        return MekanismRecipeTypes.TYPE_PIGMENT_EXTRACTING.value();
    }

    @Override
    public String getGroup() {
        return "pigment_extractor";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(PIGMENT_EXTRACTOR);
    }
}
