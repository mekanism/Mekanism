package mekanism.common.integration.crafttweaker.bracket;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.BracketValidator;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import java.util.Optional;
import java.util.function.Predicate;
import mekanism.api.MekanismAPI;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_BRACKET_VALIDATORS)
public class CrTBracketValidators {

    /**
     * Validates if there is a {@link mekanism.api.chemical.gas.Gas} with the given registry name.
     *
     * @param tokens The resource location to validate.
     *
     * @return {@code true} if valid, {@code false} otherwise.
     */
    @ZenCodeType.Method
    @BracketValidator(CrTConstants.BRACKET_GAS)
    public static boolean validateGasStack(String tokens) {
        return validate(CrTConstants.BRACKET_GAS, tokens, MekanismAPI.gasRegistry());
    }

    /**
     * Validates if there is a {@link mekanism.api.chemical.infuse.InfuseType} with the given registry name.
     *
     * @param tokens The resource location to validate.
     *
     * @return {@code true} if valid, {@code false} otherwise.
     */
    @ZenCodeType.Method
    @BracketValidator(CrTConstants.BRACKET_INFUSE_TYPE)
    public static boolean validateInfusionStack(String tokens) {
        return validate(CrTConstants.BRACKET_INFUSE_TYPE, tokens, MekanismAPI.infuseTypeRegistry());
    }

    /**
     * Validates if there is a {@link mekanism.api.chemical.pigment.Pigment} with the given registry name.
     *
     * @param tokens The resource location to validate.
     *
     * @return {@code true} if valid, {@code false} otherwise.
     */
    @ZenCodeType.Method
    @BracketValidator(CrTConstants.BRACKET_PIGMENT)
    public static boolean validatePigmentStack(String tokens) {
        return validate(CrTConstants.BRACKET_PIGMENT, tokens, MekanismAPI.pigmentRegistry());
    }

    /**
     * Validates if there is a {@link mekanism.api.chemical.slurry.Slurry} with the given registry name.
     *
     * @param tokens The resource location to validate.
     *
     * @return {@code true} if valid, {@code false} otherwise.
     */
    @ZenCodeType.Method
    @BracketValidator(CrTConstants.BRACKET_SLURRY)
    public static boolean validateSlurryStack(String tokens) {
        return validate(CrTConstants.BRACKET_SLURRY, tokens, MekanismAPI.slurryRegistry());
    }

    /**
     * Validates if there is a {@link mekanism.api.robit.RobitSkin} with the given registry name.
     *
     * @param tokens The resource location to validate.
     *
     * @return {@code true} if valid, {@code false} otherwise.
     */
    @ZenCodeType.Method
    @BracketValidator(CrTConstants.BRACKET_ROBIT_SKIN)
    public static boolean validateRobitSkin(String tokens) {
        return validate(CrTConstants.BRACKET_ROBIT_SKIN, tokens, MekanismAPI.robitSkinRegistryName());
    }

    /**
     * Validates if there is a {@link mekanism.api.gear.ModuleData} with the given registry name.
     *
     * @param tokens The resource location to validate.
     *
     * @return {@code true} if valid, {@code false} otherwise.
     */
    @ZenCodeType.Method
    @BracketValidator(CrTConstants.BRACKET_MODULE_DATA)
    public static boolean validateModuleData(String tokens) {
        return validate(CrTConstants.BRACKET_MODULE_DATA, tokens, MekanismAPI.moduleRegistry());
    }

    private static boolean validate(String bracket, String tokens, IForgeRegistry<?> registry) {
        return validate(bracket, tokens, registryName -> isRegistryUnlocked(registry) || registry.containsKey(registryName));
    }

    private static boolean isRegistryUnlocked(IForgeRegistry<?> registry) {
        return registry instanceof ForgeRegistry<?> forgeRegistry && !forgeRegistry.isLocked();
    }

    private static boolean validate(String bracket, String tokens, ResourceKey<? extends Registry<?>> registryKey) {
        return validate(bracket, tokens, registryName -> {
            Optional<Registry<Object>> registry = CraftTweakerAPI.getAccessibleElementsProvider()
                  .registryAccess()
                  .registry(registryKey);
            return registry.isEmpty() || registry.get().containsKey(registryName);
        });
    }

    private static boolean validate(String bracket, String tokens, Predicate<ResourceLocation> unlockedOrHas) {
        ResourceLocation registryName = ResourceLocation.tryParse(tokens);
        if (registryName == null) {
            CrTConstants.CRT_LOGGER.error("Could not get BEP <{}:{}>. Syntax is <{}:modid:{}_name>", bracket, tokens, bracket, bracket);
            return false;
        }
        if (unlockedOrHas.test(registryName)) {
            return true;
        }
        String typeName = bracket.replace("_", " ");
        CrTConstants.CRT_LOGGER.error("Could not get {} for <{}:{}>, {} does not appear to exist!", typeName, bracket, tokens, typeName);
        return false;
    }
}