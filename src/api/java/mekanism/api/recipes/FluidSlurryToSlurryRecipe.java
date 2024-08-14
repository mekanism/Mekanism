package mekanism.api.recipes;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Input: FluidStack
 * <br>
 * Input: Chemical
 * <br>
 * Output: ChemicalStack
 *
 * @apiNote Chemical Washers can process this recipe type.
 */
@NothingNullByDefault
public abstract class FluidSlurryToSlurryRecipe extends FluidChemicalToChemicalRecipe {

    private static final Holder<Item> CHEMICAL_WASHER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_washer"));

    @Override
    public final RecipeType<FluidSlurryToSlurryRecipe> getType() {
        return MekanismRecipeTypes.TYPE_WASHING.value();
    }

    @Override
    public String getGroup() {
        return "chemical_washer";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(CHEMICAL_WASHER);
    }
}
