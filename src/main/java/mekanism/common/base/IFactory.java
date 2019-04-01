package mekanism.common.base;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.InfuseStorage;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.inputs.DoubleMachineInput;
import mekanism.common.recipe.inputs.InfusionInput;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.recipe.machines.BasicMachineRecipe;
import mekanism.common.recipe.machines.DoubleMachineRecipe;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.util.LangUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

/**
 * Internal interface for managing various Factory types.
 *
 * @author AidanBrady
 */
public interface IFactory {

    /**
     * Gets the recipe type this Smelting Factory currently has.
     *
     * @param itemStack - stack to check
     * @return RecipeType ordinal
     */
    int getRecipeType(ItemStack itemStack);

    /**
     * Sets the recipe type of this Smelting Factory to a new value.
     *
     * @param type - RecipeType ordinal
     * @param itemStack - stack to set
     */
    void setRecipeType(int type, ItemStack itemStack);

    enum MachineFuelType {
        BASIC,
        ADVANCED,
        DOUBLE
    }

    enum RecipeType implements IStringSerializable {
        SMELTING("Smelting", "smelter", MachineType.ENERGIZED_SMELTER, MachineFuelType.BASIC, false,
              Recipe.ENERGIZED_SMELTER),
        ENRICHING("Enriching", "enrichment", MachineType.ENRICHMENT_CHAMBER, MachineFuelType.BASIC, false,
              Recipe.ENRICHMENT_CHAMBER),
        CRUSHING("Crushing", "crusher", MachineType.CRUSHER, MachineFuelType.BASIC, false, Recipe.CRUSHER),
        COMPRESSING("Compressing", "compressor", MachineType.OSMIUM_COMPRESSOR, MachineFuelType.ADVANCED, false,
              Recipe.OSMIUM_COMPRESSOR),
        COMBINING("Combining", "combiner", MachineType.COMBINER, MachineFuelType.DOUBLE, false, Recipe.COMBINER),
        PURIFYING("Purifying", "purifier", MachineType.PURIFICATION_CHAMBER, MachineFuelType.ADVANCED, true,
              Recipe.PURIFICATION_CHAMBER),
        INJECTING("Injecting", "injection", MachineType.CHEMICAL_INJECTION_CHAMBER, MachineFuelType.ADVANCED, true,
              Recipe.CHEMICAL_INJECTION_CHAMBER),
        INFUSING("Infusing", "metalinfuser", MachineType.METALLURGIC_INFUSER, MachineFuelType.BASIC, false,
              Recipe.METALLURGIC_INFUSER);

        private String name;
        private SoundEvent sound;
        private MachineType type;
        private MachineFuelType fuelType;
        private boolean fuelSpeed;
        private Recipe recipe;
        private TileEntityAdvancedElectricMachine cacheTile;

        RecipeType(String s, String s1, MachineType t, MachineFuelType ft, boolean b1, Recipe r) {
            name = s;
            sound = new SoundEvent(new ResourceLocation("mekanism", "tile.machine." + s1));
            type = t;
            fuelType = ft;
            fuelSpeed = b1;
            recipe = r;
        }

        public static RecipeType getFromMachine(Block block, int meta) {
            RecipeType type = null;

            for (RecipeType iterType : RecipeType.values()) {
                ItemStack machineStack = iterType.getStack();

                if (Block.getBlockFromItem(machineStack.getItem()) == block && machineStack.getItemDamage() == meta) {
                    type = iterType;
                    break;
                }
            }

            return type;
        }

        public BasicMachineRecipe getRecipe(ItemStackInput input) {
            return RecipeHandler.getRecipe(input, recipe.get());
        }

        public BasicMachineRecipe getRecipe(ItemStack input) {
            return getRecipe(new ItemStackInput(input));
        }

        public AdvancedMachineRecipe getRecipe(AdvancedMachineInput input) {
            return RecipeHandler.getRecipe(input, recipe.get());
        }

        public AdvancedMachineRecipe getRecipe(ItemStack input, Gas gas) {
            return getRecipe(new AdvancedMachineInput(input, gas));
        }

        public DoubleMachineRecipe getRecipe(DoubleMachineInput input) {
            return RecipeHandler.getRecipe(input, recipe.get());
        }

        public DoubleMachineRecipe getRecipe(ItemStack input, ItemStack extra) {
            return getRecipe(new DoubleMachineInput(input, extra));
        }

        public MetallurgicInfuserRecipe getRecipe(InfusionInput input) {
            return RecipeHandler.getMetallurgicInfuserRecipe(input);
        }

        public MetallurgicInfuserRecipe getRecipe(ItemStack input, InfuseStorage storage) {
            return getRecipe(new InfusionInput(storage, input));
        }

        public MachineRecipe getAnyRecipe(ItemStack slotStack, ItemStack extraStack, Gas gasType,
              InfuseStorage infuse) {
            if (fuelType == MachineFuelType.ADVANCED) {
                return getRecipe(slotStack, gasType);
            } else if (fuelType == MachineFuelType.DOUBLE) {
                return getRecipe(slotStack, extraStack);
            } else if (this == INFUSING) {
                if (infuse.type != null) {
                    return RecipeHandler.getMetallurgicInfuserRecipe(new InfusionInput(infuse, slotStack));
                } else {
                    for (Entry<InfusionInput, MetallurgicInfuserRecipe> entry : Recipe.METALLURGIC_INFUSER.get()
                          .entrySet()) {
                        if (entry.getKey().inputStack.isItemEqual(slotStack)) {
                            return entry.getValue();
                        }
                    }
                }
            }

            return getRecipe(slotStack);
        }

        public GasStack getItemGas(ItemStack itemstack) {
            if (fuelType == MachineFuelType.ADVANCED) {
                return getTile().getItemGas(itemstack);
            }

            return null;
        }

        public int getSecondaryEnergyPerTick() {
            if (fuelType == MachineFuelType.ADVANCED) {
                return getTile().BASE_SECONDARY_ENERGY_PER_TICK;
            }

            return 0;
        }

        public boolean canReceiveGas(EnumFacing side, Gas type) {
            if (fuelType == MachineFuelType.ADVANCED) {
                return getTile().canReceiveGas(side, type);
            }

            return false;
        }

        public boolean supportsGas() {
            return fuelType == MachineFuelType.ADVANCED;
        }

        public boolean isValidGas(Gas gas) {
            if (fuelType == MachineFuelType.ADVANCED) {
                return getTile().isValidGas(gas);
            }

            return false;
        }

        public boolean hasRecipe(ItemStack itemStack) {
            if (itemStack.isEmpty()) {
                return false;
            }

            for (Object obj : recipe.get().entrySet()) {
                if (((Map.Entry) obj).getKey() instanceof AdvancedMachineInput) {
                    Map.Entry entry = (Map.Entry) obj;
                    ItemStack stack = ((AdvancedMachineInput) entry.getKey()).itemStack;

                    if (StackUtils.equalsWildcard(stack, itemStack)) {
                        return true;
                    }
                }
            }

            return false;
        }

        public boolean hasRecipeForExtra(ItemStack extraStack) {
            if (extraStack.isEmpty()) {
                return false;
            }

            for (Object obj : recipe.get().entrySet()) {
                if (((Map.Entry) obj).getKey() instanceof DoubleMachineInput) {
                    Map.Entry entry = (Map.Entry) obj;
                    ItemStack stack = ((DoubleMachineInput) entry.getKey()).extraStack;

                    if (StackUtils.equalsWildcard(stack, extraStack)) {
                        return true;
                    }
                }
            }

            return false;
        }

        public TileEntityAdvancedElectricMachine getTile() {
            if (cacheTile == null) {
                MachineType type = MachineType.get(getStack());
                cacheTile = (TileEntityAdvancedElectricMachine) type.create();
            }

            return cacheTile;
        }

        public double getEnergyUsage() {
            return type.getUsage();
        }

        public int getMaxSecondaryEnergy() {
            return 200;
        }

        public ItemStack getStack() {
            return type.getStack();
        }

        public String getTranslationKey() {
            return name;
        }

        public String getLocalizedName() {
            return LangUtils.localize("gui.factory." + name);
        }

        public SoundEvent getSound() {
            return sound;
        }

        public MachineFuelType getFuelType() {
            return fuelType;
        }

        public boolean fuelEnergyUpgrades() {
            return fuelSpeed;
        }

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public MachineType getType() {
            return type;
        }
    }
}
