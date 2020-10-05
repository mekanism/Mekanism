package mekanism.api;

/**
 * Class for storing constants that are used in various JSON serializers we have, to reduce the chances of typos
 */
public class JsonConstants {

    private JsonConstants() {
    }

    //Ingredients
    public static final String INGREDIENT = "ingredient";
    public static final String AMOUNT = "amount";
    public static final String COUNT = "count";
    public static final String TAG = "tag";
    public static final String NBT = "nbt";
    public static final String ITEM = "item";
    public static final String GAS = "gas";
    public static final String INFUSE_TYPE = "infuse_type";
    public static final String PIGMENT = "pigment";
    public static final String SLURRY = "slurry";
    public static final String FLUID = "fluid";

    //Recipes
    public static final String TYPE = "type";
    public static final String CONDITIONS = "conditions";
    public static final String CHEMICAL_TYPE = "chemicalType";
    public static final String ENERGY_MULTIPLIER = "energyMultiplier";
    public static final String ENERGY_REQUIRED = "energyRequired";
    public static final String DURATION = "duration";
    public static final String INPUT = "input";
    public static final String LEFT_INPUT = "leftInput";
    public static final String RIGHT_INPUT = "rightInput";
    public static final String MAIN_INPUT = "mainInput";
    public static final String EXTRA_INPUT = "extraInput";
    public static final String ITEM_INPUT = "itemInput";
    public static final String INFUSION_INPUT = "infusionInput";
    public static final String GAS_INPUT = "gasInput";
    public static final String SLURRY_INPUT = "slurryInput";
    public static final String FLUID_INPUT = "fluidInput";
    public static final String OUTPUT = "output";
    public static final String LEFT_GAS_OUTPUT = "leftGasOutput";
    public static final String RIGHT_GAS_OUTPUT = "rightGasOutput";
    public static final String ITEM_OUTPUT = "itemOutput";
    public static final String GAS_OUTPUT = "gasOutput";
    public static final String FLUID_OUTPUT = "fluidOutput";
    public static final String MAIN_OUTPUT = "mainOutput";
    public static final String SECONDARY_OUTPUT = "secondaryOutput";
    public static final String SECONDARY_CHANCE = "secondaryChance";

    //Transmitter model
    public static final String GLASS = "glass";
}