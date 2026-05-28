package mekanism.client.recipe_viewer.jei;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.attachments.containers.type.ContainerType;
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
        ResourceHandler<ChemicalResource> chemicalHandler = ContainerType.CHEMICAL.getCapOrUnexposed(itemAccess);
        if (chemicalHandler != null) {
            for (int tank = 0, tanks = chemicalHandler.size(); tank < tanks; tank++) {
                ChemicalResource chemicalType = chemicalHandler.getResource(tank);
                //Store the type of the chemical. We skip empty chemicals if there is only a single tank
                if (!chemicalType.isEmpty() || tanks > 1) {
                    subTypeData = tryAddData(subTypeData, chemicalType);
                }
            }
        }

        ResourceHandler<FluidResource> fluidHandler = ContainerType.FLUID.getCapOrUnexposed(itemAccess);
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

        IStrictEnergyHandler energyHandler = ContainerType.ENERGY.getCapOrUnexposed(itemAccess);
        if (energyHandler != null) {
            for (int container = 0, containers = energyHandler.size(); container < containers; container++) {
                //TODO: Should we just be storing the amount of stored energy??
                if (energyHandler.getAmountAsLong(container) >= energyHandler.getCapacityAsLong(container)) {
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
}