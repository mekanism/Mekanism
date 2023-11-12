package mekanism.api.recipes;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;
import org.jetbrains.annotations.Contract;

/**
 * Input: ItemStack
 * <br>
 * Output: PigmentStack
 *
 * @apiNote Pigment Extractors can process this recipe type.
 */
@NothingNullByDefault
public abstract class ItemStackToPigmentRecipe extends ItemStackToChemicalRecipe<Pigment, PigmentStack> {

    private static final RegistryObject<Item> PIGMENT_EXTRACTOR = RegistryObject.create(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "pigment_extractor"), ForgeRegistries.ITEMS);

    @Override
    public abstract boolean test(ItemStack itemStack);

    @Override
    public abstract ItemStackIngredient getInput();

    @Override
    @Contract(value = "_ -> new", pure = true)
    public abstract PigmentStack getOutput(ItemStack input);

    @Override
    public abstract List<PigmentStack> getOutputDefinition();

    @Override
    public final RecipeType<ItemStackToPigmentRecipe> getType() {
        return MekanismRecipeTypes.TYPE_PIGMENT_EXTRACTING.get();
    }

    @Override
    public String getGroup() {
        return "pigment_extractor";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(PIGMENT_EXTRACTOR.get());
    }
}
