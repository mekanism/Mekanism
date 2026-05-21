package mekanism.client.recipe_viewer.jei;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.ChemicalResource;
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

        ItemAccess itemAccess = ItemAccess.forStack(stack);
        ResourceHandler<ChemicalResource> chemicalHandler = getChemicalHandler(itemAccess);
        if (chemicalHandler != null) {
            for (int tank = 0, tanks = chemicalHandler.size(); tank < tanks; tank++) {
                ChemicalResource chemicalType = chemicalHandler.getResource(tank);
                //Store the type of the chemical. We skip empty chemicals if there is only a single tank
                if (!chemicalType.isEmpty() || tanks > 1) {
                    subTypeData = tryAddData(subTypeData, chemicalType);
                }
            }
        }

        ResourceHandler<FluidResource> fluidHandler = getFluidHandler(itemAccess);
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

        IStrictEnergyHandler energyHandler = getEnergyHandler(itemAccess);
        if (energyHandler != null) {
            for (int container = 0, containers = energyHandler.size(); container < containers; container++) {
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
    private static ResourceHandler<ChemicalResource> getChemicalHandler(ItemAccess itemAccess) {
        ResourceHandler<ChemicalResource> handler = ContainerType.CHEMICAL.createHandlerIfData(itemAccess);
        if (handler == null) {
            return Capabilities.CHEMICAL.getCapability(itemAccess);
        }
        return handler;
    }

    @Nullable
    private static ResourceHandler<FluidResource> getFluidHandler(ItemAccess itemAccess) {
        ResourceHandler<FluidResource> handler = ContainerType.FLUID.createHandlerIfData(itemAccess);
        if (handler == null) {
            return Capabilities.FLUID.getCapability(itemAccess);
        }
        return handler;
    }

    @Nullable
    private static IStrictEnergyHandler getEnergyHandler(ItemAccess itemAccess) {
        IStrictEnergyHandler handler = ContainerType.ENERGY.createHandlerIfData(itemAccess);
        if (handler == null) {
            return Capabilities.STRICT_ENERGY.getCapability(itemAccess);
        }
        return handler;
    }
}