package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.tag.CraftTweakerTagRegistry;
import com.blamejared.crafttweaker.api.tag.manager.type.KnownTagManager;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTSlurryStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public class CrTUtils {

    /**
     * Creates a {@link ResourceLocation} in CraftTweaker's domain from the given path.
     *
     * @param path Path of the resource location
     *
     * @return Resource location in CraftTweaker's domain.
     */
    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MekanismHooks.CRAFTTWEAKER_MOD_ID, path);
    }

    /**
     * Helper to create an {@link ICrTGasStack} from a {@link Gas} with a stack size of one mB.
     */
    public static ICrTGasStack stackFromGas(Gas gas) {
        return new CrTGasStack(gas.getStack(1));
    }

    /**
     * Helper to create an {@link ICrTInfusionStack} from a {@link InfuseType} with a stack size of one mB.
     */
    public static ICrTInfusionStack stackFromInfuseType(InfuseType infuseType) {
        return new CrTInfusionStack(infuseType.getStack(1));
    }

    /**
     * Helper to create an {@link ICrTPigmentStack} from a {@link Pigment} with a stack size of one mB.
     */
    public static ICrTPigmentStack stackFromPigment(Pigment pigment) {
        return new CrTPigmentStack(pigment.getStack(1));
    }

    /**
     * Helper to create an {@link ICrTSlurryStack} from a {@link Slurry} with a stack size of one mB.
     */
    public static ICrTSlurryStack stackFromSlurry(Slurry slurry) {
        return new CrTSlurryStack(slurry.getStack(1));
    }

    /**
     * Helper method to convert a {@link BoxedChemicalStack} to an {@link ICrTChemicalStack}.
     *
     * @return {@link ICrTChemicalStack} representation of the given stack or {@code null} if empty.
     */
    @Nullable
    public static ICrTChemicalStack<?, ?, ?> fromBoxedStack(BoxedChemicalStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return switch (stack.getChemicalType()) {
            case GAS -> new CrTGasStack((GasStack) stack.getChemicalStack());
            case INFUSION -> new CrTInfusionStack((InfusionStack) stack.getChemicalStack());
            case PIGMENT -> new CrTPigmentStack((PigmentStack) stack.getChemicalStack());
            case SLURRY -> new CrTSlurryStack((SlurryStack) stack.getChemicalStack());
        };
    }

    /**
     * Helper method for describing the outputs of a recipe that may have multiple outputs.
     */
    public static <TYPE> String describeOutputs(List<TYPE> outputs, Function<TYPE, Object> converter) {
        int size = outputs.size();
        if (size == 0) {
            return "";
        } else if (size == 1) {
            return converter.apply(outputs.get(0)).toString();
        }
        //Note: This isn't the best way to describe multiple outputs, but it is probably as close as we can get
        StringBuilder description = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                description.append(", or ");
            }
            description.append(converter.apply(outputs.get(i)));
        }
        return description.toString();
    }

    /**
     * Helper to convert a CraftTweaker type tag to a regular tag and validate it exists
     */
    public static <TYPE> TagKey<TYPE> validateTagAndGet(KnownTag<TYPE> tag) {
        if (tag.exists()) {
            return tag.getTagKey();
        }
        throw new IllegalArgumentException("Tag " + tag.getCommandString() + " does not exist.");
    }

    /**
     * Helper to convert a list of one type to a list of another.
     */
    public static <TYPE, CRT_TYPE> List<CRT_TYPE> convert(List<TYPE> elements, Function<TYPE, CRT_TYPE> converter) {
        return elements.stream().map(converter).toList();
    }

    /**
     * Helper to get CraftTweaker's item tag manager.
     */
    public static KnownTagManager<Item> itemTags() {
        return CraftTweakerTagRegistry.INSTANCE.knownTagManager(Registry.ITEM_REGISTRY);
    }

    /**
     * Helper to get CraftTweaker's fluid tag manager.
     */
    public static KnownTagManager<Fluid> fluidTags() {
        return CraftTweakerTagRegistry.INSTANCE.knownTagManager(Registry.FLUID_REGISTRY);
    }

    /**
     * Helper to get CraftTweaker's gas tag manager.
     */
    public static KnownTagManager<Gas> gasTags() {
        return CraftTweakerTagRegistry.INSTANCE.knownTagManager(MekanismAPI.gasRegistryName());
    }

    /**
     * Helper to get CraftTweaker's infuse type tag manager.
     */
    public static KnownTagManager<InfuseType> infuseTypeTags() {
        return CraftTweakerTagRegistry.INSTANCE.knownTagManager(MekanismAPI.infuseTypeRegistryName());
    }

    /**
     * Helper to get CraftTweaker's pigment tag manager.
     */
    public static KnownTagManager<Pigment> pigmentTags() {
        return CraftTweakerTagRegistry.INSTANCE.knownTagManager(MekanismAPI.pigmentRegistryName());
    }

    /**
     * Helper to get CraftTweaker's slurry tag manager.
     */
    public static KnownTagManager<Slurry> slurryTags() {
        return CraftTweakerTagRegistry.INSTANCE.knownTagManager(MekanismAPI.slurryRegistryName());
    }
}