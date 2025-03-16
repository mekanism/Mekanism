package mekanism.common.util;

import java.util.List;
import java.util.function.BiFunction;
import mekanism.api.Action;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
        if (energyHandlerItem == null) {
            //Fall back to trying to look up the stored energy by the container type if the stack doesn't expose it
            energyHandlerItem = ContainerType.ENERGY.createHandlerIfData(stack);
        }
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

    public static void addStoredChemical(@NotNull ItemStack stack, @NotNull List<Component> tooltip) {
        IChemicalHandler handler = Capabilities.CHEMICAL.getCapability(stack);
        if (handler == null) {
            //Fall back to trying to look up the stored chemical by the container type if the stack doesn't expose it
            handler = ContainerType.CHEMICAL.createHandlerIfData(stack);
        }
        if (handler != null) {
            int tanks = handler.getChemicalTanks();
            for (int tank = 0; tank < tanks; tank++) {
                ChemicalStack chemicalInTank = handler.getChemicalInTank(tank);
                if (chemicalInTank.isEmpty()) {
                    tooltip.add(MekanismLang.NO_CHEMICAL.translateColored(EnumColor.GRAY));
                } else {
                    tooltip.add(MekanismLang.STORED.translateColored(EnumColor.ORANGE, EnumColor.ORANGE, chemicalInTank, EnumColor.GRAY,
                          MekanismLang.GENERIC_MB.translate(TextUtils.format(chemicalInTank.getAmount()))));
                }
            }
        } else {
            tooltip.add(MekanismLang.NO_CHEMICAL.translate());
        }
    }

    public static void addStoredFluid(@NotNull ItemStack stack, @NotNull List<Component> tooltip) {
        addStoredFluid(stack, tooltip, MekanismLang.NO_FLUID_TOOLTIP);
    }

    public static void addStoredFluid(@NotNull ItemStack stack, @NotNull List<Component> tooltip, ILangEntry emptyLangEntry) {
        addStoredFluid(stack, tooltip, emptyLangEntry, (stored, emptyLang) -> {
            if (stored.isEmpty()) {
                return emptyLang.translateColored(EnumColor.GRAY);
            }
            return MekanismLang.STORED.translateColored(EnumColor.ORANGE, EnumColor.ORANGE, stored, EnumColor.GRAY,
                  MekanismLang.GENERIC_MB.translate(TextUtils.format(stored.getAmount())));
        });
    }

    public static void addStoredFluid(@NotNull ItemStack stack, @NotNull List<Component> tooltip, ILangEntry emptyLangEntry,
          BiFunction<FluidStack, ILangEntry, Component> storedFunction) {
        IFluidHandlerItem handler = Capabilities.FLUID.getCapability(stack);
        if (handler == null) {
            //Fall back to trying to look up the stored fluid by the container type if the stack doesn't expose it
            handler = ContainerType.FLUID.createHandlerIfData(stack);
        }
        if (handler != null) {
            for (int tank = 0, tanks = handler.getTanks(); tank < tanks; tank++) {
                tooltip.add(storedFunction.apply(handler.getFluidInTank(tank), emptyLangEntry));
            }
        } else {
            tooltip.add(emptyLangEntry.translate());
        }
    }

    /**
     * @implNote Assumes there is only one "type" per substance type
     */
    public static void addStoredSubstance(@NotNull ItemStack stack, @NotNull List<Component> tooltip, boolean isCreative) {
        FluidStack fluidStack = getStoredFluidFromAttachment(stack);
        ChemicalStack chemicalStack = getStoredChemicalFromAttachment(stack);
        if (fluidStack.isEmpty() && chemicalStack.isEmpty()) {
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
            contents = chemicalStack;
            amount = chemicalStack.getAmount();
            type = MekanismLang.CHEMICAL;
        }
        if (isCreative) {
            tooltip.add(type.translateColored(EnumColor.YELLOW, EnumColor.ORANGE, MekanismLang.GENERIC_STORED.translate(contents, EnumColor.GRAY, MekanismLang.INFINITE)));
        } else {
            tooltip.add(type.translateColored(EnumColor.YELLOW, EnumColor.ORANGE, MekanismLang.GENERIC_STORED_MB.translate(contents, EnumColor.GRAY, TextUtils.format(amount))));
        }
    }

    @NotNull
    public static ChemicalStack getContainedChemical(ItemStack stack, Holder<Chemical> type) {
        return getContainedChemical(Capabilities.CHEMICAL.getCapability(stack), type);
    }

    @NotNull
    public static ChemicalStack getContainedChemical(IChemicalHandler handler, Holder<Chemical> type) {
        for (int tank = 0, tanks = handler.getChemicalTanks(); tank < tanks; tank++) {
            ChemicalStack chemicalInTank = handler.getChemicalInTank(tank);
            if (chemicalInTank.is(type)) {
                return chemicalInTank;
            }
        }
        return ChemicalStack.EMPTY;
    }

    public static FluidStack getContainedFluid(@NotNull IFluidHandlerItem fluidHandlerItem, FluidStack type) {
        for (int i = 0, tanks = fluidHandlerItem.getTanks(); i < tanks; i++) {
            FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(i);
            if (FluidStack.isSameFluidSameComponents(fluidInTank, type)) {
                return fluidInTank;
            }
        }
        return FluidStack.EMPTY;
    }

    /**
     * Gets the fluid stored in an item's container by checking the attachment. This is for cases when we may not actually have a fluid handler provided as a capability
     * from our item, but it may have stored data in its container from when it was a block
     */
    @NotNull
    public static FluidStack getStoredFluidFromAttachment(ItemStack stack) {
        List<IExtendedFluidTank> containers = ContainerType.FLUID.getAttachmentContainersIfPresent(stack);
        return switch (containers.size()) {
            case 0 -> FluidStack.EMPTY;
            case 1 -> containers.getFirst().getFluid().copy();
            default -> {
                FluidStack fluid = FluidStack.EMPTY;
                for (IExtendedFluidTank tank : containers) {
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
                yield fluid;
            }
        };
    }

    /**
     * Gets the FIRST fluid stored in an item's container by checking the attachment. This is for cases when we may not actually have a fluid handler provided as a
     * capability from our item, but it may have stored data in its container from when it was a block. Do NOT modify the result
     *
     * @return the first found fluid FOR DISPLAY. Do NOT modify.
     */
    public static FluidStack getFirstFluidFromAttachment(ItemStack stack) {
        List<IExtendedFluidTank> containers = ContainerType.FLUID.getAttachmentContainersIfPresent(stack);
        int size = containers.size();
        return switch (size) {
            case 0 -> FluidStack.EMPTY;
            case 1 -> containers.getFirst().getFluid();
            default -> {
                for (int i = 0; i < size; i++) {
                    FluidStack fluid = containers.get(i).getFluid();
                    if (!fluid.isEmpty()) {
                        yield fluid;
                    }
                }
                yield FluidStack.EMPTY;
            }
        };
    }

    /**
     * Gets the chemical stored in an item's container by checking the attachment. This is for cases when we may not actually have a chemical handler provided as a
     * capability from our item, but it may have stored data in its container from when it was a block
     */
    @NotNull
    public static ChemicalStack getStoredChemicalFromAttachment(ItemStack stack) {
        List<IChemicalTank> containers = ContainerType.CHEMICAL.getAttachmentContainersIfPresent(stack);
        return switch (containers.size()) {
            case 0 -> ChemicalStack.EMPTY;
            case 1 -> containers.getFirst().getStack().copy();
            default -> {
                ChemicalStack chemicalStack = ChemicalStack.EMPTY;
                for (IChemicalTank tank : containers) {
                    if (tank.isEmpty()) {
                        continue;
                    }
                    if (chemicalStack.isEmpty()) {
                        chemicalStack = tank.getStack().copy();
                    } else if (tank.isTypeEqual(chemicalStack)) {
                        if (chemicalStack.getAmount() < Long.MAX_VALUE - tank.getStored()) {
                            chemicalStack.grow(tank.getStored());
                        } else {
                            chemicalStack.setAmount(Long.MAX_VALUE);
                        }
                    }
                    //Note: If we have multiple tanks that have different types stored we only return the first type
                }
                yield chemicalStack;
            }
        };
    }

    /**
     * Gets the FIRST chemical stored in an item's container by checking the attachment. This is for cases when we may not actually have a chemical handler provided as a
     * capability from our item, but it may have stored data in its container from when it was a block. Do NOT modify the result
     *
     * @return the first found chemical FOR DISPLAY. Do NOT modify.
     */
    @NotNull
    public static ChemicalStack getFirstChemicalFromAttachment(ItemStack stack) {
        List<IChemicalTank> containers = ContainerType.CHEMICAL.getAttachmentContainersIfPresent(stack);
        int size = containers.size();
        return switch (size) {
            case 0 -> ChemicalStack.EMPTY;
            case 1 -> containers.getFirst().getStack();
            default -> {
                for (int i = 0; i < size; i++) {
                    ChemicalStack chemicalStack = containers.get(i).getStack();
                    if (!chemicalStack.isEmpty()) {
                        yield chemicalStack;
                    }
                }
                yield ChemicalStack.EMPTY;
            }
        };
    }

    /**
     * Gets the energy if one is stored from an item's container by checking the attachment. This is for cases when we may not actually have an energy handler provided as
     * a capability from our item, but it may have stored data in its container from when it was a block
     */
    public static long getStoredEnergyFromAttachment(ItemStack stack) {
        long energy = 0;
        for (IEnergyContainer energyContainer : ContainerType.ENERGY.getAttachmentContainersIfPresent(stack)) {
            energy = MathUtils.addClamped(energy, energyContainer.getEnergy());
        }
        return energy;
    }

    public static ItemStack getFilledEnergyVariant(Holder<Item> toFill) {
        return getFilledEnergyVariant(new ItemStack(toFill));
    }

    public static ItemStack getFilledEnergyVariant(ItemStack toFill) {
        IMekanismStrictEnergyHandler attachment = ContainerType.ENERGY.createHandler(toFill);
        if (attachment != null) {
            for (IEnergyContainer energyContainer : attachment.getEnergyContainers(null)) {
                energyContainer.setEnergy(energyContainer.getMaxEnergy());
            }
        }
        //The item is now filled return it for convenience
        return toFill;
    }

    @Nullable
    public static IEnergyContainer getEnergyContainer(ItemStack stack, int container) {
        if (stack.isEmpty()) {
            //While getCapability will return null for an empty stack, we just short circuit here
            return null;
        }
        IStrictEnergyHandler energyHandlerItem = Capabilities.STRICT_ENERGY.getCapability(stack);
        if (energyHandlerItem instanceof IMekanismStrictEnergyHandler energyHandler) {
            return energyHandler.getEnergyContainer(container, null);
        }
        return null;
    }

    public static double getEnergyRatio(ItemStack stack) {
        IEnergyContainer container = getEnergyContainer(stack, 0);
        return container == null ? 0 : MathUtils.divideToLevel(container.getEnergy(), container.getMaxEnergy());
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
        if (stack.getCount() > 1) {
            //Note: Technically this is handled by the below check as the capability isn't exposed (so this isn't even visible),
            // but we may as well short circuit it here
            return 0;
        }
        return MathUtils.clampToInt(Math.round(13.0F - 13.0F * getDurabilityForDisplay(stack)));
    }

    private static double getDurabilityForDisplay(ItemStack stack) {
        double bestRatio = 0;
        IChemicalHandler handler = stack.getCapability((ItemCapability<? extends IChemicalHandler, Void>) Capabilities.CHEMICAL.item());
        if (handler != null) {
            for (int chemTack = 0, chemTanks = handler.getChemicalTanks(); chemTack < chemTanks; chemTack++) {
                bestRatio = Math.max(bestRatio, getRatio(handler.getChemicalInTank(chemTack).getAmount(), handler.getChemicalTankCapacity(chemTack)));
            }
        }
        IFluidHandlerItem fluidHandlerItem = Capabilities.FLUID.getCapability(stack);
        if (fluidHandlerItem != null) {
            for (int tank = 0, tanks = fluidHandlerItem.getTanks(); tank < tanks; tank++) {
                bestRatio = Math.max(bestRatio, getRatio(fluidHandlerItem.getFluidInTank(tank).getAmount(), fluidHandlerItem.getTankCapacity(tank)));
            }
        }
        return 1 - bestRatio;
    }

    public static int getEnergyBarWidth(ItemStack stack) {
        if (stack.getCount() > 1) {
            //Note: Technically this is handled by the below check as the capability isn't exposed (so this isn't even visible),
            // but we may as well short circuit it here
            return 0;
        }
        return MathUtils.clampToInt(Math.round(13.0F - 13.0F * getEnergyDurabilityForDisplay(stack)));
    }

    private static double getEnergyDurabilityForDisplay(ItemStack stack) {
        double bestRatio = 0;
        IStrictEnergyHandler energyHandlerItem = Capabilities.STRICT_ENERGY.getCapability(stack);
        if (energyHandlerItem != null) {
            int containers = energyHandlerItem.getEnergyContainerCount();
            for (int container = 0; container < containers; container++) {
                bestRatio = Math.max(bestRatio, MathUtils.divideToLevel(energyHandlerItem.getEnergy(container), energyHandlerItem.getMaxEnergy(container)));
            }
        }
        return 1 - bestRatio;
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
                        tank.setStack(mergeStack.copyWithAmount(capacity));
                        int remaining = mergeStack.getAmount() - capacity;
                        if (remaining > 0) {
                            rejects.add(mergeStack.copyWithAmount(remaining));
                        }
                    }
                } else if (tank.isFluidEqual(mergeStack)) {
                    int amount = tank.growStack(mergeStack.getAmount(), Action.EXECUTE);
                    int remaining = mergeStack.getAmount() - amount;
                    if (remaining > 0) {
                        rejects.add(mergeStack.copyWithAmount(remaining));
                    }
                } else {
                    rejects.add(mergeStack);
                }
            }
        }
    }

    public static void mergeTanks(List<IChemicalTank> tanks, List<IChemicalTank> toAdd, List<ChemicalStack> rejects) {
        validateSizeMatches(tanks, toAdd, "tank");
        for (int i = 0; i < toAdd.size(); i++) {
            IChemicalTank mergeTank = toAdd.get(i);
            if (!mergeTank.isEmpty()) {
                IChemicalTank tank = tanks.get(i);
                ChemicalStack mergeStack = mergeTank.getStack();
                if (tank.isEmpty()) {
                    long capacity = tank.getCapacity();
                    if (mergeStack.getAmount() <= capacity) {
                        tank.setStack(mergeStack);
                    } else {
                        tank.setStack(mergeStack.copyWithAmount(capacity));
                        long remaining = mergeStack.getAmount() - capacity;
                        if (remaining > 0) {
                            rejects.add(mergeStack.copyWithAmount(remaining));
                        }
                    }
                } else if (tank.isTypeEqual(mergeStack)) {
                    long amount = tank.growStack(mergeStack.getAmount(), Action.EXECUTE);
                    long remaining = mergeStack.getAmount() - amount;
                    if (remaining > 0) {
                        rejects.add(mergeStack.copyWithAmount(remaining));
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
            container.setEnergy(MathUtils.addClamped(container.getEnergy(), mergeContainer.getEnergy()));
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