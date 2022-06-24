package mekanism.common.recipe.compat;

import java.util.function.Consumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import potionstudios.byg.common.block.BYGWoodTypes;
import yamahari.ilikewood.plugin.byg.OhTheBiomesYoullGoWoodTypes;
import yamahari.ilikewood.registry.woodtype.IWoodType;

@ParametersAreNotNullByDefault
public class ILikeWoodBYGRecipeProvider extends CompatRecipeProvider {

    public ILikeWoodBYGRecipeProvider() {
        super(yamahari.ilikewood.plugin.byg.util.Constants.MOD_ID, "byg");
    }

    @Override
    protected void registerRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        addWoodType(consumer, basePath, BYGWoodTypes.ASPEN, OhTheBiomesYoullGoWoodTypes.ASPEN);
        addWoodType(consumer, basePath, BYGWoodTypes.BAOBAB, OhTheBiomesYoullGoWoodTypes.BAOBAB);
        addWoodType(consumer, basePath, BYGWoodTypes.BLUE_ENCHANTED, OhTheBiomesYoullGoWoodTypes.BLUE_ENCHANTED);
        addWoodType(consumer, basePath, BYGWoodTypes.BULBIS, OhTheBiomesYoullGoWoodTypes.BULBIS);
        addWoodType(consumer, basePath, BYGWoodTypes.CHERRY, OhTheBiomesYoullGoWoodTypes.CHERRY);
        addWoodType(consumer, basePath, BYGWoodTypes.CIKA, OhTheBiomesYoullGoWoodTypes.CIKA);
        addWoodType(consumer, basePath, BYGWoodTypes.CYPRESS, OhTheBiomesYoullGoWoodTypes.CYPRESS);
        addWoodType(consumer, basePath, BYGWoodTypes.EBONY, OhTheBiomesYoullGoWoodTypes.EBONY);
        addWoodType(consumer, basePath, BYGWoodTypes.EMBUR, OhTheBiomesYoullGoWoodTypes.EMBUR);
        addWoodType(consumer, basePath, BYGWoodTypes.ETHER, OhTheBiomesYoullGoWoodTypes.ETHER);
        addWoodType(consumer, basePath, BYGWoodTypes.FIR, OhTheBiomesYoullGoWoodTypes.FIR);
        addWoodType(consumer, basePath, BYGWoodTypes.GREEN_ENCHANTED, OhTheBiomesYoullGoWoodTypes.GREEN_ENCHANTED);
        addWoodType(consumer, basePath, BYGWoodTypes.HOLLY, OhTheBiomesYoullGoWoodTypes.HOLLY);
        addWoodType(consumer, basePath, BYGWoodTypes.IMPARIUS, OhTheBiomesYoullGoWoodTypes.IMPARIUS);
        addWoodType(consumer, basePath, BYGWoodTypes.JACARANDA, OhTheBiomesYoullGoWoodTypes.JACARANDA);
        addWoodType(consumer, basePath, BYGWoodTypes.LAMENT, OhTheBiomesYoullGoWoodTypes.LAMENT);
        addWoodType(consumer, basePath, BYGWoodTypes.MAHOGANY, OhTheBiomesYoullGoWoodTypes.MAHOGANY);
        addWoodType(consumer, basePath, BYGWoodTypes.WHITE_MANGROVE, OhTheBiomesYoullGoWoodTypes.MANGROVE);
        addWoodType(consumer, basePath, BYGWoodTypes.MAPLE, OhTheBiomesYoullGoWoodTypes.MAPLE);
        addWoodType(consumer, basePath, BYGWoodTypes.NIGHTSHADE, OhTheBiomesYoullGoWoodTypes.NIGHTSHADE);
        addWoodType(consumer, basePath, BYGWoodTypes.PALM, OhTheBiomesYoullGoWoodTypes.PALM);
        addWoodType(consumer, basePath, BYGWoodTypes.PINE, OhTheBiomesYoullGoWoodTypes.PINE);
        addWoodType(consumer, basePath, BYGWoodTypes.REDWOOD, OhTheBiomesYoullGoWoodTypes.REDWOOD);
        addWoodType(consumer, basePath, BYGWoodTypes.SKYRIS, OhTheBiomesYoullGoWoodTypes.SKYRIS);
        addWoodType(consumer, basePath, BYGWoodTypes.SYTHIAN, OhTheBiomesYoullGoWoodTypes.SYTHIAN);
        addWoodType(consumer, basePath, BYGWoodTypes.WILLOW, OhTheBiomesYoullGoWoodTypes.WILLOW);
        addWoodType(consumer, basePath, BYGWoodTypes.WITCH_HAZEL, OhTheBiomesYoullGoWoodTypes.WITCH_HAZEL);
        addWoodType(consumer, basePath, BYGWoodTypes.ZELKOVA, OhTheBiomesYoullGoWoodTypes.ZELKOVA);
    }

    private void addWoodType(Consumer<FinishedRecipe> consumer, String basePath, BYGWoodTypes bygWoodType, IWoodType woodType) {
        ILikeWoodRecipeProvider.addWoodType(consumer, allModsLoaded, basePath, bygWoodType.planks(), bygWoodType.log(), bygWoodType.fence(), woodType);
    }
}