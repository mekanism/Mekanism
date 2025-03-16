package mekanism.client.recipe_viewer.jei;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class MekanismSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {

    private List<Object> tryAddData(@Nullable List<Object> subTypeData, Object data) {
        if (subTypeData == null) {
            subTypeData = new ArrayList<>();
        }
        subTypeData.add(data);
        return subTypeData;
    }

    @Nullable
    @Override
    public Object getSubtypeData(ItemStack stack, UidContext context) {
        if (context != UidContext.Ingredient) {
            return null;
        }
        List<Object> subTypeData = null;

        IChemicalHandler chemicalHandler = getChemicalHandler(stack);
        if (chemicalHandler != null) {
            for (int tank = 0, tanks = chemicalHandler.getChemicalTanks(); tank < tanks; tank++) {
                ChemicalStack chemicalStack = chemicalHandler.getChemicalInTank(tank);
                //Store the type of the chemical. We skip empty chemicals if there is only a single tank
                if (!chemicalStack.isEmpty() || tanks > 1) {
                    subTypeData = tryAddData(subTypeData, chemicalStack.getChemical());
                }
            }
        }

        IFluidHandler fluidHandler = getFluidHandler(stack);
        if (fluidHandler != null) {
            for (int tank = 0, tanks = fluidHandler.getTanks(); tank < tanks; tank++) {
                FluidStack fluidStack = fluidHandler.getFluidInTank(tank);
                //Store the type of the fluid. We skip empty fluids if there is only a single tank
                if (!fluidStack.isEmpty() || tanks > 1) {
                    //TODO: Should this be using the fluidstack's subtype interpretation? (So that it takes fluid components into account?
                    subTypeData = tryAddData(subTypeData, fluidStack.getFluid());
                }
            }
        }

        IStrictEnergyHandler energyHandler = getEnergyHandler(stack);
        if (energyHandler != null) {
            for (int container = 0, containers = energyHandler.getEnergyContainerCount(); container < containers; container++) {
                //TODO: Should we just be storing the amount of stored energy??
                long neededEnergy = energyHandler.getNeededEnergy(container);
                if (neededEnergy == 0L) {
                    //Energy container is full
                    subTypeData = tryAddData(subTypeData, true);
                } else if (containers > 1) {
                    //Energy container is not full
                    subTypeData = tryAddData(subTypeData, false);
                }
            }
        }
        return subTypeData;
    }

    @Override
    @Deprecated
    public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
        if (context == UidContext.Ingredient) {
            String representation = getChemicalComponent(stack);
            representation = addInterpretation(representation, getFluidComponent(stack));
            representation = addInterpretation(representation, getEnergyComponent(stack));
            return representation;
        }
        return "";
    }

    @Deprecated
    private static String addInterpretation(String nbtRepresentation, String component) {
        return nbtRepresentation.isEmpty() ? component : nbtRepresentation + ":" + component;
    }

    @Deprecated
    private static String getChemicalComponent(ItemStack stack) {
        IChemicalHandler chemicalHandler = getChemicalHandler(stack);
        if (chemicalHandler != null) {
            String component = "";
            for (int tank = 0, tanks = chemicalHandler.getChemicalTanks(); tank < tanks; tank++) {
                ChemicalStack chemicalStack = chemicalHandler.getChemicalInTank(tank);
                if (!chemicalStack.isEmpty()) {
                    component = addInterpretation(component, chemicalStack.getChemical().toString());
                } else if (tanks > 1) {
                    component = addInterpretation(component, "empty");
                }
            }
            return component;
        }
        return "";
    }

    @Deprecated
    private static String getFluidComponent(ItemStack stack) {
        IFluidHandler handler = getFluidHandler(stack);
        if (handler != null) {
            String component = "";
            for (int tank = 0, tanks = handler.getTanks(); tank < tanks; tank++) {
                FluidStack fluidStack = handler.getFluidInTank(tank);
                if (!fluidStack.isEmpty()) {
                    component = addInterpretation(component, fluidStack.getFluid().toString());
                } else if (tanks > 1) {
                    component = addInterpretation(component, "empty");
                }
            }
            return component;
        }
        return "";
    }

    @Deprecated
    private static String getEnergyComponent(ItemStack stack) {
        IStrictEnergyHandler energyHandlerItem = getEnergyHandler(stack);
        if (energyHandlerItem != null) {
            String component = "";
            int containers = energyHandlerItem.getEnergyContainerCount();
            for (int container = 0; container < containers; container++) {
                long neededEnergy = energyHandlerItem.getNeededEnergy(container);
                if (neededEnergy == 0L) {
                    component = addInterpretation(component, "filled");
                } else if (containers > 1) {
                    component = addInterpretation(component, "empty");
                }
            }
            return component;
        }
        return "";
    }

    @Nullable
    private static IChemicalHandler getChemicalHandler(ItemStack stack) {
        IChemicalHandler handler = ContainerType.CHEMICAL.createHandlerIfData(stack);
        if (handler == null) {
            return Capabilities.CHEMICAL.getCapability(stack);
        }
        return handler;
    }

    @Nullable
    private static IFluidHandler getFluidHandler(ItemStack stack) {
        IFluidHandler handler = ContainerType.FLUID.createHandlerIfData(stack);
        if (handler == null) {
            return Capabilities.FLUID.getCapability(stack);
        }
        return handler;
    }

    @Nullable
    private static IStrictEnergyHandler getEnergyHandler(ItemStack stack) {
        IStrictEnergyHandler handler = ContainerType.ENERGY.createHandlerIfData(stack);
        if (handler == null) {
            return Capabilities.STRICT_ENERGY.getCapability(stack);
        }
        return handler;
    }
}