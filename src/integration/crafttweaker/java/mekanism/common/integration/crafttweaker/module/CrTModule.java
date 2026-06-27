package mekanism.common.integration.crafttweaker.module;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.gear.IModule;
import mekanism.common.integration.crafttweaker.CrTConstants;

@ZenRegister
@NativeMethod(name = "getData", parameters = {}, getterName = "data")
@NativeMethod(name = "getInstalledCount", parameters = {}, getterName = "installed")
@NativeMethod(name = "isEnabled", parameters = {}, getterName = "enabled")
@NativeTypeRegistration(value = IModule.class, zenCodeName = CrTConstants.CLASS_MODULE)
public class CrTModule {
}