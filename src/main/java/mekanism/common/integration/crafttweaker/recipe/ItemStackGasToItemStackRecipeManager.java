package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ITEM_STACK_GAS_TO_ITEM_STACK)
public abstract class ItemStackGasToItemStackRecipeManager extends MekanismRecipeManager {

    protected ItemStackGasToItemStackRecipeManager() {
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_COMPRESSING)
    public static class OsmiumCompressorRecipeManager extends ItemStackGasToItemStackRecipeManager {

        public static final OsmiumCompressorRecipeManager INSTANCE = new OsmiumCompressorRecipeManager();

        private OsmiumCompressorRecipeManager() {
        }

        @Override
        public IRecipeType<ItemStackGasToItemStackRecipe> getRecipeType() {
            return MekanismRecipeType.COMPRESSING;
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_PURIFYING)
    public static class PurificationRecipeManager extends ItemStackGasToItemStackRecipeManager {

        public static final PurificationRecipeManager INSTANCE = new PurificationRecipeManager();

        private PurificationRecipeManager() {
        }

        @Override
        public IRecipeType<ItemStackGasToItemStackRecipe> getRecipeType() {
            return MekanismRecipeType.PURIFYING;
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_INJECTING)
    public static class ChemicalInjectionRecipeManager extends ItemStackGasToItemStackRecipeManager {

        public static final ChemicalInjectionRecipeManager INSTANCE = new ChemicalInjectionRecipeManager();

        private ChemicalInjectionRecipeManager() {
        }

        @Override
        public IRecipeType<ItemStackGasToItemStackRecipe> getRecipeType() {
            return MekanismRecipeType.INJECTING;
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_NUCLEOSYNTHESIZING)
    public static class NucleosynthesizingRecipeManager extends MekanismRecipeManager {

        public static final NucleosynthesizingRecipeManager INSTANCE = new NucleosynthesizingRecipeManager();

        private NucleosynthesizingRecipeManager() {
        }

        @Override
        public IRecipeType<NucleosynthesizingRecipe> getRecipeType() {
            //TODO: Note this also needs a duration
            return MekanismRecipeType.NUCLEOSYNTHESIZING;
        }
    }

    private static class ActionAddItemStackGasToItemStackRecipe extends ActionAddMekanismRecipe<ItemStackGasToItemStackRecipe> {

        protected ActionAddItemStackGasToItemStackRecipe(MekanismRecipeManager recipeManager, ItemStackGasToItemStackRecipe recipe) {
            super(recipeManager, recipe);
        }

        @Override
        protected String describeOutputs() {
            return CrTUtils.describeOutputs(getRecipe().getOutputDefinition(), MCItemStackMutable::new);
        }
    }
}