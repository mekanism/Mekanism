package mekanism.api.recipes;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.display.CombiningRecipeDisplay;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

public abstract class ChemicalDissolutionRecipe extends ItemStackChemicalToObjectRecipe<ChemicalStackTemplate> {

    private static final Holder<Item> CHEMICAL_DISSOLUTION_CHAMBER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_dissolution_chamber"));

    @Override
    public final RecipeType<ChemicalDissolutionRecipe> getType() {
        return MekanismRecipeTypes.TYPE_DISSOLUTION.value();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new CombiningRecipeDisplay(
              getItemInput().display(),
              getChemicalInput().display(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(CHEMICAL_DISSOLUTION_CHAMBER)
        ));
    }
}
