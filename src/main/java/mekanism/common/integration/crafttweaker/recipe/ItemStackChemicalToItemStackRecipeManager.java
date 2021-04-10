package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.CompressingIRecipe;
import mekanism.common.recipe.impl.InjectingIRecipe;
import mekanism.common.recipe.impl.MetallurgicInfuserIRecipe;
import mekanism.common.recipe.impl.PaintingIRecipe;
import mekanism.common.recipe.impl.PurifyingIRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ITEM_STACK_CHEMICAL_TO_ITEM_STACK)
public abstract class ItemStackChemicalToItemStackRecipeManager<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ItemStackChemicalToItemStackRecipe<CHEMICAL, STACK, INGREDIENT>>
      extends MekanismRecipeManager<RECIPE> {

    protected ItemStackChemicalToItemStackRecipeManager(MekanismRecipeType<RECIPE, ?> recipeType) {
        super(recipeType);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, ItemStackIngredient itemInput, INGREDIENT chemicalInput, IItemStack output) {
        addRecipe(makeRecipe(getAndValidateName(name), itemInput, chemicalInput, getAndValidateNotEmpty(output)));
    }

    protected abstract RECIPE makeRecipe(ResourceLocation id, ItemStackIngredient itemInput, INGREDIENT chemicalInput, ItemStack output);

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
    public static class OsmiumCompressorRecipeManager extends ItemStackChemicalToItemStackRecipeManager<Gas, GasStack, GasStackIngredient, ItemStackGasToItemStackRecipe> {

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
    public static class PurificationRecipeManager extends ItemStackChemicalToItemStackRecipeManager<Gas, GasStack, GasStackIngredient, ItemStackGasToItemStackRecipe> {

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
    public static class ChemicalInjectionRecipeManager extends ItemStackChemicalToItemStackRecipeManager<Gas, GasStack, GasStackIngredient, ItemStackGasToItemStackRecipe> {

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
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_METALLURGIC_INFUSING)
    public static class MetallurgicInfuserRecipeManager extends ItemStackChemicalToItemStackRecipeManager<InfuseType, InfusionStack, InfusionStackIngredient,
          MetallurgicInfuserRecipe> {

        public static final MetallurgicInfuserRecipeManager INSTANCE = new MetallurgicInfuserRecipeManager();

        private MetallurgicInfuserRecipeManager() {
            super(MekanismRecipeType.METALLURGIC_INFUSING);
        }

        @Override
        protected MetallurgicInfuserRecipe makeRecipe(ResourceLocation id, ItemStackIngredient itemInput, InfusionStackIngredient infusionInput, ItemStack output) {
            return new MetallurgicInfuserIRecipe(id, itemInput, infusionInput, output);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_PAINTING)
    public static class PaintingRecipeManager extends ItemStackChemicalToItemStackRecipeManager<Pigment, PigmentStack, PigmentStackIngredient, PaintingRecipe> {

        public static final PaintingRecipeManager INSTANCE = new PaintingRecipeManager();

        private PaintingRecipeManager() {
            super(MekanismRecipeType.PAINTING);
        }

        @Override
        protected PaintingRecipe makeRecipe(ResourceLocation id, ItemStackIngredient itemInput, PigmentStackIngredient pigmentInput, ItemStack output) {
            return new PaintingIRecipe(id, itemInput, pigmentInput, output);
        }
    }
}