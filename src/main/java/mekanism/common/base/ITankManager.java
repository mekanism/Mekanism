package mekanism.common.base;

import java.util.Optional;
import mekanism.api.Action;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

//TODO: Re-evaluate
public interface ITankManager {

    Object[] getTanks();

    class DropperHandler {

        public static void useDropper(PlayerEntity player, Object tank, int button) {
            ItemStack stack = player.inventory.getItemStack();

            if (stack.isEmpty() || !(stack.getItem() instanceof ItemGaugeDropper)) {
                return;
            }
            if (!stack.isEmpty()) {
                if (tank instanceof IChemicalTank) {
                    IChemicalTank<?, ?> chemicalTank = (IChemicalTank<?, ?>) tank;
                    boolean hasFluid = FluidUtil.getFluidContained(stack).isPresent();
                    if (chemicalTank.getEmptyStack() == GasStack.EMPTY) {
                        //It is actually a gas tank
                        IChemicalTank<Gas, GasStack> gasTank = (IChemicalTank<Gas, GasStack>) chemicalTank;
                        Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
                        if (capability.isPresent()) {
                            IGasHandler gasHandlerItem = capability.get();
                            if (gasHandlerItem.getGasTankCount() > 0) {
                                //Validate something didn't go terribly wrong and we actually do have the tank we expect to have
                                GasStack storedGas = gasHandlerItem.getGasInTank(0);
                                if (!storedGas.isTypeEqual(gasTank.getStack())) {
                                    return;
                                }
                                if (button == 0) { //Insert gas into dropper
                                    if (hasFluid || gasTank.isEmpty()) {
                                        return;
                                    }
                                    GasStack gasInTank = gasTank.getStack();
                                    GasStack simulatedRemainder = gasHandlerItem.insertGas(gasInTank, Action.SIMULATE);
                                    int remainder = simulatedRemainder.getAmount();
                                    int amount = gasInTank.getAmount();
                                    if (remainder < amount) {
                                        //We are able to fit at least some of the gas from our tank into the item
                                        GasStack extractedGas = gasTank.extract(amount - remainder, Action.EXECUTE, AutomationType.INTERNAL);
                                        if (!extractedGas.isEmpty()) {
                                            //If we were able to actually extract it from our tank, then insert it into the item
                                            if (!gasHandlerItem.insertGas(extractedGas, Action.EXECUTE).isEmpty()) {
                                                //TODO: Print warning/error
                                            }
                                            ((ServerPlayerEntity) player).sendContainerToPlayer(player.openContainer);
                                        }
                                    }
                                } else if (button == 1) { //Extract gas from dropper
                                    if (hasFluid || gasTank.getNeeded() == 0) {
                                        //If the dropper has fluid or the tank interacting with is already full of gas
                                        return;
                                    }
                                    GasStack simulatedRemainder = gasTank.insert(storedGas, Action.SIMULATE, AutomationType.INTERNAL);
                                    int gasInItemAmount = storedGas.getAmount();
                                    int remainder = simulatedRemainder.getAmount();
                                    if (remainder < gasInItemAmount) {
                                        GasStack extractedGas = gasHandlerItem.extractGas(0, gasInItemAmount - remainder, Action.EXECUTE);
                                        if (!extractedGas.isEmpty()) {
                                            //If we were able to actually extract it from the item, then insert it into our gas tank
                                            if (!gasTank.insert(extractedGas, Action.EXECUTE, AutomationType.INTERNAL).isEmpty()) {
                                                //TODO: Print warning/error
                                            }
                                            ((ServerPlayerEntity) player).sendContainerToPlayer(player.openContainer);
                                        }
                                    }
                                } else if (button == 2) { //Dump the tank
                                    gasTank.setEmpty();
                                }
                            }
                        } else if (chemicalTank.getEmptyStack() == InfusionStack.EMPTY) {
                            //It is actually an infusion tank
                            IChemicalTank<InfuseType, InfusionStack> infusionTank = (IChemicalTank<InfuseType, InfusionStack>) chemicalTank;
                            //TODO: Implement at some point
                        }
                    }
                    //TODO: Handle other chemical tanks like maybe infusion tanks
                } else if (tank instanceof FluidTank) {
                    FluidTank fluidTank = (FluidTank) tank;
                    FluidStack fluidStack = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
                    int dropperStored = fluidStack.getAmount();

                    if (!fluidStack.isEmpty() && !fluidTank.isEmpty() && !fluidStack.isFluidEqual(fluidTank.getFluid())) {
                        return;
                    }

                    GasStack storedGas = GasStack.EMPTY;
                    Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
                    if (capability.isPresent()) {
                        IGasHandler gasHandlerItem = capability.get();
                        if (gasHandlerItem.getGasTankCount() > 0) {
                            storedGas = gasHandlerItem.getGasInTank(0);
                        }
                    }

                    if (button == 0) { //Insert fluid into dropper
                        if (!storedGas.isEmpty() || fluidTank.isEmpty()) {
                            return;
                        }

                        int toInsert = Math.min(fluidTank.getFluidAmount(), ItemGaugeDropper.CAPACITY - dropperStored);
                        FluidUtil.getFluidHandler(stack).ifPresent(handler -> handler.fill(fluidTank.drain(toInsert, FluidAction.EXECUTE), FluidAction.EXECUTE));

                        ((ServerPlayerEntity) player).sendContainerToPlayer(player.openContainer);
                    } else if (button == 1) { //Extract fluid from dropper
                        if (!storedGas.isEmpty() || fluidTank.getSpace() == 0) {
                            return;
                        }

                        int toExtract = Math.min(fluidTank.getSpace(), dropperStored);
                        FluidUtil.getFluidHandler(stack).ifPresent(handler -> fluidTank.fill(handler.drain(toExtract, FluidAction.EXECUTE), FluidAction.EXECUTE));

                        ((ServerPlayerEntity) player).sendContainerToPlayer(player.openContainer);
                    } else if (button == 2) { //Dump the tank
                        fluidTank.setFluid(FluidStack.EMPTY);
                    }
                }
            }
        }
    }
}