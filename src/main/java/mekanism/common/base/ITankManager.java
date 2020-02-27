package mekanism.common.base;

import mekanism.api.Action;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.inventory.AutomationType;
import mekanism.common.item.ItemGaugeDropper;
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

            ItemGaugeDropper dropper = (ItemGaugeDropper) stack.getItem();

            if (!stack.isEmpty()) {
                GasStack storedGas = dropper.getGas(stack);
                if (tank instanceof BasicGasTank) {
                    BasicGasTank gasTank = (BasicGasTank) tank;
                    int dropperStored = storedGas.getAmount();

                    if (!storedGas.isTypeEqual(gasTank.getStack())) {
                        return;
                    }

                    if (button == 0) { //Insert gas into dropper
                        if (FluidUtil.getFluidContained(stack).isPresent() || gasTank.isEmpty()) {
                            return;
                        }

                        int toInsert = Math.min(gasTank.getStored(), ItemGaugeDropper.CAPACITY - dropperStored);
                        GasStack extracted = gasTank.extract(toInsert, Action.EXECUTE, AutomationType.INTERNAL);
                        if (!extracted.isEmpty()) {
                            dropper.setGas(stack, new GasStack(extracted, dropperStored + extracted.getAmount()));
                        }
                        ((ServerPlayerEntity) player).sendContainerToPlayer(player.openContainer);
                    } else if (button == 1) { //Extract gas from dropper
                        if (FluidUtil.getFluidContained(stack).isPresent() || gasTank.getNeeded() == 0) {
                            return;
                        }
                        dropper.setGas(stack, gasTank.insert(new GasStack(storedGas, dropperStored), Action.EXECUTE, AutomationType.INTERNAL));
                        ((ServerPlayerEntity) player).sendContainerToPlayer(player.openContainer);
                    } else if (button == 2) { //Dump the tank
                        gasTank.setEmpty();
                    }
                } else if (tank instanceof FluidTank) {
                    FluidTank fluidTank = (FluidTank) tank;
                    FluidStack fluidStack = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
                    int dropperStored = fluidStack.getAmount();

                    if (!fluidStack.isEmpty() && !fluidTank.isEmpty() && !fluidStack.isFluidEqual(fluidTank.getFluid())) {
                        return;
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