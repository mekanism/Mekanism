package mekanism.common.recipe.compat;

import static mekanism.common.recipe.compat.BiomesOPlentyRecipeProvider.getBOPBlock;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.block.Block;
import yamahari.ilikewood.plugin.biomesoplenty.BiomesOPlentyWoodTypes;
import yamahari.ilikewood.registry.woodtype.IWoodType;

@ParametersAreNotNullByDefault
public class ILikeWoodBOPRecipeProvider extends CompatRecipeProvider {

    public ILikeWoodBOPRecipeProvider(String modid) {
        super(modid, "biomesoplenty");
    }

    @Override
    protected void registerRecipes(RecipeOutput consumer, String basePath) {
        addWoodType(consumer, basePath, getBOPBlock("DEAD_PLANKS"), getBOPBlock("DEAD_LOG"), getBOPBlock("DEAD_FENCE"), BiomesOPlentyWoodTypes.DEAD);
        addWoodType(consumer, basePath, getBOPBlock("FIR_PLANKS"), getBOPBlock("FIR_LOG"), getBOPBlock("FIR_FENCE"), BiomesOPlentyWoodTypes.FIR);
        addWoodType(consumer, basePath, getBOPBlock("HELLBARK_PLANKS"), getBOPBlock("HELLBARK_LOG"), getBOPBlock("HELLBARK_FENCE"), BiomesOPlentyWoodTypes.HELLBARK);
        addWoodType(consumer, basePath, getBOPBlock("JACARANDA_PLANKS"), getBOPBlock("JACARANDA_LOG"), getBOPBlock("JACARANDA_FENCE"), BiomesOPlentyWoodTypes.JACARANDA);
        addWoodType(consumer, basePath, getBOPBlock("MAGIC_PLANKS"), getBOPBlock("MAGIC_LOG"), getBOPBlock("MAGIC_FENCE"), BiomesOPlentyWoodTypes.MAGIC);
        addWoodType(consumer, basePath, getBOPBlock("MAHOGANY_PLANKS"), getBOPBlock("MAHOGANY_LOG"), getBOPBlock("MAHOGANY_FENCE"), BiomesOPlentyWoodTypes.MAHOGANY);
        addWoodType(consumer, basePath, getBOPBlock("PALM_PLANKS"), getBOPBlock("PALM_LOG"), getBOPBlock("PALM_FENCE"), BiomesOPlentyWoodTypes.PALM);
        addWoodType(consumer, basePath, getBOPBlock("REDWOOD_PLANKS"), getBOPBlock("REDWOOD_LOG"), getBOPBlock("REDWOOD_FENCE"), BiomesOPlentyWoodTypes.REDWOOD);
        addWoodType(consumer, basePath, getBOPBlock("UMBRAN_PLANKS"), getBOPBlock("UMBRAN_LOG"), getBOPBlock("UMBRAN_FENCE"), BiomesOPlentyWoodTypes.UMBRAN);
        addWoodType(consumer, basePath, getBOPBlock("WILLOW_PLANKS"), getBOPBlock("WILLOW_LOG"), getBOPBlock("WILLOW_FENCE"), BiomesOPlentyWoodTypes.WILLOW);
    }

    private void addWoodType(RecipeOutput consumer, String basePath, Holder<Block> planks, Holder<Block> log, Holder<Block> fences,
          IWoodType woodType) {
        ILikeWoodRecipeProvider.addWoodType(consumer, allModsLoaded, basePath, planks.value(), log.value(), fences.value(), woodType);
    }
}