package mekanism.common.integration.crafttweaker.module;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.gear.ModuleData;
import mekanism.common.integration.crafttweaker.CrTConstants;

@ZenRegister
@NativeMethod(name = "getRarity", parameters = {}, getterName = "rarity")
@NativeMethod(name = "getMaxStackSize", parameters = {}, getterName = "maxStackSize")
@NativeMethod(name = "isExclusive", parameters = {}, getterName = "exclusive")
@NativeTypeRegistration(value = ModuleData.class, zenCodeName = CrTConstants.CLASS_MODULE_DATA)
public class CrTModuleData {
}