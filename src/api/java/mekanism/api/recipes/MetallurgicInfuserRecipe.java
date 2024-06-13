package mekanism.api.recipes;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Input: ItemStack
 * <br>
 * Input: Infuse Type
 * <br>
 * Output: ItemStack
 *
 * @apiNote Metallurgic Infusers and Infusing Factories can process this recipe type.
 */
@NothingNullByDefault
public abstract class MetallurgicInfuserRecipe extends ItemStackChemicalToItemStackRecipe<InfuseType, InfusionStack, InfusionStackIngredient> {

    private static final Holder<Item> METALLURGIC_INFUSER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "metallurgic_infuser"));

    @Override
    public abstract ItemStackIngredient getItemInput();

    @Override
    public abstract InfusionStackIngredient getChemicalInput();

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public abstract ItemStack getOutput(ItemStack inputItem, InfusionStack inputChemical);

    @NotNull
    @Override
    public abstract ItemStack getResultItem(@NotNull HolderLookup.Provider provider);

    @Override
    public abstract boolean test(ItemStack itemStack, InfusionStack gasStack);

    @Override
    public abstract List<@NotNull ItemStack> getOutputDefinition();

    @Override
    public final RecipeType<MetallurgicInfuserRecipe> getType() {
        return MekanismRecipeTypes.TYPE_METALLURGIC_INFUSING.value();
    }

    @Override
    public String getGroup() {
        return "metallurgic_infuser";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(METALLURGIC_INFUSER);
    }
}
