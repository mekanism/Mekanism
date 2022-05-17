package mekanism.common.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class StorageUtils {

    private StorageUtils() {
    }

    public static void addStoredEnergy(@Nonnull ItemStack stack, @Nonnull List<Component> tooltip, boolean showMissingCap) {
        addStoredEnergy(stack, tooltip, showMissingCap, MekanismLang.STORED_ENERGY);
    }

    public static void addStoredEnergy(@Nonnull ItemStack stack, @Nonnull List<Component> tooltip, boolean showMissingCap, ILangEntry langEntry) {
        Optional<IStrictEnergyHandler> capability = stack.getCapability(Capabilities.STRICT_ENERGY_CAPABILITY).resolve();
        if (capability.isPresent()) {
            IStrictEnergyHandler energyHandlerItem = capability.get();
            int energyContainerCount = energyHandlerItem.getEnergyContainerCount();
            for (int container = 0; container < energyContainerCount; container++) {
                tooltip.add(langEntry.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY,
                      EnergyDisplay.of(energyHandlerItem.getEnergy(container), energyHandlerItem.getMaxEnergy(container))));
            }
        } else if (showMissingCap) {
            tooltip.add(langEntry.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY, EnergyDisplay.ZERO));
        }
    }

    public static void addStoredGas(@Nonnull ItemStack stack, @Nonnull List<Component> tooltip, boolean showMissingCap, boolean showAttributes) {
        addStoredGas(stack, tooltip, showMissingCap, showAttributes, MekanismLang.NO_GAS);
    }

    public static void addStoredGas(@Nonnull ItemStack stack, @Nonnull List<Component> tooltip, boolean showMissingCap, boolean showAttributes,
          ILangEntry emptyLangEntry) {
        addStoredChemical(stack, tooltip, showMissingCap, showAttributes, emptyLangEntry, stored -> {
            if (stored.isEmpty()) {
                return emptyLangEntry.translateColored(EnumColor.GRAY);
            }
            return MekanismLang.STORED.translateColored(EnumColor.ORANGE, EnumColor.ORANGE, stored, EnumColor.GRAY,
                  MekanismLang.GENERIC_MB.translate(TextUtils.format(stored.getAmount())));
        }, Capabilities.GAS_HANDLER_CAPABILITY);
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>>
    void addStoredChemical(@Nonnull ItemStack stack, @Nonnull List<Component> tooltip, boolean showMissingCap, boolean showAttributes, ILangEntry emptyLangEntry,
          Function<STACK, Component> storedFunction, Capability<HANDLER> capability) {
        Optional<HANDLER> cap = stack.getCapability(capability).resolve();
        if (cap.isPresent()) {
            HANDLER handler = cap.get();
            for (int tank = 0, tanks = handler.getTanks(); tank < tanks; tank++) {
                STACK chemicalInTank = handler.getChemicalInTank(tank);
                tooltip.add(storedFunction.apply(chemicalInTank));
                if (showAttributes) {
                    ChemicalUtil.addAttributeTooltips(tooltip, chemicalInTank.getType());
                }
            }
        } else if (showMissingCap) {
            tooltip.add(emptyLangEntry.translate());
        }
    }

    public static void addStoredFluid(@Nonnull ItemStack stack, @Nonnull List<Component> tooltip, boolean showMissingCap) {
        addStoredFluid(stack, tooltip, showMissingCap, MekanismLang.NO_FLUID_TOOLTIP);
    }

    public static void addStoredFluid(@Nonnull ItemStack stack, @Nonnull List<Component> tooltip, boolean showMissingCap, ILangEntry emptyLangEntry) {
        addStoredFluid(stack, tooltip, showMissingCap, emptyLangEntry, stored -> {
            if (stored.isEmpty()) {
                return emptyLangEntry.translateColored(EnumColor.GRAY);
            }
            return MekanismLang.STORED.translateColored(EnumColor.ORANGE, EnumColor.ORANGE, stored, EnumColor.GRAY,
                  MekanismLang.GENERIC_MB.translate(TextUtils.format(stored.getAmount())));
        });
    }

    public static void addStoredFluid(@Nonnull ItemStack stack, @Nonnull List<Component> tooltip, boolean showMissingCap, ILangEntry emptyLangEntry,
          Function<FluidStack, Component> storedFunction) {
        Optional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(stack).resolve();
        if (cap.isPresent()) {
            IFluidHandlerItem handler = cap.get();
            for (int tank = 0, tanks = handler.getTanks(); tank < tanks; tank++) {
                tooltip.add(storedFunction.apply(handler.getFluidInTank(tank)));
            }
        } else if (showMissingCap) {
            tooltip.add(emptyLangEntry.translate());
        }
    }

    /**
     * @implNote Assumes there is only one "tank"
     */
    public static void addStoredSubstance(@Nonnull ItemStack stack, @Nonnull List<Component> tooltip, boolean isCreative) {
        FluidStack fluidStack = StorageUtils.getStoredFluidFromNBT(stack);
        GasStack gasStack = StorageUtils.getStoredGasFromNBT(stack);
        InfusionStack infusionStack = StorageUtils.getStoredInfusionFromNBT(stack);
        PigmentStack pigmentStack = StorageUtils.getStoredPigmentFromNBT(stack);
        SlurryStack slurryStack = StorageUtils.getStoredSlurryFromNBT(stack);
        if (fluidStack.isEmpty() && gasStack.isEmpty() && infusionStack.isEmpty() && pigmentStack.isEmpty() && slurryStack.isEmpty()) {
            tooltip.add(MekanismLang.EMPTY.translate());
            return;
        }
        ILangEntry type;
        Object contents;
        long amount;
        if (!fluidStack.isEmpty()) {
            contents = fluidStack;
            amount = fluidStack.getAmount();
            type = MekanismLang.LIQUID;
        } else {
            ChemicalStack<?> chemicalStack;
            if (!gasStack.isEmpty()) {
                chemicalStack = gasStack;
                type = MekanismLang.GAS;
            } else if (!infusionStack.isEmpty()) {
                chemicalStack = infusionStack;
                type = MekanismLang.INFUSE_TYPE;
            } else if (!pigmentStack.isEmpty()) {
                chemicalStack = pigmentStack;
                type = MekanismLang.PIGMENT;
            } else if (!slurryStack.isEmpty()) {
                chemicalStack = slurryStack;
                type = MekanismLang.SLURRY;
            } else {
                throw new IllegalStateException("Unknown chemical");
            }
            contents = chemicalStack;
            amount = chemicalStack.getAmount();
        }
        if (isCreative) {
            tooltip.add(type.translateColored(EnumColor.YELLOW, EnumColor.ORANGE, MekanismLang.GENERIC_STORED.translate(contents, EnumColor.GRAY, MekanismLang.INFINITE)));
        } else {
            tooltip.add(type.translateColored(EnumColor.YELLOW, EnumColor.ORANGE, MekanismLang.GENERIC_STORED_MB.translate(contents, EnumColor.GRAY, TextUtils.format(amount))));
        }
    }

    /**
     * Gets the fluid if one is stored from an item's tank going off the basis there is a single tank. This is for cases when we may not actually have a fluid handler
     * attached to our item, but it may have stored data in its tank from when it was a block
     */
    @Nonnull
    public static FluidStack getStoredFluidFromNBT(ItemStack stack) {
        BasicFluidTank tank = BasicFluidTank.create(Integer.MAX_VALUE, null);
        ItemDataUtils.readContainers(stack, NBTConstants.FLUID_TANKS, Collections.singletonList(tank));
        return tank.getFluid();
    }

    /**
     * Gets the gas if one is stored from an item's tank going off the basis there is a single tank. This is for cases when we may not actually have a gas handler
     * attached to our item, but it may have stored data in its tank from when it was a block
     */
    @Nonnull
    public static GasStack getStoredGasFromNBT(ItemStack stack) {
        return getStoredChemicalFromNBT(stack, ChemicalTankBuilder.GAS.createDummy(Long.MAX_VALUE), NBTConstants.GAS_TANKS);
    }

    /**
     * Gets the infuse type if one is stored from an item's tank going off the basis there is a single tank. This is for cases when we may not actually have an infusion
     * handler attached to our item, but it may have stored data in its tank from when it was a block
     */
    @Nonnull
    public static InfusionStack getStoredInfusionFromNBT(ItemStack stack) {
        return getStoredChemicalFromNBT(stack, ChemicalTankBuilder.INFUSION.createDummy(Long.MAX_VALUE), NBTConstants.INFUSION_TANKS);
    }

    /**
     * Gets the pigment if one is stored from an item's tank going off the basis there is a single tank. This is for cases when we may not actually have a pigment handler
     * attached to our item, but it may have stored data in its tank from when it was a block
     */
    @Nonnull
    public static PigmentStack getStoredPigmentFromNBT(ItemStack stack) {
        return getStoredChemicalFromNBT(stack, ChemicalTankBuilder.PIGMENT.createDummy(Long.MAX_VALUE), NBTConstants.PIGMENT_TANKS);
    }

    /**
     * Gets the slurry if one is stored from an item's tank going off the basis there is a single tank. This is for cases when we may not actually have a slurry handler
     * attached to our item, but it may have stored data in its tank from when it was a block
     */
    @Nonnull
    public static SlurryStack getStoredSlurryFromNBT(ItemStack stack) {
        return getStoredChemicalFromNBT(stack, ChemicalTankBuilder.SLURRY.createDummy(Long.MAX_VALUE), NBTConstants.SLURRY_TANKS);
    }

    @Nonnull
    private static <STACK extends ChemicalStack<?>> STACK getStoredChemicalFromNBT(ItemStack stack, IChemicalTank<?, STACK> tank, String tag) {
        ItemDataUtils.readContainers(stack, tag, Collections.singletonList(tank));
        return tank.getStack();
    }

    /**
     * Gets the energy if one is stored from an item's container going off the basis there is a single energy container. This is for cases when we may not actually have
     * an energy handler attached to our item, but it may have stored data in its container from when it was a block
     */
    public static FloatingLong getStoredEnergyFromNBT(ItemStack stack) {
        BasicEnergyContainer container = BasicEnergyContainer.create(FloatingLong.MAX_VALUE, null);
        ItemDataUtils.readContainers(stack, NBTConstants.ENERGY_CONTAINERS, Collections.singletonList(container));
        return container.getEnergy();
    }

    public static ItemStack getFilledEnergyVariant(ItemStack toFill, FloatingLong capacity) {
        //Manually handle this as capabilities are not necessarily loaded yet (at least not on the first call to this, which is made via fillItemGroup)
        BasicEnergyContainer container = BasicEnergyContainer.create(capacity, null);
        container.setEnergy(capacity);
        ItemDataUtils.writeContainers(toFill, NBTConstants.ENERGY_CONTAINERS, Collections.singletonList(container));
        //The item is now filled return it for convenience
        return toFill;
    }

    @Nullable
    public static IEnergyContainer getEnergyContainer(ItemStack stack, int container) {
        if (!stack.isEmpty()) {
            Optional<IStrictEnergyHandler> energyCapability = stack.getCapability(Capabilities.STRICT_ENERGY_CAPABILITY).resolve();
            if (energyCapability.isPresent()) {
                IStrictEnergyHandler energyHandlerItem = energyCapability.get();
                if (energyHandlerItem instanceof IMekanismStrictEnergyHandler energyHandler) {
                    return energyHandler.getEnergyContainer(container, null);
                }
            }
        }
        return null;
    }

    public static double getEnergyRatio(ItemStack stack) {
        IEnergyContainer container = getEnergyContainer(stack, 0);
        double ratio = 0.0D;
        if (container != null) {
            ratio = container.getEnergy().divideToLevel(container.getMaxEnergy());
        }
        return ratio;
    }

    public static Component getEnergyPercent(ItemStack stack, boolean colorText) {
        return getStoragePercent(getEnergyRatio(stack), colorText);
    }

    public static Component getStoragePercent(double ratio, boolean colorText) {
        Component text = TextUtils.getPercent(ratio);
        if (!colorText) {
            return text;
        }
        EnumColor color;
        if (ratio < 0.01F) {
            color = EnumColor.DARK_RED;
        } else if (ratio < 0.1F) {
            color = EnumColor.RED;
        } else if (ratio < 0.25F) {
            color = EnumColor.ORANGE;
        } else if (ratio < 0.5F) {
            color = EnumColor.YELLOW;
        } else {
            color = EnumColor.BRIGHT_GREEN;
        }
        return TextComponentUtil.build(color, text);
    }

    public static int getBarWidth(ItemStack stack) {
        return MathUtils.clampToInt(Math.round(13.0F - 13.0F * getDurabilityForDisplay(stack)));
    }

    private static double getDurabilityForDisplay(ItemStack stack) {
        double bestRatio = 0;
        bestRatio = calculateRatio(stack, bestRatio, Capabilities.GAS_HANDLER_CAPABILITY);
        bestRatio = calculateRatio(stack, bestRatio, Capabilities.INFUSION_HANDLER_CAPABILITY);
        bestRatio = calculateRatio(stack, bestRatio, Capabilities.PIGMENT_HANDLER_CAPABILITY);
        bestRatio = calculateRatio(stack, bestRatio, Capabilities.SLURRY_HANDLER_CAPABILITY);
        Optional<IFluidHandlerItem> fluidCapability = FluidUtil.getFluidHandler(stack).resolve();
        if (fluidCapability.isPresent()) {
            IFluidHandlerItem fluidHandlerItem = fluidCapability.get();
            int tanks = fluidHandlerItem.getTanks();
            for (int tank = 0; tank < tanks; tank++) {
                bestRatio = Math.max(bestRatio, getRatio(fluidHandlerItem.getFluidInTank(tank).getAmount(), fluidHandlerItem.getTankCapacity(tank)));
            }
        }
        return 1 - bestRatio;
    }

    public static int getEnergyBarWidth(ItemStack stack) {
        return MathUtils.clampToInt(Math.round(13.0F - 13.0F * getEnergyDurabilityForDisplay(stack)));
    }

    private static double getEnergyDurabilityForDisplay(ItemStack stack) {
        double bestRatio = 0;
        Optional<IStrictEnergyHandler> energyCapability = stack.getCapability(Capabilities.STRICT_ENERGY_CAPABILITY).resolve();
        if (energyCapability.isPresent()) {
            IStrictEnergyHandler energyHandlerItem = energyCapability.get();
            int containers = energyHandlerItem.getEnergyContainerCount();
            for (int container = 0; container < containers; container++) {
                bestRatio = Math.max(bestRatio, energyHandlerItem.getEnergy(container).divideToLevel(energyHandlerItem.getMaxEnergy(container)));
            }
        }
        return 1 - bestRatio;
    }

    private static double calculateRatio(ItemStack stack, double bestRatio, Capability<? extends IChemicalHandler<?, ?>> capability) {
        Optional<? extends IChemicalHandler<?, ?>> cap = stack.getCapability(capability).resolve();
        if (cap.isPresent()) {
            IChemicalHandler<?, ?> handler = cap.get();
            for (int tank = 0, tanks = handler.getTanks(); tank < tanks; tank++) {
                bestRatio = Math.max(bestRatio, getRatio(handler.getChemicalInTank(tank).getAmount(), handler.getTankCapacity(tank)));
            }
        }
        return bestRatio;
    }

    public static double getRatio(long amount, long capacity) {
        return capacity == 0 ? 1 : amount / (double) capacity;
    }

    public static void mergeTanks(IExtendedFluidTank tank, IExtendedFluidTank mergeTank) {
        if (tank.isEmpty()) {
            tank.setStack(mergeTank.getFluid());
        } else if (!mergeTank.isEmpty() && tank.isFluidEqual(mergeTank.getFluid())) {
            tank.growStack(mergeTank.getFluidAmount(), Action.EXECUTE);
        }
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void mergeTanks(IChemicalTank<CHEMICAL, STACK> tank,
          IChemicalTank<CHEMICAL, STACK> mergeTank) {
        if (tank.isEmpty()) {
            tank.setStack(mergeTank.getStack());
        } else if (!mergeTank.isEmpty() && tank.isTypeEqual(mergeTank.getStack())) {
            tank.growStack(mergeTank.getStored(), Action.EXECUTE);
        }
    }

    public static void mergeContainers(IEnergyContainer container, IEnergyContainer mergeContainer) {
        container.setEnergy(container.getEnergy().add(mergeContainer.getEnergy()));
    }

    public static void mergeContainers(IHeatCapacitor capacitor, IHeatCapacitor mergeCapacitor) {
        capacitor.setHeat(capacitor.getHeat() + mergeCapacitor.getHeat());
        if (capacitor instanceof BasicHeatCapacitor heatCapacitor) {
            heatCapacitor.setHeatCapacity(capacitor.getHeatCapacity() + mergeCapacitor.getHeatCapacity(), false);
        }
    }
}