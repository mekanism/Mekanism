package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.gear.ICustomModule;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.api.recipes.ingredients.chemical.IChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IGasIngredient;
import mekanism.api.recipes.ingredients.chemical.IInfusionIngredient;
import mekanism.api.recipes.ingredients.chemical.IPigmentIngredient;
import mekanism.api.recipes.ingredients.chemical.ISlurryIngredient;
import mekanism.common.Mekanism;

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

    //TODO - 1.18: Remove the below dummies once https://github.com/ZenCodeLang/ZenCode/issues/97 is resolved
    @ZenRegister
    @NativeTypeRegistration(value = IChemicalProvider.class, zenCodeName = "mods." + Mekanism.MODID + ".api.provider.ChemicalProvider" + DUMMY)
    public static class CrTNativeChemicalProvider {

        private CrTNativeChemicalProvider() {
        }
    }
}