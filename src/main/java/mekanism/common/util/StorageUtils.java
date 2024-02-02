package mekanism.common.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.Action;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
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
import mekanism.common.attachments.containers.AttachedChemicalTanks;
import mekanism.common.attachments.containers.AttachedEnergyContainers;
import mekanism.common.attachments.containers.AttachedFluidTanks;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StorageUtils {

    private StorageUtils() {
    }

    public static void addStoredEnergy(@NotNull ItemStack stack, @NotNull List<Component> tooltip, boolean showMissingCap) {
        addStoredEnergy(stack, tooltip, showMissingCap, MekanismLang.STORED_ENERGY);
    }

    public static void addStoredEnergy(@NotNull ItemStack stack, @NotNull List<Component> tooltip, boolean showMissingCap, ILangEntry langEntry) {
        IStrictEnergyHandler energyHandlerItem = Capabilities.STRICT_ENERGY.getCapability(stack);
        if (energyHandlerItem != null) {
            int energyContainerCount = energyHandlerItem.getEnergyContainerCount();
            for (int container = 0; container < energyContainerCount; container++) {
                tooltip.add(langEntry.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY,
                      EnergyDisplay.of(energyHandlerItem.getEnergy(container), energyHandlerItem.getMaxEnergy(container))));
            }
        } else if (showMissingCap) {
            tooltip.add(langEntry.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY, EnergyDisplay.ZERO));
        }
    }

    public static void addStoredGas(@NotNull ItemStack stack, @NotNull List<Component> tooltip, boolean showMissingCap, boolean showAttributes) {
        addStoredGas(stack, tooltip, showMissingCap, showAttributes, MekanismLang.NO_GAS);
    }

    public static void addStoredGas(@NotNull ItemStack stack, @NotNull List<Component> tooltip, boolean showMissingCap, boolean showAttributes,
          ILangEntry emptyLangEntry) {
        addStoredChemical(stack, tooltip, showMissingCap, showAttributes, emptyLangEntry, stored -> {
            if (stored.isEmpty()) {
                return emptyLangEntry.translateColored(EnumColor.GRAY);
            }
            return MekanismLang.STORED.translateColored(EnumColor.ORANGE, EnumColor.ORANGE, stored, EnumColor.GRAY,
                  MekanismLang.GENERIC_MB.translate(TextUtils.format(stored.getAmount())));
        }, Capabilities.GAS.item());
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>>
    void addStoredChemical(@NotNull ItemStack stack, @NotNull List<Component> tooltip, boolean showMissingCap, boolean showAttributes, ILangEntry emptyLangEntry,
          Function<STACK, Component> storedFunction, ItemCapability<HANDLER, Void> capability) {
        HANDLER handler = stack.getCapability(capability);
        if (handler != null) {
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

    public static void addStoredFluid(@NotNull ItemStack stack, @NotNull List<Component> tooltip, boolean showMissingCap) {
        addStoredFluid(stack, tooltip, showMissingCap, MekanismLang.NO_FLUID_TOOLTIP);
    }

    public static void addStoredFluid(@NotNull ItemStack stack, @NotNull List<Component> tooltip, boolean showMissingCap, ILangEntry emptyLangEntry) {
        addStoredFluid(stack, tooltip, showMissingCap, emptyLangEntry, stored -> {
            if (stored.isEmpty()) {
                return emptyLangEntry.translateColored(EnumColor.GRAY);
            }
            return MekanismLang.STORED.translateColored(EnumColor.ORANGE, EnumColor.ORANGE, stored, EnumColor.GRAY,
                  MekanismLang.GENERIC_MB.translate(TextUtils.format(stored.getAmount())));
        });
    }

    public static void addStoredFluid(@NotNull ItemStack stack, @NotNull List<Component> tooltip, boolean showMissingCap, ILangEntry emptyLangEntry,
          Function<FluidStack, Component> storedFunction) {
        IFluidHandlerItem handler = Capabilities.FLUID.getCapability(stack);
        if (handler != null) {
            for (int tank = 0, tanks = handler.getTanks(); tank < tanks; tank++) {
                tooltip.add(storedFunction.apply(handler.getFluidInTank(tank)));
            }
        } else if (showMissingCap) {
            tooltip.add(emptyLangEntry.translate());
        }
    }

    /**
     * @implNote Assumes there is only one "type" per substance type
     */
    public static void addStoredSubstance(@NotNull ItemStack stack, @NotNull List<Component> tooltip, boolean isCreative) {
        FluidStack fluidStack = getStoredFluidFromAttachment(stack);
        GasStack gasStack = getStoredGasFromAttachment(stack);
        InfusionStack infusionStack = getStoredInfusionFromAttachment(stack);
        PigmentStack pigmentStack = getStoredPigmentFromAttachment(stack);
        SlurryStack slurryStack = getStoredSlurryFromAttachment(stack);
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

    @NotNull
    public static GasStack getContainedGas(ItemStack stack, Gas type) {
        return getContainedGas(Capabilities.GAS.getCapability(stack), type);
    }

    @NotNull
    public static GasStack getContainedGas(IGasHandler gasHandler, Gas type) {
        return getContainedChemical(gasHandler, type).orElse(GasStack.EMPTY);
    }

    @NotNull
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>>
    Optional<STACK> getContainedChemical(HANDLER handler, CHEMICAL type) {
        for (int tank = 0, tanks = handler.getTanks(); tank < tanks; tank++) {
            STACK chemicalInTank = handler.getChemicalInTank(tank);
            if (chemicalInTank.isTypeEqual(type)) {
                return Optional.of(chemicalInTank);
            }
        }
        return Optional.empty();
    }

    public static FluidStack getContainedFluid(@NotNull IFluidHandlerItem fluidHandlerItem, FluidStack type) {
        for (int i = 0, tanks = fluidHandlerItem.getTanks(); i < tanks; i++) {
            FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(i);
            if (fluidInTank.isFluidEqual(type)) {
                return fluidInTank;
            }
        }
        return FluidStack.EMPTY;
    }

    /**
     * Gets the fluid stored in an item's container by checking the attachment. This is for cases when we may not actually have an energy handler provided as
     * a capability from our item, but it may have stored data in its container from when it was a block
     */
    @NotNull
    public static FluidStack getStoredFluidFromAttachment(ItemStack stack) {//TODO - 1.20.4: Test this
        FluidStack fluid = FluidStack.EMPTY;
        if (stack.hasData(MekanismAttachmentTypes.FLUID_TANKS)) {
            AttachedFluidTanks attachment = stack.getData(MekanismAttachmentTypes.FLUID_TANKS);
            for (IExtendedFluidTank tank : attachment.getFluidTanks(null)) {
                if (tank.isEmpty()) {
                    continue;
                }
                if (fluid.isEmpty()) {
                    fluid = tank.getFluid().copy();
                } else if (tank.isFluidEqual(fluid)) {
                    if (fluid.getAmount() < Integer.MAX_VALUE - tank.getFluidAmount()) {
                        fluid.grow(tank.getFluidAmount());
                    } else {
                        fluid.setAmount(Integer.MAX_VALUE);
                    }
                }
                //Note: If we have multiple tanks that have different types stored we only return the first type
            }
        }
        return fluid;
    }
    /**
     * Gets the gas stored in an item's container by checking the attachment. This is for cases when we may not actually have an energy handler provided as
     * a capability from our item, but it may have stored data in its container from when it was a block
     */
    @NotNull
    public static GasStack getStoredGasFromAttachment(ItemStack stack) {//TODO - 1.20.4: Test this
        return getStoredChemicalFromAttachment(stack, GasStack.EMPTY, MekanismAttachmentTypes.GAS_TANKS);
    }

    /**
     * Gets the infuse type stored in an item's container by checking the attachment. This is for cases when we may not actually have an energy handler provided as
     * a capability from our item, but it may have stored data in its container from when it was a block
     */
    @NotNull
    public static InfusionStack getStoredInfusionFromAttachment(ItemStack stack) {//TODO - 1.20.4: Test this
        return getStoredChemicalFromAttachment(stack, InfusionStack.EMPTY, MekanismAttachmentTypes.INFUSION_TANKS);
    }

    /**
     * Gets the pigment stored in an item's container by checking the attachment. This is for cases when we may not actually have an energy handler provided as
     * a capability from our item, but it may have stored data in its container from when it was a block
     */
    @NotNull
    public static PigmentStack getStoredPigmentFromAttachment(ItemStack stack) {//TODO - 1.20.4: Test this
        return getStoredChemicalFromAttachment(stack, PigmentStack.EMPTY, MekanismAttachmentTypes.PIGMENT_TANKS);
    }

    /**
     * Gets the slurry stored in an item's container by checking the attachment. This is for cases when we may not actually have an energy handler provided as
     * a capability from our item, but it may have stored data in its container from when it was a block
     */
    @NotNull
    public static SlurryStack getStoredSlurryFromAttachment(ItemStack stack) {//TODO - 1.20.4: Test this
        return getStoredChemicalFromAttachment(stack, SlurryStack.EMPTY, MekanismAttachmentTypes.SLURRY_TANKS);
    }

    @NotNull
    private static <STACK extends ChemicalStack<?>, TANK extends IChemicalTank<?, STACK>, ATTACHMENT extends AttachedChemicalTanks<?, STACK, TANK>>
    STACK getStoredChemicalFromAttachment(ItemStack stack, STACK emptyStack, Supplier<AttachmentType<ATTACHMENT>> attachmentType) {
        STACK chemicalStack = emptyStack;
        if (stack.hasData(attachmentType)) {
            ATTACHMENT attachment = stack.getData(attachmentType);
            for (TANK tank : attachment.getChemicalTanks(null)) {
                if (tank.isEmpty()) {
                    continue;
                }
                if (chemicalStack.isEmpty()) {
                    chemicalStack = ChemicalUtil.copy(tank.getStack());
                } else if (tank.isTypeEqual(chemicalStack)) {
                    if (chemicalStack.getAmount() < Long.MAX_VALUE - tank.getStored()) {
                        chemicalStack.grow(tank.getStored());
                    } else {
                        chemicalStack.setAmount(Long.MAX_VALUE);
                    }
                }
                //Note: If we have multiple tanks that have different types stored we only return the first type
            }
        }
        return chemicalStack;
    }

    /**
     * Gets the energy if one is stored from an item's container by checking the attachment. This is for cases when we may not actually have an energy handler provided as
     * a capability from our item, but it may have stored data in its container from when it was a block
     */
    public static FloatingLong getStoredEnergyFromAttachment(ItemStack stack) {//TODO - 1.20.4: Test this
        FloatingLong energy = FloatingLong.ZERO;
        if (stack.hasData(MekanismAttachmentTypes.ENERGY_CONTAINERS)) {
            AttachedEnergyContainers attachment = stack.getData(MekanismAttachmentTypes.ENERGY_CONTAINERS);
            for (IEnergyContainer energyContainer : attachment.getEnergyContainers(null)) {
                energy = energy.plusEqual(energyContainer.getEnergy());
            }
        }
        return energy;
    }

    public static ItemStack getFilledEnergyVariant(ItemStack toFill) {
        //TODO - 1.20.4: Why doesn't this work for what is displayed in JEI, and why does it display for the creative energy cube
        AttachedEnergyContainers attachment = ContainerType.ENERGY.getAttachment(toFill);
        if (attachment != null) {
            for (IEnergyContainer energyContainer : attachment.getEnergyContainers(null)) {
                //TODO - 1.20.4: Evaluate these direct set calls, both here and for fluids and chemicals
                energyContainer.setEnergy(energyContainer.getMaxEnergy());
            }
        }
        //The item is now filled return it for convenience
        return toFill;
    }

    @Nullable
    public static IEnergyContainer getEnergyContainer(ItemStack stack, int container) {
        IStrictEnergyHandler energyHandlerItem = Capabilities.STRICT_ENERGY.getCapability(stack);
        if (energyHandlerItem instanceof IMekanismStrictEnergyHandler energyHandler) {
            return energyHandler.getEnergyContainer(container, null);
        }
        return null;
    }

    public static double getEnergyRatio(ItemStack stack) {
        //TODO - 1.20.4: Move this to using attachments maybe?
        IEnergyContainer container = getEnergyContainer(stack, 0);
        return container == null ? 0 : container.getEnergy().divideToLevel(container.getMaxEnergy());
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
        bestRatio = calculateRatio(stack, bestRatio, Capabilities.GAS.item());
        bestRatio = calculateRatio(stack, bestRatio, Capabilities.INFUSION.item());
        bestRatio = calculateRatio(stack, bestRatio, Capabilities.PIGMENT.item());
        bestRatio = calculateRatio(stack, bestRatio, Capabilities.SLURRY.item());
        IFluidHandlerItem fluidHandlerItem = Capabilities.FLUID.getCapability(stack);
        if (fluidHandlerItem != null) {
            for (int tank = 0, tanks = fluidHandlerItem.getTanks(); tank < tanks; tank++) {
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
        IStrictEnergyHandler energyHandlerItem = Capabilities.STRICT_ENERGY.getCapability(stack);
        if (energyHandlerItem != null) {
            int containers = energyHandlerItem.getEnergyContainerCount();
            for (int container = 0; container < containers; container++) {
                bestRatio = Math.max(bestRatio, energyHandlerItem.getEnergy(container).divideToLevel(energyHandlerItem.getMaxEnergy(container)));
            }
        }
        return 1 - bestRatio;
    }

    private static double calculateRatio(ItemStack stack, double bestRatio, ItemCapability<? extends IChemicalHandler<?, ?>, Void> capability) {
        IChemicalHandler<?, ?> handler = stack.getCapability(capability);
        if (handler != null) {
            for (int tank = 0, tanks = handler.getTanks(); tank < tanks; tank++) {
                bestRatio = Math.max(bestRatio, getRatio(handler.getChemicalInTank(tank).getAmount(), handler.getTankCapacity(tank)));
            }
        }
        return bestRatio;
    }

    public static double getRatio(long amount, long capacity) {
        return capacity == 0 ? 1 : amount / (double) capacity;
    }

    public static void mergeFluidTanks(List<IExtendedFluidTank> tanks, List<IExtendedFluidTank> toAdd, List<FluidStack> rejects) {
        validateSizeMatches(tanks, toAdd, "tank");
        for (int i = 0; i < toAdd.size(); i++) {
            IExtendedFluidTank mergeTank = toAdd.get(i);
            if (!mergeTank.isEmpty()) {
                IExtendedFluidTank tank = tanks.get(i);
                FluidStack mergeStack = mergeTank.getFluid();
                if (tank.isEmpty()) {
                    int capacity = tank.getCapacity();
                    if (mergeStack.getAmount() <= capacity) {
                        tank.setStack(mergeStack);
                    } else {
                        tank.setStack(new FluidStack(mergeStack, capacity));
                        int remaining = mergeStack.getAmount() - capacity;
                        if (remaining > 0) {
                            rejects.add(new FluidStack(mergeStack, remaining));
                        }
                    }
                } else if (tank.isFluidEqual(mergeStack)) {
                    int amount = tank.growStack(mergeStack.getAmount(), Action.EXECUTE);
                    int remaining = mergeStack.getAmount() - amount;
                    if (remaining > 0) {
                        rejects.add(new FluidStack(mergeStack, remaining));
                    }
                } else {
                    rejects.add(mergeStack);
                }
            }
        }
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> void mergeTanks(
          List<TANK> tanks, List<TANK> toAdd, List<STACK> rejects) {
        validateSizeMatches(tanks, toAdd, "tank");
        for (int i = 0; i < toAdd.size(); i++) {
            TANK mergeTank = toAdd.get(i);
            if (!mergeTank.isEmpty()) {
                TANK tank = tanks.get(i);
                STACK mergeStack = mergeTank.getStack();
                if (tank.isEmpty()) {
                    long capacity = tank.getCapacity();
                    if (mergeStack.getAmount() <= capacity) {
                        tank.setStack(mergeStack);
                    } else {
                        tank.setStack(ChemicalUtil.copyWithAmount(mergeStack, capacity));
                        long remaining = mergeStack.getAmount() - capacity;
                        if (remaining > 0) {
                            rejects.add(ChemicalUtil.copyWithAmount(mergeStack, remaining));
                        }
                    }
                } else if (tank.isTypeEqual(mergeStack)) {
                    long amount = tank.growStack(mergeStack.getAmount(), Action.EXECUTE);
                    long remaining = mergeStack.getAmount() - amount;
                    if (remaining > 0) {
                        rejects.add(ChemicalUtil.copyWithAmount(mergeStack, remaining));
                    }
                } else {
                    rejects.add(mergeStack);
                }
            }
        }
    }

    public static void mergeEnergyContainers(List<IEnergyContainer> containers, List<IEnergyContainer> toAdd) {
        validateSizeMatches(containers, toAdd, "energy container");
        for (int i = 0; i < toAdd.size(); i++) {
            IEnergyContainer container = containers.get(i);
            IEnergyContainer mergeContainer = toAdd.get(i);
            container.setEnergy(container.getEnergy().add(mergeContainer.getEnergy()));
        }
    }

    public static void mergeHeatCapacitors(List<IHeatCapacitor> capacitors, List<IHeatCapacitor> toAdd) {
        validateSizeMatches(capacitors, toAdd, "heat capacitor");
        for (int i = 0; i < toAdd.size(); i++) {
            IHeatCapacitor capacitor = capacitors.get(i);
            IHeatCapacitor mergeCapacitor = toAdd.get(i);
            capacitor.setHeat(capacitor.getHeat() + mergeCapacitor.getHeat());
            if (capacitor instanceof BasicHeatCapacitor heatCapacitor) {
                heatCapacitor.setHeatCapacity(capacitor.getHeatCapacity() + mergeCapacitor.getHeatCapacity(), false);
            }
        }
    }

    public static <T> void validateSizeMatches(List<T> base, List<T> toAdd, String type) {
        if (base.size() != toAdd.size()) {
            throw new IllegalArgumentException("Mismatched " + type + " count, orig: " + base.size() + ", toAdd: " + toAdd.size());
        }
    }
}