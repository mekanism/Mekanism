package mekanism.common.item.interfaces;

import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasHandler.IMekanismGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.world.item.ItemStack;

public interface IGasItem {

    @Nonnull
    default GasStack useGas(ItemStack stack, long amount) {
        Optional<IGasHandler> capability = stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).resolve();
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            if (gasHandlerItem instanceof IMekanismGasHandler gasHandler) {
                //TODO: If we end up having more tanks than one in any IGasItem's just kill off this if branch
                IGasTank gasTank = gasHandler.getChemicalTank(0, null);
                if (gasTank != null) {
                    //Should always reach here
                    return gasTank.extract(amount, Action.EXECUTE, AutomationType.MANUAL);
                }
            }
            return gasHandlerItem.extractChemical(amount, Action.EXECUTE);
        }
        return GasStack.EMPTY;
    }

    default boolean hasGas(ItemStack stack) {
        return stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY)
              .map(handler -> {
                  for (int tank = 0, tanks = handler.getTanks(); tank < tanks; tank++) {
                      if (!handler.getChemicalInTank(tank).isEmpty()) {
                          return true;
                      }
                  }
                  return false;
              }).orElse(false);
    }
}