package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.ingredient.CrTGasStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.CompressingIRecipe;
import mekanism.common.recipe.impl.InjectingIRecipe;
import mekanism.common.recipe.impl.NucleosynthesizingIRecipe;
import mekanism.common.recipe.impl.PurifyingIRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ITEM_STACK_GAS_TO_ITEM_STACK)
public abstract class ItemStackGasToItemStackRecipeManager<RECIPE extends ItemStackGasToItemStackRecipe> extends MekanismRecipeManager<RECIPE> {

    protected ItemStackGasToItemStackRecipeManager(MekanismRecipeType<RECIPE> recipeType) {
        super(recipeType);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CrTItemStackIngredient itemInput, CrTGasStackIngredient gasInput, IItemStack output) {
        addRecipe(makeRecipe(getAndValidateName(name), itemInput.getInternal(), gasInput.getInternal(), getAndValidateNotEmpty(output)));
    }

    protected abstract RECIPE makeRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output);

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

        @Override
        protected ItemStackGasToItemStackRecipe makeRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
            return new CompressingIRecipe(id, itemInput, gasInput, output);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_PURIFYING)
    public static class PurificationRecipeManager extends ItemStackGasToItemStackRecipeManager<ItemStackGasToItemStackRecipe> {

        public static final PurificationRecipeManager INSTANCE = new PurificationRecipeManager();

        private PurificationRecipeManager() {
            super(MekanismRecipeType.PURIFYING);
        }

        @Override
        protected ItemStackGasToItemStackRecipe makeRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
            return new PurifyingIRecipe(id, itemInput, gasInput, output);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_INJECTING)
    public static class ChemicalInjectionRecipeManager extends ItemStackGasToItemStackRecipeManager<ItemStackGasToItemStackRecipe> {

        public static final ChemicalInjectionRecipeManager INSTANCE = new ChemicalInjectionRecipeManager();

        private ChemicalInjectionRecipeManager() {
            super(MekanismRecipeType.INJECTING);
        }

        @Override
        protected ItemStackGasToItemStackRecipe makeRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
            return new InjectingIRecipe(id, itemInput, gasInput, output);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_NUCLEOSYNTHESIZING)
    public static class NucleosynthesizingRecipeManager extends ItemStackGasToItemStackRecipeManager<NucleosynthesizingRecipe> {

        public static final NucleosynthesizingRecipeManager INSTANCE = new NucleosynthesizingRecipeManager();

        private NucleosynthesizingRecipeManager() {
            super(MekanismRecipeType.NUCLEOSYNTHESIZING);
        }

        @Override
        @Deprecated
        protected NucleosynthesizingRecipe makeRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
            throw new UnsupportedOperationException("Nucleosynthesizing recipes require a duration.");
        }

        @ZenCodeType.Method
        public void addRecipe(String name, CrTItemStackIngredient itemInput, CrTGasStackIngredient gasInput, IItemStack output, int duration) {
            if (duration <= 0) {
                throw new IllegalArgumentException("Duration must be a number greater than zero! Duration: " + duration);
            }
            addRecipe(new NucleosynthesizingIRecipe(getAndValidateName(name), itemInput.getInternal(), gasInput.getInternal(), getAndValidateNotEmpty(output), duration));
        }
    }
}