package mekanism.common.integration.crafttweaker.module;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.gear.ModuleData;
import mekanism.common.integration.crafttweaker.CrTConstants;

@ZenRegister
@NativeTypeRegistration(value = ModuleData.class, zenCodeName = CrTConstants.CLASS_MODULE_DATA)
public class CrTModuleData {
}