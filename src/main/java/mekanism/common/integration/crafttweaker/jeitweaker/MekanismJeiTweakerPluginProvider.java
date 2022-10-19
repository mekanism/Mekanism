package mekanism.common.integration.crafttweaker.jeitweaker;

import com.blamejared.jeitweaker.api.IngredientEnumerator;
import com.blamejared.jeitweaker.api.IngredientEnumeratorRegistration;
import com.blamejared.jeitweaker.api.IngredientTypeHolder;
import com.blamejared.jeitweaker.api.IngredientTypeRegistration;
import com.blamejared.jeitweaker.api.JeiTweakerPluginProvider;
import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import mekanism.common.util.ChemicalUtil;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.IForgeRegistry;

//TODO - 1.19: Update once JEI Tweaker updates
//@JeiTweakerPlugin
public class MekanismJeiTweakerPluginProvider implements JeiTweakerPluginProvider {

    public static final IngredientTypeHolder<ICrTGasStack, GasStack> GAS = createTypeHolder("gas", ICrTGasStack.class, GasStack.class, CrTUtils.GAS_CONVERTER);
    public static final IngredientTypeHolder<ICrTInfusionStack, InfusionStack> INFUSION = createTypeHolder("infusion", ICrTInfusionStack.class, InfusionStack.class, CrTUtils.INFUSION_CONVERTER);
    public static final IngredientTypeHolder<ICrTPigmentStack, PigmentStack> PIGMENT = createTypeHolder("pigment", ICrTPigmentStack.class, PigmentStack.class, CrTUtils.PIGMENT_CONVERTER);
    public static final IngredientTypeHolder<ICrTSlurryStack, SlurryStack> SLURRY = createTypeHolder("slurry", ICrTSlurryStack.class, SlurryStack.class, CrTUtils.SLURRY_CONVERTER);

    private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>>
    IngredientTypeHolder<CRT_STACK, STACK> createTypeHolder(String type, Class<CRT_STACK> crtClass, Class<STACK> clazz, Function<STACK, CRT_STACK> converter) {
        return IngredientTypeHolder.of(Mekanism.rl(type), crtClass, clazz, ICrTChemicalStack::getInternal, converter, ICrTChemicalStack::getRegistryName,
              ICrTChemicalStack::isEqual);
    }

    @Override
    public void registerIngredientTypes(IngredientTypeRegistration registration) {
        GAS.registerTo(registration);
        INFUSION.registerTo(registration);
        PIGMENT.registerTo(registration);
        SLURRY.registerTo(registration);
    }

    @Override
    public void registerIngredientEnumerators(IngredientEnumeratorRegistration registration) {
        addEnumerators(registration, GAS, MekanismAPI.gasRegistry());
        addEnumerators(registration, INFUSION, MekanismAPI.infuseTypeRegistry());
        addEnumerators(registration, PIGMENT, MekanismAPI.pigmentRegistry());
        addEnumerators(registration, SLURRY, MekanismAPI.slurryRegistry());
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>> void
    addEnumerators(IngredientEnumeratorRegistration registration, IngredientTypeHolder<CRT_STACK, STACK> typeHolder, IForgeRegistry<CHEMICAL> registry) {
        registration.registerEnumerator(typeHolder.get(), IngredientEnumerator.ofJei(typeHolder.get(),
              registry.getValues().stream().map(chemical -> ChemicalUtil.<CHEMICAL, STACK>withAmount(chemical, FluidType.BUCKET_VOLUME)).toList()
        ));
    }
}