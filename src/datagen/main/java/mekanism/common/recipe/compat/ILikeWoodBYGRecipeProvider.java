package mekanism.common.recipe.compat;

import corgiaoc.byg.core.BYGBlocks;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.util.IItemProvider;
import yamahari.ilikewood.plugin.byg.OhTheBiomesYoullGoWoodTypes;
import yamahari.ilikewood.registry.woodtype.IWoodType;

@ParametersAreNonnullByDefault
public class ILikeWoodBYGRecipeProvider extends CompatRecipeProvider {

    public ILikeWoodBYGRecipeProvider() {
        super(yamahari.ilikewood.plugin.byg.util.Constants.MOD_ID, "byg");
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addWoodType(consumer, basePath, BYGBlocks.ASPEN_PLANKS, BYGBlocks.ASPEN_LOG, BYGBlocks.ASPEN_FENCE, OhTheBiomesYoullGoWoodTypes.ASPEN);
        addWoodType(consumer, basePath, BYGBlocks.BAOBAB_PLANKS, BYGBlocks.BAOBAB_LOG, BYGBlocks.BAOBAB_FENCE, OhTheBiomesYoullGoWoodTypes.BAOBAB);
        addWoodType(consumer, basePath, BYGBlocks.BLUE_ENCHANTED_PLANKS, BYGBlocks.BLUE_ENCHANTED_LOG, BYGBlocks.BLUE_ENCHANTED_FENCE, OhTheBiomesYoullGoWoodTypes.BLUE_ENCHANTED);
        addWoodType(consumer, basePath, BYGBlocks.BULBIS_PLANKS, BYGBlocks.BULBIS_STEM, BYGBlocks.BULBIS_FENCE, OhTheBiomesYoullGoWoodTypes.BULBIS);
        addWoodType(consumer, basePath, BYGBlocks.CHERRY_PLANKS, BYGBlocks.CHERRY_LOG, BYGBlocks.CHERRY_FENCE, OhTheBiomesYoullGoWoodTypes.CHERRY);
        addWoodType(consumer, basePath, BYGBlocks.CIKA_PLANKS, BYGBlocks.CIKA_LOG, BYGBlocks.CIKA_FENCE, OhTheBiomesYoullGoWoodTypes.CIKA);
        addWoodType(consumer, basePath, BYGBlocks.CYPRESS_PLANKS, BYGBlocks.CYPRESS_LOG, BYGBlocks.CYPRESS_FENCE, OhTheBiomesYoullGoWoodTypes.CYPRESS);
        addWoodType(consumer, basePath, BYGBlocks.EBONY_PLANKS, BYGBlocks.EBONY_LOG, BYGBlocks.EBONY_FENCE, OhTheBiomesYoullGoWoodTypes.EBONY);
        addWoodType(consumer, basePath, BYGBlocks.EMBUR_PLANKS, BYGBlocks.EMBUR_PEDU, BYGBlocks.EMBUR_FENCE, OhTheBiomesYoullGoWoodTypes.EMBUR);
        addWoodType(consumer, basePath, BYGBlocks.ETHER_PLANKS, BYGBlocks.ETHER_LOG, BYGBlocks.ETHER_FENCE, OhTheBiomesYoullGoWoodTypes.ETHER);
        addWoodType(consumer, basePath, BYGBlocks.FIR_PLANKS, BYGBlocks.FIR_LOG, BYGBlocks.FIR_FENCE, OhTheBiomesYoullGoWoodTypes.FIR);
        addWoodType(consumer, basePath, BYGBlocks.GREEN_ENCHANTED_PLANKS, BYGBlocks.GREEN_ENCHANTED_LOG, BYGBlocks.GREEN_ENCHANTED_FENCE, OhTheBiomesYoullGoWoodTypes.GREEN_ENCHANTED);
        addWoodType(consumer, basePath, BYGBlocks.HOLLY_PLANKS, BYGBlocks.HOLLY_LOG, BYGBlocks.HOLLY_FENCE, OhTheBiomesYoullGoWoodTypes.HOLLY);
        addWoodType(consumer, basePath, BYGBlocks.IMPARIUS_PLANKS, BYGBlocks.IMPARIUS_STEM, BYGBlocks.IMPARIUS_FENCE, OhTheBiomesYoullGoWoodTypes.IMPARIUS);
        addWoodType(consumer, basePath, BYGBlocks.JACARANDA_PLANKS, BYGBlocks.JACARANDA_LOG, BYGBlocks.JACARANDA_FENCE, OhTheBiomesYoullGoWoodTypes.JACARANDA);
        addWoodType(consumer, basePath, BYGBlocks.LAMENT_PLANKS, BYGBlocks.LAMENT_LOG, BYGBlocks.LAMENT_FENCE, OhTheBiomesYoullGoWoodTypes.LAMENT);
        addWoodType(consumer, basePath, BYGBlocks.MAHOGANY_PLANKS, BYGBlocks.MAHOGANY_LOG, BYGBlocks.MAHOGANY_FENCE, OhTheBiomesYoullGoWoodTypes.MAHOGANY);
        addWoodType(consumer, basePath, BYGBlocks.MANGROVE_PLANKS, BYGBlocks.MANGROVE_LOG, BYGBlocks.MANGROVE_FENCE, OhTheBiomesYoullGoWoodTypes.MANGROVE);
        addWoodType(consumer, basePath, BYGBlocks.MAPLE_PLANKS, BYGBlocks.MAPLE_LOG, BYGBlocks.MAPLE_FENCE, OhTheBiomesYoullGoWoodTypes.MAPLE);
        addWoodType(consumer, basePath, BYGBlocks.NIGHTSHADE_PLANKS, BYGBlocks.NIGHTSHADE_LOG, BYGBlocks.NIGHTSHADE_FENCE, OhTheBiomesYoullGoWoodTypes.NIGHTSHADE);
        addWoodType(consumer, basePath, BYGBlocks.PALM_PLANKS, BYGBlocks.PALM_LOG, BYGBlocks.PALM_FENCE, OhTheBiomesYoullGoWoodTypes.PALM);
        addWoodType(consumer, basePath, BYGBlocks.PINE_PLANKS, BYGBlocks.PINE_LOG, BYGBlocks.PINE_FENCE, OhTheBiomesYoullGoWoodTypes.PINE);
        addWoodType(consumer, basePath, BYGBlocks.RAINBOW_EUCALYPTUS_PLANKS, BYGBlocks.RAINBOW_EUCALYPTUS_LOG, BYGBlocks.RAINBOW_EUCALYPTUS_FENCE, OhTheBiomesYoullGoWoodTypes.RAINBOW_EUCALYPTUS);
        addWoodType(consumer, basePath, BYGBlocks.REDWOOD_PLANKS, BYGBlocks.REDWOOD_LOG, BYGBlocks.REDWOOD_FENCE, OhTheBiomesYoullGoWoodTypes.REDWOOD);
        addWoodType(consumer, basePath, BYGBlocks.SKYRIS_PLANKS, BYGBlocks.SKYRIS_LOG, BYGBlocks.SKYRIS_FENCE, OhTheBiomesYoullGoWoodTypes.SKYRIS);
        addWoodType(consumer, basePath, BYGBlocks.SYTHIAN_PLANKS, BYGBlocks.SYTHIAN_STEM, BYGBlocks.SYTHIAN_FENCE, OhTheBiomesYoullGoWoodTypes.SYTHIAN);
        addWoodType(consumer, basePath, BYGBlocks.WILLOW_PLANKS, BYGBlocks.WILLOW_LOG, BYGBlocks.WILLOW_FENCE, OhTheBiomesYoullGoWoodTypes.WILLOW);
        addWoodType(consumer, basePath, BYGBlocks.WITCH_HAZEL_PLANKS, BYGBlocks.WITCH_HAZEL_LOG, BYGBlocks.WITCH_HAZEL_FENCE, OhTheBiomesYoullGoWoodTypes.WITCH_HAZEL);
        addWoodType(consumer, basePath, BYGBlocks.ZELKOVA_PLANKS, BYGBlocks.ZELKOVA_LOG, BYGBlocks.ZELKOVA_FENCE, OhTheBiomesYoullGoWoodTypes.ZELKOVA);
    }

    private void addWoodType(Consumer<IFinishedRecipe> consumer, String basePath, IItemProvider planks, IItemProvider log, IItemProvider fences, IWoodType woodType) {
        ILikeWoodRecipeProvider.addWoodType(consumer, allModsLoaded, basePath, planks, log, fences, woodType, allModsLoaded, allModsLoaded);
    }
}