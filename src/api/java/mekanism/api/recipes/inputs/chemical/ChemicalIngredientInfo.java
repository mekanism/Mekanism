package mekanism.api.recipes.inputs.chemical;

import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.ChemicalUtils.ChemicalToStackCreator;
import mekanism.api.chemical.ChemicalUtils.StackToStackCreator;
import mekanism.api.chemical.IEmptyStackProvider;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

/**
 * Helper class for providing information to the various chemical ingredients
 */
@SuppressWarnings("Convert2Diamond")
//The types cannot properly be inferred
class ChemicalIngredientInfo<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements IEmptyStackProvider<CHEMICAL, STACK> {

    public static final ChemicalIngredientInfo<Gas, GasStack> GAS = new ChemicalIngredientInfo<Gas, GasStack>(GasStack.EMPTY, GasStack::new, GasStack::new, JsonConstants.GAS, ChemicalTags.GAS);
    public static final ChemicalIngredientInfo<InfuseType, InfusionStack> INFUSION = new ChemicalIngredientInfo<InfuseType, InfusionStack>(InfusionStack.EMPTY, InfusionStack::new, InfusionStack::new, JsonConstants.INFUSE_TYPE, ChemicalTags.INFUSE_TYPE);
    public static final ChemicalIngredientInfo<Pigment, PigmentStack> PIGMENT = new ChemicalIngredientInfo<Pigment, PigmentStack>(PigmentStack.EMPTY, PigmentStack::new, PigmentStack::new, JsonConstants.PIGMENT, ChemicalTags.PIGMENT);
    public static final ChemicalIngredientInfo<Slurry, SlurryStack> SLURRY = new ChemicalIngredientInfo<Slurry, SlurryStack>(SlurryStack.EMPTY, SlurryStack::new, SlurryStack::new, JsonConstants.SLURRY, ChemicalTags.SLURRY);

    private final ChemicalToStackCreator<CHEMICAL, STACK> chemicalToStackCreator;
    private final StackToStackCreator<STACK> stackToStackCreator;
    private final ChemicalTags<CHEMICAL> tags;
    private final String serializationKey;
    private final STACK emptyStack;

    private ChemicalIngredientInfo(STACK emptyStack, ChemicalToStackCreator<CHEMICAL, STACK> chemicalToStackCreator, StackToStackCreator<STACK> stackToStackCreator,
          String serializationKey, ChemicalTags<CHEMICAL> tags) {
        this.chemicalToStackCreator = chemicalToStackCreator;
        this.stackToStackCreator = stackToStackCreator;
        this.serializationKey = serializationKey;
        this.emptyStack = emptyStack;
        this.tags = tags;
    }

    public String getSerializationKey() {
        return serializationKey;
    }

    @Nonnull
    @Override
    public STACK getEmptyStack() {
        return emptyStack;
    }

    public STACK createStack(CHEMICAL chemical, long amount) {
        return chemicalToStackCreator.createStack(chemical, amount);
    }

    public STACK createStack(STACK stack, long amount) {
        return stackToStackCreator.createStack(stack, amount);
    }

    public ResourceLocation getTagLocation(ITag<CHEMICAL> tag) {
        return tags.lookupTag(tag);
    }
}