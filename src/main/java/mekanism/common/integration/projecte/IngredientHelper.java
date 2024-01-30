package mekanism.common.integration.projecte;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

//TODO: Document this class
public class IngredientHelper {

    private final IMappingCollector<NormalizedSimpleStack, Long> mapper;
    private Map<NormalizedSimpleStack, Integer> ingredientMap = new HashMap<>();
    /**
     * Gets set to false if we have a recipe that is more than we can handle
     */
    private boolean isValid = true;

    public IngredientHelper(IMappingCollector<NormalizedSimpleStack, Long> mapper) {
        this.mapper = mapper;
    }

    public void resetHelper() {
        isValid = true;
        ingredientMap = new HashMap<>();
    }

    public void put(NormalizedSimpleStack stack, int amount) {
        if (isValid) {
            if (ingredientMap.containsKey(stack)) {
                long newAmount = ingredientMap.get(stack) + (long) amount;
                if (newAmount > Integer.MAX_VALUE || newAmount < Integer.MIN_VALUE) {
                    isValid = false;
                } else {
                    ingredientMap.put(stack, (int) newAmount);
                }
            } else {
                ingredientMap.put(stack, amount);
            }
        }
    }

    public void put(NormalizedSimpleStack stack, long amount) {
        if (amount > Integer.MAX_VALUE || amount < Integer.MIN_VALUE) {
            isValid = false;
        } else {
            put(stack, (int) amount);
        }
    }

    public void put(ChemicalStack<?> stack) {
        put(convertToNSS(stack), stack.getAmount());
    }

    public void put(FluidStack stack) {
        put(NSSFluid.createFluid(stack), stack.getAmount());
    }

    public void put(ItemStack stack) {
        put(NSSItem.createItem(stack), stack.getCount());
    }

    public boolean addAsConversion(NormalizedSimpleStack output, int outputAmount) {
        if (isValid) {
            mapper.addConversion(outputAmount, output, ingredientMap);
            return true;
        }
        return false;
    }

    public boolean addAsConversion(NormalizedSimpleStack output, long outputAmount) {
        if (outputAmount > Integer.MAX_VALUE) {
            return false;
        }
        return addAsConversion(output, (int) outputAmount);
    }

    public boolean addAsConversion(ChemicalStack<?> stack) {
        return addAsConversion(convertToNSS(stack), stack.getAmount());
    }

    public boolean addAsConversion(FluidStack stack) {
        return addAsConversion(NSSFluid.createFluid(stack), stack.getAmount());
    }

    public boolean addAsConversion(ItemStack stack) {
        return addAsConversion(NSSItem.createItem(stack), stack.getCount());
    }

    private NormalizedSimpleStack convertToNSS(ChemicalStack<?> chemicalStack) {
        if (chemicalStack instanceof GasStack stack) {
            return NSSGas.createGas(stack);
        } else if (chemicalStack instanceof InfusionStack stack) {
            return NSSInfuseType.createInfuseType(stack);
        } else if (chemicalStack instanceof PigmentStack stack) {
            return NSSPigment.createPigment(stack);
        } else if (chemicalStack instanceof SlurryStack stack) {
            return NSSSlurry.createSlurry(stack);
        }
        throw new IllegalStateException("Unknown Chemical Type");
    }
}