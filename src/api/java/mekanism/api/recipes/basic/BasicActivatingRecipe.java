package mekanism.api.recipes.basic;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

@NothingNullByDefault
public class BasicActivatingRecipe extends BasicChemicalToChemicalRecipe {

    private static final Holder<Item> SOLAR_NEUTRON_ACTIVATOR = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "solar_neutron_activator"));

    public BasicActivatingRecipe(ChemicalStackIngredient input, ChemicalStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_ACTIVATING.value());
    }

    @Override
    public RecipeSerializer<BasicActivatingRecipe> getSerializer() {
        return MekanismRecipeSerializers.ACTIVATING.value();
    }

    @Override
    public String getGroup() {
        return "solar_neutron_activator";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(SOLAR_NEUTRON_ACTIVATOR);
    }
}