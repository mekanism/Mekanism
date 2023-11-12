package mekanism.api.recipes;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
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
 * Output: InfusionStack
 *
 * @apiNote Infusion conversion recipes can be used in any slots in Mekanism machines that are able to convert items to infuse types, for example in Metallurgic Infusers
 * and Infusing Factories.
 */
@NothingNullByDefault
public abstract class ItemStackToInfuseTypeRecipe extends ItemStackToChemicalRecipe<InfuseType, InfusionStack> {

    private static final RegistryObject<Item> METALLURGIC_INFUSER = RegistryObject.create(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "metallurgic_infuser"), ForgeRegistries.ITEMS);

    @Override
    public abstract boolean test(ItemStack itemStack);

    @Override
    public abstract ItemStackIngredient getInput();

    @Override
    @Contract(value = "_ -> new", pure = true)
    public abstract InfusionStack getOutput(ItemStack input);

    @Override
    public abstract List<InfusionStack> getOutputDefinition();

    @Override
    public final RecipeType<ItemStackToInfuseTypeRecipe> getType() {
        return MekanismRecipeTypes.TYPE_INFUSION_CONVERSION.get();
    }

    @Override
    public String getGroup() {
        return "infusion_conversion";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(METALLURGIC_INFUSER.get());
    }
}
