package mekanism.common.tile.machine;

import javax.annotation.Nonnull;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityElectricMachine;

public class TileEntityEnrichmentChamber extends TileEntityElectricMachine {

    public TileEntityEnrichmentChamber() {
        super(MekanismBlocks.ENRICHMENT_CHAMBER, 200);
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ItemStackToItemStackRecipe> getRecipeType() {
        return MekanismRecipeType.ENRICHING;
    }
}