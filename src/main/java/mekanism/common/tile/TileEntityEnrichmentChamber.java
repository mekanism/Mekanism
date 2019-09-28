package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.RecipeHandler.RecipeWrapper;
import mekanism.common.tile.prefab.TileEntityElectricMachine;

public class TileEntityEnrichmentChamber extends TileEntityElectricMachine {

    public TileEntityEnrichmentChamber() {
        super(MekanismBlock.ENRICHMENT_CHAMBER, 200);
    }

    @Nonnull
    @Override
    public RecipeWrapper<ItemStackToItemStackRecipe> getRecipeWrapper() {
        return RecipeWrapper.ENRICHING;
    }
}