package mekanism.common.util;

import com.google.common.primitives.Ints;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.math.MathUtils;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StorageUtils {//TODO - 26.1: Re-evaluate which of these methods are the same and can be deduplicated and moved to ResourceUtils

    private StorageUtils() {
    }

    public static void addStoredEnergy(@NotNull ItemStack stack, @NotNull Consumer<Component> tooltipAdder, boolean showMissingCap) {
        addStoredEnergy(stack, tooltipAdder, showMissingCap, MekanismLang.STORED_ENERGY);
    }

    public static void addStoredEnergy(@NotNull ItemStack stack, @NotNull Consumer<Component> tooltipAdder, boolean showMissingCap, ILangEntry langEntry) {
        IStrictEnergyHandler energyHandlerItem = Capabilities.STRICT_ENERGY.getCapability(ItemAccess.forStack(stack));
        if (energyHandlerItem == null) {
            //Fall back to trying to look up the stored energy by the container type if the stack doesn't expose it
            energyHandlerItem = ContainerType.ENERGY.createHandlerIfData(stack);
        }
        if (energyHandlerItem != null) {
            int energyContainerCount = energyHandlerItem.size();
            for (int container = 0; container < energyContainerCount; container++) {
                tooltipAdder.accept(langEntry.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY,
                      EnergyDisplay.of(energyHandlerItem.getAmountAsLong(container), energyHandlerItem.getCapacityAsLong(container))));
            }
        } else if (showMissingCap) {
            tooltipAdder.accept(langEntry.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY, EnergyDisplay.ZERO));
        }
    }

    public static void addStoredChemical(@NotNull ItemStack stack, @NotNull Consumer<Component> tooltipAdder) {
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(ItemAccess.forStack(stack));
        if (handler == null) {
            //Fall back to trying to look up the stored chemical by the container type if the stack doesn't expose it
            handler = ContainerType.CHEMICAL.createHandlerIfData(stack);
        }
        if (handler != null) {
            for (int tank = 0, tanks = handler.size(); tank < tanks; tank++) {
                ChemicalResource chemicalInTank = handler.getResource(tank);
                if (chemicalInTank.isEmpty()) {
                    tooltipAdder.accept(MekanismLang.NO_CHEMICAL.translateColored(EnumColor.GRAY));
                } else {
                    tooltipAdder.accept(MekanismLang.STORED.translateColored(EnumColor.ORANGE, EnumColor.ORANGE, chemicalInTank, EnumColor.GRAY,
                          MekanismLang.GENERIC_MB.translate(TextUtils.format(handler.getAmountAsLong(tank)))));
                }
            }
        } else {
            tooltipAdder.accept(MekanismLang.NO_CHEMICAL.translate());
        }
    }

    public static void addStoredFluid(@NotNull ItemStack stack, @NotNull Consumer<Component> tooltipAdder) {
        addStoredFluid(stack, tooltipAdder, MekanismLang.NO_FLUID_TOOLTIP);
    }

    public static void addStoredFluid(@NotNull ItemStack stack, @NotNull Consumer<Component> tooltipAdder, ILangEntry emptyLangEntry) {
        addStoredFluid(stack, tooltipAdder, emptyLangEntry, (stored, emptyLang) -> {
            if (stored.isEmpty()) {
                return emptyLang.translateColored(EnumColor.GRAY);
            }
            return MekanismLang.STORED.translateColored(EnumColor.ORANGE, EnumColor.ORANGE, stored, EnumColor.GRAY,
                  MekanismLang.GENERIC_MB.translate(TextUtils.format(stored.amount())));
        });
    }

    public static void addStoredFluid(@NotNull ItemStack stack, @NotNull Consumer<Component> tooltipAdder, ILangEntry emptyLangEntry,
          BiFunction<FluidStack, ILangEntry, Component> storedFunction) {
        ResourceHandler<FluidResource> handler = Capabilities.FLUID.getCapability(ItemAccess.forStack(stack));
        if (handler == null) {
            //Fall back to trying to look up the stored fluid by the container type if the stack doesn't expose it
            handler = ContainerType.FLUID.createHandlerIfData(stack);
        }
        if (handler != null) {
            for (int tank = 0, tanks = handler.size(); tank < tanks; tank++) {
                //TODO - 26.1: Custom function for storedFunction rather than converting this back into a stack?
                tooltipAdder.accept(storedFunction.apply(handler.getResource(tank).toStack(handler.getAmountAsInt(tank)), emptyLangEntry));
            }
        } else {
            tooltipAdder.accept(emptyLangEntry.translate());
        }
    }

    /**
     * @implNote Assumes there is only one "type" per substance type
     */
    public static void addStoredSubstance(@NotNull ItemStack stack, @NotNull Consumer<Component> tooltipAdder, boolean isCreative) {
        LargeResourceStack<FluidResource> fluidStack = getStoredFluidFromAttachment(stack);
        LargeResourceStack<ChemicalResource> chemicalStack = getStoredContentsFromAttachment(stack, ContainerType.CHEMICAL, LargeResourceStack.CHEMICAL_HELPER);
        if (fluidStack.isEmpty() && chemicalStack.isEmpty()) {
            tooltipAdder.accept(MekanismLang.EMPTY.translate());
            return;
        }
        ILangEntry type;
        LargeResourceStack<?> contents;
        if (!fluidStack.isEmpty()) {
            contents = fluidStack;
            type = MekanismLang.LIQUID;
        } else {
            contents = chemicalStack;
            type = MekanismLang.CHEMICAL;
        }
        if (isCreative) {
            tooltipAdder.accept(type.translateColored(EnumColor.YELLOW, EnumColor.ORANGE, MekanismLang.GENERIC_STORED.translate(contents.resource(), EnumColor.GRAY, MekanismLang.INFINITE)));
        } else {
            tooltipAdder.accept(type.translateColored(EnumColor.YELLOW, EnumColor.ORANGE, MekanismLang.GENERIC_STORED_MB.translate(contents.resource(), EnumColor.GRAY, TextUtils.format(contents.amount()))));
        }
    }

    public static long getContainedChemical(ItemStack stack, Holder<Chemical> type) {
        return getContainedChemical(Capabilities.CHEMICAL.getCapability(ItemAccess.forStack(stack)), type);
    }

    public static long getContainedChemical(@Nullable ResourceHandler<ChemicalResource> handler, Holder<Chemical> type) {
        if (handler != null) {
            for (int tank = 0, tanks = handler.size(); tank < tanks; tank++) {
                ChemicalResource chemicalInTank = handler.getResource(tank);
                if (chemicalInTank.is(type)) {
                    return handler.getAmountAsLong(tank);
                }
            }
        }
        return 0;
    }

    /**
     * Gets the fluid stored in an item's container by checking the attachment. This is for cases when we may not actually have a fluid handler provided as a capability
     * from our item, but it may have stored data in its container from when it was a block
     */
    @NotNull//TODO - 26.1: Update docs
    public static <RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> LargeResourceStack<RESOURCE> getStoredContentsFromAttachment(ItemStack stack,
          ContainerType<CONTAINER, ?, ?> containerType, LargeResourceStack.StackHelper<RESOURCE> stackHelper) {
        List<CONTAINER> containers = containerType.getAttachmentContainersIfPresent(stack);
        return switch (containers.size()) {
            case 0 -> stackHelper.empty();
            case 1 -> containers.getFirst().asStack();
            default -> {
                RESOURCE type = stackHelper.empty().resource();
                long storedAmount = 0;
                for (CONTAINER container : containers) {
                    if (container.isEmpty()) {
                        continue;
                    }
                    RESOURCE tankType = container.resource();
                    long tankAmount = container.amountAsLong();
                    if (type.isEmpty()) {
                        type = tankType;
                        storedAmount = tankAmount;
                    } else if (tankType.equals(type)) {
                        if (storedAmount < Long.MAX_VALUE - tankAmount) {
                            storedAmount += tankAmount;
                        } else {
                            storedAmount = Long.MAX_VALUE;
                            break;
                        }
                    }
                    //Note: If we have multiple tanks that have different types stored we only return the first type
                }
                yield stackHelper.createStack(type, storedAmount);
            }
        };
    }

    /**
     * Gets the fluid stored in an item's container by checking the attachment. This is for cases when we may not actually have a fluid handler provided as a capability
     * from our item, but it may have stored data in its container from when it was a block
     */
    @NotNull
    public static LargeResourceStack<FluidResource> getStoredFluidFromAttachment(ItemStack stack) {
        return getStoredContentsFromAttachment(stack, ContainerType.FLUID, LargeResourceStack.FLUID_HELPER);
    }

    /**
     * Gets the FIRST fluid stored in an item's container by checking the attachment. This is for cases when we may not actually have a fluid handler provided as a
     * capability from our item, but it may have stored data in its container from when it was a block. Do NOT modify the result
     *
     * @return the first found fluid FOR DISPLAY. Do NOT modify.
     */
    public static FluidStack getFirstFluidFromAttachment(ItemStack stack) {
        List<IFluidTank> containers = ContainerType.FLUID.getAttachmentContainersIfPresent(stack);
        return switch (containers.size()) {
            case 0 -> FluidStack.EMPTY;
            case 1 -> {
                IFluidTank tank = containers.getFirst();
                yield tank.resource().toStack(tank.amountAsInt());
            }
            default -> {
                for (IFluidTank tank : containers) {
                    if (!tank.isEmpty()) {
                        yield tank.resource().toStack(tank.amountAsInt());
                    }
                }
                yield FluidStack.EMPTY;
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
    public static ChemicalResource getFirstChemicalFromAttachment(ItemStack stack) {
        List<IChemicalTank> containers = ContainerType.CHEMICAL.getAttachmentContainersIfPresent(stack);
        int size = containers.size();
        return switch (size) {
            case 0 -> ChemicalResource.EMPTY;
            case 1 -> containers.getFirst().resource();
            default -> {
                for (IChemicalTank tank : containers) {
                    if (!tank.isEmpty()) {
                        yield tank.resource();
                    }
                }
                yield ChemicalResource.EMPTY;
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
            energy = MathUtils.addClamped(energy, energyContainer.energy());
        }
        return energy;
    }

    public static ItemStack getFilledEnergyVariant(Holder<Item> toFill) {
        return getFilledEnergyVariant(new ItemStack(toFill));
    }

    public static ItemStack getFilledEnergyVariant(ItemStack toFill) {
        IMekanismStrictEnergyHandler attachment = ContainerType.ENERGY.createHandler(toFill);
        if (attachment != null) {
            for (IEnergyContainer energyContainer : attachment.getContainers()) {
                energyContainer.setEnergy(energyContainer.capacity());
            }
        }
        //The item is now filled return it for convenience
        return toFill;
    }

    @Nullable//TODO - 26.1: Evaluate usages and probably try to remove this method
    public static IEnergyContainer getEnergyContainer(ItemStack stack, int container) {
        //TODO - 26.1: See which ones of these can be moved to the item access method with more specific item access values
        if (stack.isEmpty()) {
            //While getCapability will return null for an empty stack, we just short circuit here
            return null;
        }
        return getEnergyContainer(ItemAccess.forStack(stack), container);
    }

    @Nullable
    public static IEnergyContainer getEnergyContainer(ItemAccess itemAccess, int container) {//TODO - 26.1: Re-evaluate callers
        IStrictEnergyHandler energyHandlerItem = Capabilities.STRICT_ENERGY.getCapability(itemAccess);
        if (energyHandlerItem instanceof IMekanismStrictEnergyHandler energyHandler && container >= 0 && container < energyHandler.size()) {
            return energyHandler.getContainer(container);
        }
        return null;
    }

    public static double getEnergyRatio(ItemStack stack) {
        IEnergyContainer container = getEnergyContainer(stack, 0);
        return container == null ? 0 : MathUtils.divideToLevel(container.energy(), container.capacity());
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
        if (stack.count() > 1) {
            //Note: Technically this is handled by the below check as the capability isn't exposed (so this isn't even visible),
            // but we may as well short circuit it here
            return 0;
        }
        return Ints.saturatedCast(Math.round(13.0F - 13.0F * getDurabilityForDisplay(stack)));
    }

    private static double getDurabilityForDisplay(ItemStack stack) {
        double bestRatio = 0;
        ItemAccess itemAccess = ItemAccess.forStack(stack);
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(itemAccess);
        if (handler != null) {
            for (int chemTank = 0, chemTanks = handler.size(); chemTank < chemTanks; chemTank++) {
                ChemicalResource chemicalType = handler.getResource(chemTank);
                if (!chemicalType.isEmpty()) {
                    bestRatio = Math.max(bestRatio, getRatio(handler.getAmountAsLong(chemTank), handler.getCapacityAsLong(chemTank, chemicalType)));
                }
            }
        }
        ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(itemAccess);
        if (fluidHandler != null) {
            for (int tank = 0, tanks = fluidHandler.size(); tank < tanks; tank++) {
                FluidResource currentType = fluidHandler.getResource(tank);
                long stored = fluidHandler.getAmountAsLong(tank);
                long capacity = fluidHandler.getCapacityAsLong(tank, currentType);
                bestRatio = Math.max(bestRatio, getRatio(stored, capacity));
            }
        }
        return 1 - bestRatio;
    }

    public static int getEnergyBarWidth(ItemStack stack) {
        if (stack.count() > 1) {
            //Note: Technically this is handled by the below check as the capability isn't exposed (so this isn't even visible),
            // but we may as well short circuit it here
            return 0;
        }
        return Ints.saturatedCast(Math.round(13.0F - 13.0F * getEnergyDurabilityForDisplay(stack)));
    }

    private static double getEnergyDurabilityForDisplay(ItemStack stack) {
        double bestRatio = 0;
        IStrictEnergyHandler energyHandlerItem = Capabilities.STRICT_ENERGY.getCapability(ItemAccess.forStack(stack));
        if (energyHandlerItem != null) {
            int containers = energyHandlerItem.size();
            for (int container = 0; container < containers; container++) {
                bestRatio = Math.max(bestRatio, MathUtils.divideToLevel(energyHandlerItem.getAmountAsLong(container), energyHandlerItem.getCapacityAsLong(container)));
            }
        }
        return 1 - bestRatio;
    }

    public static double getRatio(long amount, long capacity) {
        return capacity == 0 ? 1 : amount / (double) capacity;
    }

    public static void mergeEnergyContainers(List<IEnergyContainer> containers, List<IEnergyContainer> toAdd, TransactionContext transaction) {
        validateSizeMatches(containers, toAdd, "energy container");
        //TODO - 26.1: Make use of the transaction?
        for (int i = 0; i < toAdd.size(); i++) {
            IEnergyContainer container = containers.get(i);
            IEnergyContainer mergeContainer = toAdd.get(i);
            container.setEnergy(MathUtils.addClamped(container.energy(), mergeContainer.energy()));
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