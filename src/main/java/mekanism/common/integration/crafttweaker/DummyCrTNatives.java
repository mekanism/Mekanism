package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.gear.ICustomModule;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;

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

    @ZenRegister
    @NativeTypeRegistration(value = ChemicalBuilder.class, zenCodeName = CrTConstants.CLASS_BUILDER_CHEMICAL + DUMMY)
    public static class CrTNativeChemicalBuilder {

        private CrTNativeChemicalBuilder() {
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = ItemStackChemicalToItemStackRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_ITEM_STACK_CHEMICAL_TO_ITEM_STACK + DUMMY)
    public static class CrTNativeItemStackChemicalToItemStackRecipe {

        private CrTNativeItemStackChemicalToItemStackRecipe() {
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
}