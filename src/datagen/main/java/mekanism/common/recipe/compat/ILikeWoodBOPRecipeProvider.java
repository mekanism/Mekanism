package mekanism.common.recipe.compat;

import biomesoplenty.api.block.BOPBlocks;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.util.IItemProvider;
import yamahari.ilikewood.plugin.biomesoplenty.BiomesOPlentyWoodTypes;
import yamahari.ilikewood.registry.woodtype.IWoodType;

@ParametersAreNonnullByDefault
public class ILikeWoodBOPRecipeProvider extends CompatRecipeProvider {

    public ILikeWoodBOPRecipeProvider() {
        super(yamahari.ilikewood.plugin.util.Constants.MOD_ID, "biomesoplenty");
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addWoodType(consumer, basePath, BOPBlocks.cherry_planks, BOPBlocks.cherry_log, BOPBlocks.cherry_fence, BiomesOPlentyWoodTypes.CHERRY);
        addWoodType(consumer, basePath, BOPBlocks.dead_planks, BOPBlocks.dead_log, BOPBlocks.dead_fence, BiomesOPlentyWoodTypes.DEAD);
        addWoodType(consumer, basePath, BOPBlocks.fir_planks, BOPBlocks.fir_log, BOPBlocks.fir_fence, BiomesOPlentyWoodTypes.FIR);
        addWoodType(consumer, basePath, BOPBlocks.hellbark_planks, BOPBlocks.hellbark_log, BOPBlocks.hellbark_fence, BiomesOPlentyWoodTypes.HELLBARK);
        addWoodType(consumer, basePath, BOPBlocks.jacaranda_planks, BOPBlocks.jacaranda_log, BOPBlocks.jacaranda_fence, BiomesOPlentyWoodTypes.JACARANDA);
        addWoodType(consumer, basePath, BOPBlocks.magic_planks, BOPBlocks.magic_log, BOPBlocks.magic_fence, BiomesOPlentyWoodTypes.MAGIC);
        addWoodType(consumer, basePath, BOPBlocks.mahogany_planks, BOPBlocks.mahogany_log, BOPBlocks.mahogany_fence, BiomesOPlentyWoodTypes.MAHOGANY);
        addWoodType(consumer, basePath, BOPBlocks.palm_planks, BOPBlocks.palm_log, BOPBlocks.palm_fence, BiomesOPlentyWoodTypes.PALM);
        addWoodType(consumer, basePath, BOPBlocks.redwood_planks, BOPBlocks.redwood_log, BOPBlocks.redwood_fence, BiomesOPlentyWoodTypes.REDWOOD);
        addWoodType(consumer, basePath, BOPBlocks.umbran_planks, BOPBlocks.umbran_log, BOPBlocks.umbran_fence, BiomesOPlentyWoodTypes.UMBRAN);
        addWoodType(consumer, basePath, BOPBlocks.willow_planks, BOPBlocks.willow_log, BOPBlocks.willow_fence, BiomesOPlentyWoodTypes.WILLOW);
    }

    private void addWoodType(Consumer<IFinishedRecipe> consumer, String basePath, IItemProvider planks, IItemProvider log, IItemProvider fences, IWoodType woodType) {
        ILikeWoodRecipeProvider.addWoodType(consumer, allModsLoaded, basePath, planks, log, fences, woodType, allModsLoaded);
    }
}