package mekanism.common.integration.crafttweaker.jeitweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.actions.IUndoableAction;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.util.text.MCTextComponent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import mekanism.common.integration.crafttweaker.content.builder.CrTChemicalBuilder;
import mekanism.common.integration.crafttweaker.content.builder.CrTGasBuilder;
import mekanism.common.integration.crafttweaker.content.builder.CrTInfuseTypeBuilder;
import mekanism.common.integration.crafttweaker.content.builder.CrTPigmentBuilder;
import mekanism.common.integration.crafttweaker.content.builder.CrTSlurryBuilder;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.LogicalSide;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister(modDeps = MekanismHooks.JEITWEAKER_MOD_ID)
@ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_JEITWEAKER)
public class JEITweakerExpansion {

    static final Set<GasStack> HIDDEN_GASES = new HashSet<>();
    static final Set<InfusionStack> HIDDEN_INFUSE_TYPES = new HashSet<>();
    static final Set<PigmentStack> HIDDEN_PIGMENTS = new HashSet<>();
    static final Set<SlurryStack> HIDDEN_SLURRIES = new HashSet<>();
    static final Map<GasStack, ITextComponent[]> GAS_DESCRIPTIONS = new HashMap<>();
    static final Map<InfusionStack, ITextComponent[]> INFUSE_TYPE_DESCRIPTIONS = new HashMap<>();
    static final Map<PigmentStack, ITextComponent[]> PIGMENT_DESCRIPTIONS = new HashMap<>();
    static final Map<SlurryStack, ITextComponent[]> SLURRY_DESCRIPTIONS = new HashMap<>();

    /**
     * Hides a gas stack ingredient from showing up in JEI.
     *
     * @param stack Gas stack to hide in JEI.
     *
     * @apiNote If the gas stack is one being added via {@link CrTGasBuilder} consider using {@link CrTChemicalBuilder#hidden()} instead.
     */
    @ZenCodeType.StaticExpansionMethod
    public static void hideGas(ICrTGasStack stack) {
        CraftTweakerAPI.apply(new ChemicalHiderAction<>(HIDDEN_GASES, stack));
    }

    /**
     * Hides an infusion stack ingredient from showing up in JEI.
     *
     * @param stack Infusion stack to hide in JEI.
     *
     * @apiNote If the infusion stack is one being added via {@link CrTInfuseTypeBuilder} consider using {@link CrTChemicalBuilder#hidden()} instead.
     */
    @ZenCodeType.StaticExpansionMethod
    public static void hideInfuseType(ICrTInfusionStack stack) {
        CraftTweakerAPI.apply(new ChemicalHiderAction<>(HIDDEN_INFUSE_TYPES, stack));
    }

    /**
     * Hides a pigment stack Ingredient from showing up in JEI.
     *
     * @param stack Pigment stack to hide in JEI.
     *
     * @apiNote If the pigment stack is one being added via {@link CrTPigmentBuilder} consider using {@link CrTChemicalBuilder#hidden()} instead.
     */
    @ZenCodeType.StaticExpansionMethod
    public static void hidePigment(ICrTPigmentStack stack) {
        CraftTweakerAPI.apply(new ChemicalHiderAction<>(HIDDEN_PIGMENTS, stack));
    }

    /**
     * Hides a slurry stack ingredient from showing up in JEI.
     *
     * @param stack Slurry stack to hide in JEI.
     *
     * @apiNote If the slurry stack is one being added via {@link CrTSlurryBuilder} consider using {@link CrTChemicalBuilder#hidden()} instead.
     */
    @ZenCodeType.StaticExpansionMethod
    public static void hideSlurry(ICrTSlurryStack stack) {
        CraftTweakerAPI.apply(new ChemicalHiderAction<>(HIDDEN_SLURRIES, stack));
    }

    /**
     * Adds a description to the given gas stack when looked at in JEI. This description will show up in JEI's "Information" category.
     *
     * @param stack        Gas stack to add a description to.
     * @param descriptions Text components representing the description to add.
     */
    @ZenCodeType.StaticExpansionMethod
    public static void addInfo(ICrTGasStack stack, MCTextComponent... descriptions) {
        CraftTweakerAPI.apply(new ChemicalDescriptionAction<>(GAS_DESCRIPTIONS, stack, descriptions));
    }

    /**
     * Adds a description to the given infusion stack when looked at in JEI. This description will show up in JEI's "Information" category.
     *
     * @param stack        Infusion stack to add a description to.
     * @param descriptions Text components representing the description to add.
     */
    @ZenCodeType.StaticExpansionMethod
    public static void addInfo(ICrTInfusionStack stack, MCTextComponent... descriptions) {
        CraftTweakerAPI.apply(new ChemicalDescriptionAction<>(INFUSE_TYPE_DESCRIPTIONS, stack, descriptions));
    }

    /**
     * Adds a description to the given pigment stack when looked at in JEI. This description will show up in JEI's "Information" category.
     *
     * @param stack        Pigment stack to add a description to.
     * @param descriptions Text components representing the description to add.
     */
    @ZenCodeType.StaticExpansionMethod
    public static void addInfo(ICrTPigmentStack stack, MCTextComponent... descriptions) {
        CraftTweakerAPI.apply(new ChemicalDescriptionAction<>(PIGMENT_DESCRIPTIONS, stack, descriptions));
    }

    /**
     * Adds a description to the given slurry stack when looked at in JEI. This description will show up in JEI's "Information" category.
     *
     * @param stack        Slurry stack to add a description to.
     * @param descriptions Text components representing the description to add.
     */
    @ZenCodeType.StaticExpansionMethod
    public static void addInfo(ICrTSlurryStack stack, MCTextComponent... descriptions) {
        CraftTweakerAPI.apply(new ChemicalDescriptionAction<>(SLURRY_DESCRIPTIONS, stack, descriptions));
    }

    private static class ChemicalDescriptionAction<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
          CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, ?, CRT_STACK>> implements IUndoableAction {

        private final Map<STACK, ITextComponent[]> descriptionMap;
        private final ITextComponent[] descriptions;
        private final CRT_STACK stack;

        private ChemicalDescriptionAction(Map<STACK, ITextComponent[]> descriptionMap, CRT_STACK stack, MCTextComponent... descriptions) {
            this.descriptionMap = descriptionMap;
            this.stack = stack.asImmutable();
            this.descriptions = new ITextComponent[descriptions.length];
            for (int i = 0; i < descriptions.length; i++) {
                //TODO - 10.1: Update min CrT version to ensure this is properly immutable https://github.com/CraftTweaker/CraftTweaker/issues/1236
                this.descriptions[i] = descriptions[i].getInternal();
            }
        }

        @Override
        public void apply() {
            descriptionMap.put(stack.getInternal(), descriptions);
        }

        @Override
        public void undo() {
            descriptionMap.remove(stack.getInternal());
        }

        @Override
        public String describeUndo() {
            return "Undoing adding JEI Info for: " + stack.getCommandString();
        }

        @Override
        public String describe() {
            return "Adding JEI Info for: " + stack.getCommandString();
        }

        @Override
        public boolean shouldApplyOn(LogicalSide side) {
            return !CraftTweakerAPI.isServer();
        }
    }

    private static class ChemicalHiderAction<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
          CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, ?, CRT_STACK>> implements IUndoableAction {

        private final Set<STACK> hidden;
        private final CRT_STACK stack;

        private ChemicalHiderAction(Set<STACK> hidden, CRT_STACK stack) {
            this.hidden = hidden;
            this.stack = stack.asImmutable();
        }

        @Override
        public void apply() {
            hidden.add(stack.getInternal());
        }

        @Override
        public void undo() {
            hidden.remove(stack.getInternal());
        }

        @Override
        public String describeUndo() {
            return "Undoing JEI Hiding Chemical: " + stack.getCommandString();
        }

        @Override
        public String describe() {
            return "JEI Hiding Chemical: " + stack.getCommandString();
        }

        @Override
        public boolean shouldApplyOn(LogicalSide side) {
            return !CraftTweakerAPI.isServer();
        }
    }
}