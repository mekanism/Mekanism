package mekanism.common.recipe.compat;

import biomesoplenty.api.block.BOPBlocks;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
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
        addWoodType(consumer, basePath, BOPBlocks.DEAD_PLANKS, BOPBlocks.DEAD_LOG, BOPBlocks.DEAD_FENCE, BiomesOPlentyWoodTypes.DEAD);
        addWoodType(consumer, basePath, BOPBlocks.FIR_PLANKS, BOPBlocks.FIR_LOG, BOPBlocks.FIR_FENCE, BiomesOPlentyWoodTypes.FIR);
        addWoodType(consumer, basePath, BOPBlocks.HELLBARK_PLANKS, BOPBlocks.HELLBARK_LOG, BOPBlocks.HELLBARK_FENCE, BiomesOPlentyWoodTypes.HELLBARK);
        addWoodType(consumer, basePath, BOPBlocks.JACARANDA_PLANKS, BOPBlocks.JACARANDA_LOG, BOPBlocks.JACARANDA_FENCE, BiomesOPlentyWoodTypes.JACARANDA);
        addWoodType(consumer, basePath, BOPBlocks.MAGIC_PLANKS, BOPBlocks.MAGIC_LOG, BOPBlocks.MAGIC_FENCE, BiomesOPlentyWoodTypes.MAGIC);
        addWoodType(consumer, basePath, BOPBlocks.MAHOGANY_PLANKS, BOPBlocks.MAHOGANY_LOG, BOPBlocks.MAHOGANY_FENCE, BiomesOPlentyWoodTypes.MAHOGANY);
        addWoodType(consumer, basePath, BOPBlocks.PALM_PLANKS, BOPBlocks.PALM_LOG, BOPBlocks.PALM_FENCE, BiomesOPlentyWoodTypes.PALM);
        addWoodType(consumer, basePath, BOPBlocks.REDWOOD_PLANKS, BOPBlocks.REDWOOD_LOG, BOPBlocks.REDWOOD_FENCE, BiomesOPlentyWoodTypes.REDWOOD);
        addWoodType(consumer, basePath, BOPBlocks.UMBRAN_PLANKS, BOPBlocks.UMBRAN_LOG, BOPBlocks.UMBRAN_FENCE, BiomesOPlentyWoodTypes.UMBRAN);
        addWoodType(consumer, basePath, BOPBlocks.WILLOW_PLANKS, BOPBlocks.WILLOW_LOG, BOPBlocks.WILLOW_FENCE, BiomesOPlentyWoodTypes.WILLOW);
    }

    private void addWoodType(RecipeOutput consumer, String basePath, Block planks, Block log, Block fences, IWoodType woodType) {
        ILikeWoodRecipeProvider.addWoodType(consumer, allModsLoaded, basePath, planks, log, fences, woodType);
    }
}