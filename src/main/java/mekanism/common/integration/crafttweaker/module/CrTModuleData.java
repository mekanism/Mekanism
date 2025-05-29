package mekanism.common.integration.crafttweaker.module;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod.MethodParameter;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.gear.ModuleData;
import mekanism.common.integration.crafttweaker.CrTConstants;

@ZenRegister
@NativeMethod(name = "getMaxStackSize", parameters = {}, getterName = "maxStackSize")
@NativeMethod(name = "isExclusive", parameters = @MethodParameter(type = int.class, name = "mask", description = "Mask of all flags to check exclusivity against."),
      getterName = "exclusive")
@NativeMethod(name = "getRegistryName", parameters = {}, getterName = "registryName")
@NativeTypeRegistration(value = ModuleData.class, zenCodeName = CrTConstants.CLASS_MODULE_DATA)
public class CrTModuleData {
}