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
    public static boolean contains(MCTag _this, ICrTChemical<?, ?, ?, ?> chemical) {
        return chemical.isIn(_this);
    }

    @ZenCodeType.Method
    public static MCTag createGasTag(MCTag _this) {
        return createChemicalTag(_this, ChemicalTags.GAS, "Gas");
    }

    @ZenCodeType.Method
    public static MCTag createInfuseTypeTag(MCTag _this) {
        return createChemicalTag(_this, ChemicalTags.INFUSE_TYPE, "Infuse Type");
    }

    @ZenCodeType.Method
    public static MCTag createPigmentTag(MCTag _this) {
        return createChemicalTag(_this, ChemicalTags.PIGMENT, "Pigment");
    }

    @ZenCodeType.Method
    public static MCTag createSlurryTag(MCTag _this) {
        return createChemicalTag(_this, ChemicalTags.SLURRY, "Slurry");
    }

    private static MCTag createChemicalTag(MCTag _this, ChemicalTags<?> tags, String type) {
        CraftTweakerAPI.apply(new ActionTagCreate<>(tags.getCollection(), type, Tag.getTagFromContents(Sets.newHashSet()), _this.getInternalID()));
        return _this;
    }

    @ZenCodeType.Getter("isGasTag")
    public static boolean isGasTag(MCTag _this) {
        return getGasTag(_this) != null;
    }

    @ZenCodeType.Getter("isInfuseTypeTag")
    public static boolean isInfuseTypeTag(MCTag _this) {
        return getInfuseTypeTag(_this) != null;
    }

    @ZenCodeType.Getter("isPigmentTag")
    public static boolean isPigmentTag(MCTag _this) {
        return getPigmentTag(_this) != null;
    }

    @ZenCodeType.Getter("isSlurryTag")
    public static boolean isSlurryTag(MCTag _this) {
        return getSlurryTag(_this) != null;
    }

    @ZenCodeType.Getter("gases")
    public static ICrTGas[] getGases(MCTag _this) {
        return getChemicals(_this, CrTTags::getGasTag, CrTGas[]::new, CrTGas::new, "GasTag");
    }

    @ZenCodeType.Getter("infuse_types")
    public static ICrTInfuseType[] getInfuseTypes(MCTag _this) {
        return getChemicals(_this, CrTTags::getInfuseTypeTag, CrTInfuseType[]::new, CrTInfuseType::new, "InfuseTypeTag");
    }

    @ZenCodeType.Getter("pigments")
    public static ICrTPigment[] getPigments(MCTag _this) {
        return getChemicals(_this, CrTTags::getPigmentTag, CrTPigment[]::new, CrTPigment::new, "PigmentTag");
    }

    @ZenCodeType.Getter("slurries")
    public static ICrTSlurry[] getSlurries(MCTag _this) {
        return getChemicals(_this, CrTTags::getSlurryTag, CrTSlurry[]::new, CrTSlurry::new, "SlurryTag");
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
          CRT_CHEMICAL extends ICrTChemical<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>>
    CRT_CHEMICAL[] getChemicals(MCTag _this, Function<MCTag, ITag<CHEMICAL>> tagGetter, IntFunction<CRT_CHEMICAL[]> arrayCreator,
          Function<CHEMICAL, CRT_CHEMICAL> chemicalConverter, String tagTypeName) {
        ITag<CHEMICAL> chemicalTag = tagGetter.apply(_this);
        if (chemicalTag == null) {
            CraftTweakerAPI.logError("\"%s\" is not a %s!", _this.getCommandString(), tagTypeName);
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
    public static ICrTGas getFirstGas(MCTag _this) {
        return getFirstChemical(_this, CrTTags::getGasTag, CrTGas::new, "GasTag");
    }

    @ZenCodeType.Getter("firstInfuseType")
    public static ICrTInfuseType getFirstInfuseType(MCTag _this) {
        return getFirstChemical(_this, CrTTags::getInfuseTypeTag, CrTInfuseType::new, "InfuseTypeTag");
    }

    @ZenCodeType.Getter("firstPigment")
    public static ICrTPigment getFirstPigment(MCTag _this) {
        return getFirstChemical(_this, CrTTags::getPigmentTag, CrTPigment::new, "PigmentTag");
    }

    @ZenCodeType.Getter("firstSlurry")
    public static ICrTSlurry getFirstSlurry(MCTag _this) {
        return getFirstChemical(_this, CrTTags::getSlurryTag, CrTSlurry::new, "SlurryTag");
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
          CRT_CHEMICAL extends ICrTChemical<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>>
    CRT_CHEMICAL getFirstChemical(MCTag _this, Function<MCTag, ITag<CHEMICAL>> tagGetter, Function<CHEMICAL, CRT_CHEMICAL> chemicalConverter, String tagTypeName) {
        ITag<CHEMICAL> chemicalTag = tagGetter.apply(_this);
        if (chemicalTag == null) {
            throw new IllegalArgumentException("\"" + _this.getCommandString() + "\" is not a " + tagTypeName + "!");
        }
        Optional<CHEMICAL> first = chemicalTag.getAllElements().stream().findFirst();
        if (first.isPresent()) {
            return chemicalConverter.apply(first.get());
        }
        throw new NoSuchElementException("Could not get get first element of \"" + _this.getCommandString() + "\" as it is an empty " + tagTypeName + "!");
    }

    @ZenCodeType.Method
    public static void addGases(MCTag _this, ICrTGas... gases) {
        CraftTweakerAPI.apply(new ActionTagAdd<>(getGasTag(_this), CrTUtils.getChemicals(gases, Gas[]::new), _this.getInternalID()));
    }

    @ZenCodeType.Method
    public static void addInfuseTypes(MCTag _this, ICrTInfuseType... infuseTypes) {
        CraftTweakerAPI.apply(new ActionTagAdd<>(getInfuseTypeTag(_this), CrTUtils.getChemicals(infuseTypes, InfuseType[]::new), _this.getInternalID()));
    }

    @ZenCodeType.Method
    public static void addPigments(MCTag _this, ICrTPigment... pigments) {
        CraftTweakerAPI.apply(new ActionTagAdd<>(getPigmentTag(_this), CrTUtils.getChemicals(pigments, Pigment[]::new), _this.getInternalID()));
    }

    @ZenCodeType.Method
    public static void addSlurries(MCTag _this, ICrTSlurry... slurries) {
        CraftTweakerAPI.apply(new ActionTagAdd<>(getSlurryTag(_this), CrTUtils.getChemicals(slurries, Slurry[]::new), _this.getInternalID()));
    }

    @ZenCodeType.Method
    public static void removeGases(MCTag _this, ICrTGas... gases) {
        CraftTweakerAPI.apply(new ActionTagRemove<>(getGasTag(_this), CrTUtils.getChemicals(gases, Gas[]::new), _this.getInternalID()));
    }

    @ZenCodeType.Method
    public static void removeInfuseTypes(MCTag _this, ICrTInfuseType... infuseTypes) {
        CraftTweakerAPI.apply(new ActionTagRemove<>(getInfuseTypeTag(_this), CrTUtils.getChemicals(infuseTypes, InfuseType[]::new), _this.getInternalID()));
    }

    @ZenCodeType.Method
    public static void removePigments(MCTag _this, ICrTPigment... pigments) {
        CraftTweakerAPI.apply(new ActionTagRemove<>(getPigmentTag(_this), CrTUtils.getChemicals(pigments, Pigment[]::new), _this.getInternalID()));
    }

    @ZenCodeType.Method
    public static void removeSlurries(MCTag _this, ICrTSlurry... slurries) {
        CraftTweakerAPI.apply(new ActionTagRemove<>(getSlurryTag(_this), CrTUtils.getChemicals(slurries, Slurry[]::new), _this.getInternalID()));
    }
}