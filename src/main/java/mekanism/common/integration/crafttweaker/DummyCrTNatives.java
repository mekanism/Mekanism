package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.gear.ICustomModule;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.chemical.IChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IGasIngredient;
import mekanism.api.recipes.ingredients.chemical.IInfusionIngredient;
import mekanism.api.recipes.ingredients.chemical.IPigmentIngredient;
import mekanism.api.recipes.ingredients.chemical.ISlurryIngredient;
import mekanism.api.recipes.vanilla_input.BiChemicalRecipeInput;
import mekanism.api.recipes.vanilla_input.ChemicalRecipeInput;
import mekanism.api.recipes.vanilla_input.FluidChemicalRecipeInput;
import mekanism.api.recipes.vanilla_input.FluidRecipeInput;
import mekanism.api.recipes.vanilla_input.ItemChemicalRecipeInput;
import mekanism.api.recipes.vanilla_input.ReactionRecipeInput;
import mekanism.api.recipes.vanilla_input.RotaryRecipeInput;
import mekanism.api.recipes.vanilla_input.SingleBoxedChemicalInput;
import mekanism.api.recipes.vanilla_input.SingleChemicalRecipeInput;
import mekanism.api.recipes.vanilla_input.SingleFluidChemicalRecipeInput;
import mekanism.api.recipes.vanilla_input.SingleFluidRecipeInput;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;

/**
 * Registers some "unused" and non instantiatable classes to ZenCode so that they can be resolved when ZenCode is resolving generics even if they don't need to be used on
 * the ZenCode side and are just there to make the Java side easier to manage.
 */
public class DummyCrTNatives {

    private DummyCrTNatives() {
    }

    private static final String DUMMY = "NativeDummy";

    @ZenRegister
    @NativeTypeRegistration(value = ChemicalStack.class, zenCodeName = CrTConstants.CLASS_CHEMICAL_STACK + DUMMY)
    public static class CrTNativeChemicalStack {

        private CrTNativeChemicalStack() {
        }
    }

    @ZenRegister(loaders = CrTConstants.CONTENT_LOADER)
    @NativeTypeRegistration(value = ChemicalBuilder.class, zenCodeName = CrTConstants.CLASS_BUILDER_CHEMICAL + DUMMY)
    public static class CrTNativeChemicalBuilder {

        private CrTNativeChemicalBuilder() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = ICustomModule.class, zenCodeName = CrTConstants.CLASS_CUSTOM_MODULE + DUMMY)
    public static class CrTNativeCustomModule {

        private CrTNativeCustomModule() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = IModuleDataProvider.class, zenCodeName = CrTConstants.CLASS_MODULE_DATA_PROVIDER + DUMMY)
    public static class CrTNativeModuleDataProvider {

        private CrTNativeModuleDataProvider() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = IChemicalIngredient.class, zenCodeName = CrTConstants.CLASS_CHEMICAL_INGREDIENT + DUMMY)
    public static class CrTNativeChemicalIngredient {

        private CrTNativeChemicalIngredient() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = IGasIngredient.class, zenCodeName = CrTConstants.CLASS_GAS_INGREDIENT + DUMMY)
    public static class CrTNativeGasIngredient {

        private CrTNativeGasIngredient() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = IInfusionIngredient.class, zenCodeName = CrTConstants.CLASS_INFUSION_INGREDIENT + DUMMY)
    public static class CrTNativeInfusionIngredient {

        private CrTNativeInfusionIngredient() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = IPigmentIngredient.class, zenCodeName = CrTConstants.CLASS_PIGMENT_INGREDIENT + DUMMY)
    public static class CrTNativePigmentIngredient {

        private CrTNativePigmentIngredient() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = ISlurryIngredient.class, zenCodeName = CrTConstants.CLASS_SLURRY_INGREDIENT + DUMMY)
    public static class CrTNativeSlurryIngredient {

        private CrTNativeSlurryIngredient() {
        }
    }

    //TODO: Remove the below dummies once https://github.com/ZenCodeLang/ZenCode/issues/97 is resolved
    @ZenRegister
    @NativeTypeRegistration(value = IChemicalProvider.class, zenCodeName = CrTConstants.CLASS_CHEMICAL_PROVIDER + DUMMY)
    public static class CrTNativeChemicalProvider {

        private CrTNativeChemicalProvider() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = BiChemicalRecipeInput.class, zenCodeName = CrTConstants.CLASS_VANILLA_INPUT_BI_CHEMICAL + DUMMY)
    public static class CrTNativeBiChemicalRecipeInput {

        private CrTNativeBiChemicalRecipeInput() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = ChemicalRecipeInput.class, zenCodeName = CrTConstants.CLASS_VANILLA_INPUT_CHEMICAL + DUMMY)
    public static class CrTNativeChemicalRecipeInput {

        private CrTNativeChemicalRecipeInput() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = FluidChemicalRecipeInput.class, zenCodeName = CrTConstants.CLASS_VANILLA_INPUT_FLUID_CHEMICAL + DUMMY)
    public static class CrTNativeFluidChemicalRecipeInput {

        private CrTNativeFluidChemicalRecipeInput() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = FluidRecipeInput.class, zenCodeName = CrTConstants.CLASS_VANILLA_INPUT_FLUID + DUMMY)
    public static class CrTNativeFluidRecipeInput {

        private CrTNativeFluidRecipeInput() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = ItemChemicalRecipeInput.class, zenCodeName = CrTConstants.CLASS_VANILLA_INPUT_ITEM_CHEMICAL + DUMMY)
    public static class CrTNativeItemChemicalRecipeInput {

        private CrTNativeItemChemicalRecipeInput() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = SingleBoxedChemicalInput.class, zenCodeName = CrTConstants.CLASS_VANILLA_INPUT_SINGLE_BOXED + DUMMY)
    public static class CrTNativeBoxedChemicalRecipeInput {

        private CrTNativeBoxedChemicalRecipeInput() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = ReactionRecipeInput.class, zenCodeName = CrTConstants.CLASS_VANILLA_INPUT_REACTION + DUMMY)
    public static class CrTNativeReactionRecipeInput {

        private CrTNativeReactionRecipeInput() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = RotaryRecipeInput.class, zenCodeName = CrTConstants.CLASS_VANILLA_INPUT_ROTARY + DUMMY)
    public static class CrTNativeRotaryRecipeInput {

        private CrTNativeRotaryRecipeInput() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = SingleChemicalRecipeInput.class, zenCodeName = CrTConstants.CLASS_VANILLA_INPUT_SINGLE_CHEMICAL + DUMMY)
    public static class CrTNativeSingleChemicalRecipeInput {

        private CrTNativeSingleChemicalRecipeInput() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = SingleFluidChemicalRecipeInput.class, zenCodeName = CrTConstants.CLASS_VANILLA_INPUT_SINGLE_FLUID_CHEMICAL + DUMMY)
    public static class CrTNativeSingleFluidChemicalRecipeInput {

        private CrTNativeSingleFluidChemicalRecipeInput() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = SingleFluidRecipeInput.class, zenCodeName = CrTConstants.CLASS_VANILLA_INPUT_SINGLE_FLUID + DUMMY)
    public static class CrTNativeSingleFluidRecipeInput {

        private CrTNativeSingleFluidRecipeInput() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = SingleItemChemicalRecipeInput.class, zenCodeName = CrTConstants.CLASS_VANILLA_INPUT_SINGLE_ITEM_CHEMICAL + DUMMY)
    public static class CrTNativeSingleItemChemicalRecipeInput {

        private CrTNativeSingleItemChemicalRecipeInput() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = MekanismRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_MANAGER + DUMMY)
    public static class CrTNativeMekanismRecipe {

        private CrTNativeMekanismRecipe() {
        }
    }
}