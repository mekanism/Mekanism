package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.actions.tags.ActionTagAdd;
import com.blamejared.crafttweaker.impl.actions.tags.ActionTagCreate;
import com.blamejared.crafttweaker.impl.actions.tags.ActionTagRemove;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.IntFunction;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.integration.crafttweaker.chemical.CrTChemical.CrTGas;
import mekanism.common.integration.crafttweaker.chemical.CrTChemical.CrTInfuseType;
import mekanism.common.integration.crafttweaker.chemical.CrTChemical.CrTPigment;
import mekanism.common.integration.crafttweaker.chemical.CrTChemical.CrTSlurry;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTGas;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTInfuseType;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTPigment;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTSlurry;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.Tag;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_MCTAG)
public class CrTTags {//TODO: Rewrite this into various tag managers once the CrT tag system finishes getting improved

    private static final Map<MCTag, ITag<Gas>> GAS_TAG_CACHE = new WeakHashMap<>();
    private static final Map<MCTag, ITag<InfuseType>> INFUSE_TYPE_TAG_CACHE = new WeakHashMap<>();
    private static final Map<MCTag, ITag<Pigment>> PIGMENT_TAG_CACHE = new WeakHashMap<>();
    private static final Map<MCTag, ITag<Slurry>> SLURRY_TAG_CACHE = new WeakHashMap<>();

    public static ITag<Gas> getGasTag(MCTag tag) {
        return GAS_TAG_CACHE.computeIfAbsent(tag, t -> ChemicalTags.GAS.getCollection().get(t.getInternalID()));
    }

    public static ITag<InfuseType> getInfuseTypeTag(MCTag tag) {
        return INFUSE_TYPE_TAG_CACHE.computeIfAbsent(tag, t -> ChemicalTags.INFUSE_TYPE.getCollection().get(t.getInternalID()));
    }

    public static ITag<Pigment> getPigmentTag(MCTag tag) {
        return PIGMENT_TAG_CACHE.computeIfAbsent(tag, t -> ChemicalTags.PIGMENT.getCollection().get(t.getInternalID()));
    }

    public static ITag<Slurry> getSlurryTag(MCTag tag) {
        return SLURRY_TAG_CACHE.computeIfAbsent(tag, t -> ChemicalTags.SLURRY.getCollection().get(t.getInternalID()));
    }

    @ZenCodeType.Method
    public static boolean contains(MCTag tag, ICrTChemical<?, ?, ?, ?> chemical) {
        return chemical.isIn(tag);
    }

    @ZenCodeType.Method
    public static MCTag createGasTag(MCTag tag) {
        return createChemicalTag(tag, ChemicalTags.GAS, "Gas");
    }

    @ZenCodeType.Method
    public static MCTag createInfuseTypeTag(MCTag tag) {
        return createChemicalTag(tag, ChemicalTags.INFUSE_TYPE, "Infuse Type");
    }

    @ZenCodeType.Method
    public static MCTag createPigmentTag(MCTag tag) {
        return createChemicalTag(tag, ChemicalTags.PIGMENT, "Pigment");
    }

    @ZenCodeType.Method
    public static MCTag createSlurryTag(MCTag tag) {
        return createChemicalTag(tag, ChemicalTags.SLURRY, "Slurry");
    }

    private static MCTag createChemicalTag(MCTag tag, ChemicalTags<?> tags, String type) {
        CraftTweakerAPI.apply(new ActionTagCreate<>(tags.getCollection(), type, Tag.getTagFromContents(Sets.newHashSet()), tag.getInternalID()));
        return tag;
    }

    @ZenCodeType.Getter("isGasTag")
    public static boolean isGasTag(MCTag tag) {
        return getGasTag(tag) != null;
    }

    @ZenCodeType.Getter("isInfuseTypeTag")
    public static boolean isInfuseTypeTag(MCTag tag) {
        return getInfuseTypeTag(tag) != null;
    }

    @ZenCodeType.Getter("isPigmentTag")
    public static boolean isPigmentTag(MCTag tag) {
        return getPigmentTag(tag) != null;
    }

    @ZenCodeType.Getter("isSlurryTag")
    public static boolean isSlurryTag(MCTag tag) {
        return getSlurryTag(tag) != null;
    }

    @ZenCodeType.Getter("gases")
    public static ICrTGas[] getGases(MCTag tag) {
        return getChemicals(tag, CrTTags::getGasTag, CrTGas[]::new, CrTGas::new, "GasTag");
    }

    @ZenCodeType.Getter("infuse_types")
    public static ICrTInfuseType[] getInfuseTypes(MCTag tag) {
        return getChemicals(tag, CrTTags::getInfuseTypeTag, CrTInfuseType[]::new, CrTInfuseType::new, "InfuseTypeTag");
    }

    @ZenCodeType.Getter("pigments")
    public static ICrTPigment[] getPigments(MCTag tag) {
        return getChemicals(tag, CrTTags::getPigmentTag, CrTPigment[]::new, CrTPigment::new, "PigmentTag");
    }

    @ZenCodeType.Getter("slurries")
    public static ICrTSlurry[] getSlurries(MCTag tag) {
        return getChemicals(tag, CrTTags::getSlurryTag, CrTSlurry[]::new, CrTSlurry::new, "SlurryTag");
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
          CRT_CHEMICAL extends ICrTChemical<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>>
    CRT_CHEMICAL[] getChemicals(MCTag tag, Function<MCTag, ITag<CHEMICAL>> tagGetter, IntFunction<CRT_CHEMICAL[]> arrayCreator,
          Function<CHEMICAL, CRT_CHEMICAL> chemicalConverter, String tagTypeName) {
        ITag<CHEMICAL> chemicalTag = tagGetter.apply(tag);
        if (chemicalTag == null) {
            CraftTweakerAPI.logError("\"%s\" is not a %s!", tag.getCommandString(), tagTypeName);
            return arrayCreator.apply(0);
        }
        List<CHEMICAL> elements = chemicalTag.getAllElements();
        CRT_CHEMICAL[] chemicals = arrayCreator.apply(elements.size());
        for (int i = 0; i < chemicals.length; i++) {
            chemicals[i] = chemicalConverter.apply(elements.get(i));
        }
        return chemicals;
    }

    @ZenCodeType.Getter("firstGas")
    public static ICrTGas getFirstGas(MCTag tag) {
        return getFirstChemical(tag, CrTTags::getGasTag, CrTGas::new, "GasTag");
    }

    @ZenCodeType.Getter("firstInfuseType")
    public static ICrTInfuseType getFirstInfuseType(MCTag tag) {
        return getFirstChemical(tag, CrTTags::getInfuseTypeTag, CrTInfuseType::new, "InfuseTypeTag");
    }

    @ZenCodeType.Getter("firstPigment")
    public static ICrTPigment getFirstPigment(MCTag tag) {
        return getFirstChemical(tag, CrTTags::getPigmentTag, CrTPigment::new, "PigmentTag");
    }

    @ZenCodeType.Getter("firstSlurry")
    public static ICrTSlurry getFirstSlurry(MCTag tag) {
        return getFirstChemical(tag, CrTTags::getSlurryTag, CrTSlurry::new, "SlurryTag");
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
          CRT_CHEMICAL extends ICrTChemical<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>>
    CRT_CHEMICAL getFirstChemical(MCTag tag, Function<MCTag, ITag<CHEMICAL>> tagGetter, Function<CHEMICAL, CRT_CHEMICAL> chemicalConverter, String tagTypeName) {
        ITag<CHEMICAL> chemicalTag = tagGetter.apply(tag);
        if (chemicalTag == null) {
            throw new IllegalArgumentException("\"" + tag.getCommandString() + "\" is not a " + tagTypeName + "!");
        }
        Optional<CHEMICAL> first = chemicalTag.getAllElements().stream().findFirst();
        if (first.isPresent()) {
            return chemicalConverter.apply(first.get());
        }
        throw new NoSuchElementException("Could not get get first element of \"" + tag.getCommandString() + "\" as it is an empty " + tagTypeName + "!");
    }

    @ZenCodeType.Method
    public static void addGases(MCTag tag, ICrTGas... gases) {
        CraftTweakerAPI.apply(new ActionTagAdd<>(getGasTag(tag), CrTUtils.getChemicals(gases, Gas[]::new), tag.getInternalID()));
    }

    @ZenCodeType.Method
    public static void addInfuseTypes(MCTag tag, ICrTInfuseType... infuseTypes) {
        CraftTweakerAPI.apply(new ActionTagAdd<>(getInfuseTypeTag(tag), CrTUtils.getChemicals(infuseTypes, InfuseType[]::new), tag.getInternalID()));
    }

    @ZenCodeType.Method
    public static void addPigments(MCTag tag, ICrTPigment... pigments) {
        CraftTweakerAPI.apply(new ActionTagAdd<>(getPigmentTag(tag), CrTUtils.getChemicals(pigments, Pigment[]::new), tag.getInternalID()));
    }

    @ZenCodeType.Method
    public static void addSlurries(MCTag tag, ICrTSlurry... slurries) {
        CraftTweakerAPI.apply(new ActionTagAdd<>(getSlurryTag(tag), CrTUtils.getChemicals(slurries, Slurry[]::new), tag.getInternalID()));
    }

    @ZenCodeType.Method
    public static void removeGases(MCTag tag, ICrTGas... gases) {
        CraftTweakerAPI.apply(new ActionTagRemove<>(getGasTag(tag), CrTUtils.getChemicals(gases, Gas[]::new), tag.getInternalID()));
    }

    @ZenCodeType.Method
    public static void removeInfuseTypes(MCTag tag, ICrTInfuseType... infuseTypes) {
        CraftTweakerAPI.apply(new ActionTagRemove<>(getInfuseTypeTag(tag), CrTUtils.getChemicals(infuseTypes, InfuseType[]::new), tag.getInternalID()));
    }

    @ZenCodeType.Method
    public static void removePigments(MCTag tag, ICrTPigment... pigments) {
        CraftTweakerAPI.apply(new ActionTagRemove<>(getPigmentTag(tag), CrTUtils.getChemicals(pigments, Pigment[]::new), tag.getInternalID()));
    }

    @ZenCodeType.Method
    public static void removeSlurries(MCTag tag, ICrTSlurry... slurries) {
        CraftTweakerAPI.apply(new ActionTagRemove<>(getSlurryTag(tag), CrTUtils.getChemicals(slurries, Slurry[]::new), tag.getInternalID()));
    }
}