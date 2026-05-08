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
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
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

        ResourceHandler<FluidResource> fluidHandler = getFluidHandler(stack);
        if (fluidHandler != null) {
            for (int tank = 0, tanks = fluidHandler.size(); tank < tanks; tank++) {
                FluidResource fluidType = fluidHandler.getResource(tank);
                //Store the type of the fluid. We skip empty fluids if there is only a single tank
                if (!fluidType.isEmpty() || tanks > 1) {
                    //TODO - 26.1: Should this be using the fluidstack's subtype interpretation? (So that it takes fluid components into account?
                    subTypeData = tryAddData(subTypeData, fluidType.getFluid());
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

    @Nullable
    private static IChemicalHandler getChemicalHandler(ItemStack stack) {
        IChemicalHandler handler = ContainerType.CHEMICAL.createHandlerIfData(stack);
        if (handler == null) {
            return Capabilities.CHEMICAL_LEGACY.getCapability(ItemAccess.forStack(stack));
        }
        return handler;
    }

    @Nullable
    private static ResourceHandler<FluidResource> getFluidHandler(ItemStack stack) {
        ResourceHandler<FluidResource> handler = ContainerType.FLUID.createHandlerIfData(stack);
        if (handler == null) {
            return Capabilities.FLUID.getCapability(ItemAccess.forStack(stack));
        }
        return handler;
    }

    @Nullable
    private static IStrictEnergyHandler getEnergyHandler(ItemStack stack) {
        IStrictEnergyHandler handler = ContainerType.ENERGY.createHandlerIfData(stack);
        if (handler == null) {
            return Capabilities.STRICT_ENERGY.getCapability(ItemAccess.forStack(stack));
        }
        return handler;
    }
}