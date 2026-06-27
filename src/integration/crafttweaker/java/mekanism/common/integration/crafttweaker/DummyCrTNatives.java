package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.gear.ICustomModule;
import mekanism.api.recipes.MekanismRecipe;

/// Registers some "unused" and non instantiatable classes to ZenCode so that they can be resolved when ZenCode is resolving generics even if they don't need to be used
/// on the ZenCode side and are just there to make the Java side easier to manage.
public class DummyCrTNatives {

    private DummyCrTNatives() {
    }

    private static final String DUMMY = "NativeDummy";

    @ZenRegister
    @NativeTypeRegistration(value = ICustomModule.class, zenCodeName = CrTConstants.CLASS_CUSTOM_MODULE + DUMMY)
    public static class CrTNativeCustomModule {

        private CrTNativeCustomModule() {
        }
    }

    //TODO: Remove the below dummies once https://github.com/ZenCodeLang/ZenCode/issues/97 is resolved
    @ZenRegister
    @NativeTypeRegistration(value = MekanismRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_MANAGER + DUMMY)
    public static class CrTNativeMekanismRecipe {

        private CrTNativeMekanismRecipe() {
        }
    }
}