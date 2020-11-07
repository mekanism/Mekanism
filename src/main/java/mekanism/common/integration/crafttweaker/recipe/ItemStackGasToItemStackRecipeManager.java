package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ITEM_STACK_GAS_TO_ITEM_STACK)
public abstract class ItemStackGasToItemStackRecipeManager<RECIPE extends ItemStackGasToItemStackRecipe> extends MekanismRecipeManager<RECIPE> {

    protected ItemStackGasToItemStackRecipeManager(MekanismRecipeType<RECIPE> recipeType) {
        super(recipeType);
    }

    @Override
    protected ActionAddMekanismRecipe getAction(RECIPE recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(getRecipe().getOutputDefinition(), MCItemStackMutable::new);
            }
        };
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_COMPRESSING)
    public static class OsmiumCompressorRecipeManager extends ItemStackGasToItemStackRecipeManager<ItemStackGasToItemStackRecipe> {

        public static final OsmiumCompressorRecipeManager INSTANCE = new OsmiumCompressorRecipeManager();

        private OsmiumCompressorRecipeManager() {
            super(MekanismRecipeType.COMPRESSING);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_PURIFYING)
    public static class PurificationRecipeManager extends ItemStackGasToItemStackRecipeManager<ItemStackGasToItemStackRecipe> {

        public static final PurificationRecipeManager INSTANCE = new PurificationRecipeManager();

        private PurificationRecipeManager() {
            super(MekanismRecipeType.PURIFYING);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_INJECTING)
    public static class ChemicalInjectionRecipeManager extends ItemStackGasToItemStackRecipeManager<ItemStackGasToItemStackRecipe> {

        public static final ChemicalInjectionRecipeManager INSTANCE = new ChemicalInjectionRecipeManager();

        private ChemicalInjectionRecipeManager() {
            super(MekanismRecipeType.INJECTING);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_NUCLEOSYNTHESIZING)
    public static class NucleosynthesizingRecipeManager extends ItemStackGasToItemStackRecipeManager<NucleosynthesizingRecipe> {

        public static final NucleosynthesizingRecipeManager INSTANCE = new NucleosynthesizingRecipeManager();

        private NucleosynthesizingRecipeManager() {
            super(MekanismRecipeType.NUCLEOSYNTHESIZING);
            //TODO: Note this also needs a duration
        }
    }
}