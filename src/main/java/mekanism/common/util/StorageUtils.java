package mekanism.common.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class StorageUtils {

    public static void addStoredEnergy(@Nonnull ItemStack stack, @Nonnull List<ITextComponent> tooltip, boolean showMissing) {
        //TODO: Do something like this for the gas capability? The big issue is some of the tooltips are different for that
        if (Capabilities.STRICT_ENERGY_CAPABILITY != null) {
            //Ensure the capability is not null, as the first call to addInformation happens before capability injection
            Optional<IStrictEnergyHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.STRICT_ENERGY_CAPABILITY));
            if (capability.isPresent()) {
                IStrictEnergyHandler energyHandlerItem = capability.get();
                int energyContainerCount = energyHandlerItem.getEnergyContainerCount();
                for (int container = 0; container < energyContainerCount; container++) {
                    //TODO: Do we want a way of having it specify which tank?
                    tooltip.add(MekanismLang.STORED_ENERGY.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY,
                          EnergyDisplay.of(energyHandlerItem.getEnergy(container), energyHandlerItem.getMaxEnergy(container))));
                }
            } else if (showMissing) {
                tooltip.add(MekanismLang.STORED_ENERGY.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY, EnergyDisplay.ZERO));
            }
        }
    }

    /**
     * Gets the fluid if one is stored from an item's tank going off the basis there is a single tank. This is for cases when we may not actually have a fluid handler
     * attached to our item but it may have stored data in its tank from when it was a block
     */
    @Nonnull
    public static FluidStack getStoredFluidFromNBT(ItemStack stack) {
        BasicFluidTank tank = BasicFluidTank.create(Integer.MAX_VALUE, null);
        DataHandlerUtils.readTanks(Collections.singletonList(tank), ItemDataUtils.getList(stack, NBTConstants.FLUID_TANKS));
        return tank.getFluid();
    }

    /**
     * Gets the energy if one is stored from an item's container going off the basis there is a single energy container. This is for cases when we may not actually have
     * an energy handler attached to our item but it may have stored data in its container from when it was a block
     */
    public static FloatingLong getStoredEnergyFromNBT(ItemStack stack) {
        BasicEnergyContainer container = BasicEnergyContainer.create(FloatingLong.MAX_VALUE, null);
        DataHandlerUtils.readContainers(Collections.singletonList(container), ItemDataUtils.getList(stack, NBTConstants.ENERGY_CONTAINERS));
        return container.getEnergy();
    }

    public static ItemStack getFilledEnergyVariant(ItemStack toFill, FloatingLong capacity) {
        //Manually handle this as capabilities are not necessarily loaded yet (at least not on the first call to this, which is made via fillItemGroup)
        BasicEnergyContainer container = BasicEnergyContainer.create(capacity, null);
        container.setEnergy(capacity);
        ItemDataUtils.setList(toFill, NBTConstants.ENERGY_CONTAINERS, DataHandlerUtils.writeContainers(Collections.singletonList(container)));
        //The item is now filled return it for convenience
        return toFill;
    }

    @Nullable
    public static IEnergyContainer getEnergyContainer(ItemStack stack, int container) {
        Optional<IStrictEnergyHandler> energyCapability = MekanismUtils.toOptional(stack.getCapability(Capabilities.STRICT_ENERGY_CAPABILITY));
        if (energyCapability.isPresent()) {
            IStrictEnergyHandler energyHandlerItem = energyCapability.get();
            if (energyHandlerItem instanceof IMekanismStrictEnergyHandler) {
                return ((IMekanismStrictEnergyHandler) energyHandlerItem).getEnergyContainer(container, null);
            }
        }
        return null;
    }

    public static double getDurabilityForDisplay(ItemStack stack) {
        //Note we ensure the capabilities are not null, as the first call to getDurabilityForDisplay happens before capability injection
        if (Capabilities.GAS_HANDLER_CAPABILITY == null || CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY == null || Capabilities.STRICT_ENERGY_CAPABILITY == null) {
            return 1;
        }
        double gasRatio = 0;
        double fluidRatio = 0;
        double energyRatio = 0;
        Optional<IGasHandler> gasCapability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (gasCapability.isPresent()) {
            IGasHandler gasHandlerItem = gasCapability.get();
            //TODO: Support having multiple tanks at some point, none of our items currently do so, so it doesn't matter that much
            if (gasHandlerItem.getGasTankCount() > 0) {
                //Validate something didn't go terribly wrong and we actually do have the tank we expect to have
                gasRatio = gasHandlerItem.getGasInTank(0).getAmount() / (double) gasHandlerItem.getGasTankCapacity(0);
            }
        }
        Optional<IFluidHandlerItem> fluidCapability = MekanismUtils.toOptional(stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY));
        if (fluidCapability.isPresent()) {
            IFluidHandlerItem fluidHandlerItem = fluidCapability.get();
            //TODO: Support having multiple tanks at some point, none of our items currently do so, so it doesn't matter that much
            if (fluidHandlerItem.getTanks() > 0) {
                //Validate something didn't go terribly wrong and we actually do have the tank we expect to have
                fluidRatio = fluidHandlerItem.getFluidInTank(0).getAmount() / (double) fluidHandlerItem.getTankCapacity(0);
            }
        }
        Optional<IStrictEnergyHandler> energyCapability = MekanismUtils.toOptional(stack.getCapability(Capabilities.STRICT_ENERGY_CAPABILITY));
        if (energyCapability.isPresent()) {
            IStrictEnergyHandler energyHandlerItem = energyCapability.get();
            //TODO: Support having multiple containers at some point, none of our items currently do so, so it doesn't matter that much
            if (energyHandlerItem.getEnergyContainerCount() > 0) {
                //Validate something didn't go terribly wrong and we actually do have the container we expect to have
                energyRatio = energyHandlerItem.getEnergy(0).divideToLevel(energyHandlerItem.getMaxEnergy(0));
            }
        }
        return 1D - Math.max(Math.max(gasRatio, fluidRatio), energyRatio);
    }

    public static void mergeTanks(IExtendedFluidTank tank, IExtendedFluidTank mergeTank) {
        if (tank.isEmpty()) {
            tank.setStack(mergeTank.getFluid());
        } else if (!mergeTank.isEmpty() && tank.isFluidEqual(mergeTank.getFluid())) {
            mergeTank.growStack(tank.getFluidAmount(), Action.EXECUTE);
        }
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void mergeTanks(IChemicalTank<CHEMICAL, STACK> tank,
          IChemicalTank<CHEMICAL, STACK> mergeTank) {
        if (tank.isEmpty()) {
            tank.setStack(mergeTank.getStack());
        } else if (!mergeTank.isEmpty() && tank.isTypeEqual(mergeTank.getStack())) {
            mergeTank.growStack(tank.getStored(), Action.EXECUTE);
        }
    }

    public static void mergeContainers(IEnergyContainer container, IEnergyContainer mergeContainer) {
        if (container.isEmpty()) {
            container.setEnergy(mergeContainer.getEnergy());
        } else if (!mergeContainer.isEmpty()) {
            mergeContainer.setEnergy(mergeContainer.getEnergy().add(mergeContainer.getEnergy()));
        }
    }
}