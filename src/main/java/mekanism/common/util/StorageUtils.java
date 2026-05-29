package mekanism.common.util;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.math.MathUtils;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StorageUtils {//TODO - 26.1: Re-evaluate which of these methods are the same and can be deduplicated and moved to ResourceUtils

    private StorageUtils() {
    }

    public static void addStoredEnergy(@NotNull ItemAccess itemAccess, @NotNull Consumer<Component> tooltipAdder, boolean showMissingCap) {
        addStoredEnergy(itemAccess, tooltipAdder, showMissingCap, MekanismLang.STORED_ENERGY);
    }

    public static void addStoredEnergy(@NotNull ItemAccess itemAccess, @NotNull Consumer<Component> tooltipAdder, boolean showMissingCap, ILangEntry langEntry) {
        EnergyHandler energyHandler = ContainerType.ENERGY.getCapOrUnexposed(itemAccess);
        if (energyHandler != null) {
            tooltipAdder.accept(langEntry.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY, EnergyDisplay.of(energyHandler)));
        } else if (showMissingCap) {
            tooltipAdder.accept(langEntry.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY, EnergyDisplay.ZERO));
        }
    }

    public static void addStoredChemical(@NotNull ItemAccess itemAccess, @NotNull Consumer<Component> tooltipAdder) {
        ResourceHandler<ChemicalResource> handler = ContainerType.CHEMICAL.getCapOrUnexposed(itemAccess);
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

    public static void addStoredFluid(@NotNull ItemAccess itemAccess, @NotNull Consumer<Component> tooltipAdder) {
        addStoredFluid(itemAccess, tooltipAdder, MekanismLang.NO_FLUID_TOOLTIP);
    }

    public static void addStoredFluid(@NotNull ItemAccess itemAccess, @NotNull Consumer<Component> tooltipAdder, ILangEntry emptyLangEntry) {
        addStoredFluid(itemAccess, tooltipAdder, emptyLangEntry, (stored, emptyLang) -> {
            if (stored.isEmpty()) {
                return emptyLang.translateColored(EnumColor.GRAY);
            }
            return MekanismLang.STORED.translateColored(EnumColor.ORANGE, EnumColor.ORANGE, stored, EnumColor.GRAY,
                  MekanismLang.GENERIC_MB.translate(TextUtils.format(stored.amount())));
        });
    }

    public static void addStoredFluid(@NotNull ItemAccess itemAccess, @NotNull Consumer<Component> tooltipAdder, ILangEntry emptyLangEntry,
          BiFunction<FluidStack, ILangEntry, Component> storedFunction) {
        ResourceHandler<FluidResource> handler = ContainerType.FLUID.getCapOrUnexposed(itemAccess);
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
    public static void addStoredSubstance(@NotNull ItemAccess itemAccess, @NotNull Consumer<Component> tooltipAdder, boolean isCreative) {
        LargeResourceStack<FluidResource> fluidStack = ContainerType.FLUID.getStoredContentsFromAttachment(itemAccess);
        LargeResourceStack<ChemicalResource> chemicalStack = ContainerType.CHEMICAL.getStoredContentsFromAttachment(itemAccess);
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

    public static ItemStack getFilledEnergyVariant(Holder<Item> toFill) {
        return getFilledEnergyVariant(ItemResource.of(toFill));
    }

    public static ItemStack getFilledEnergyVariant(ItemResource toFill) {
        return getFilledEnergyVariant(ItemAccessUtils.queryOnlyAccess(toFill));
    }

    public static ItemStack getFilledEnergyVariant(ItemAccess itemAccess) {
        EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(itemAccess);
        IEnergyContainer energyContainer = EnergyUtils.getEnergyContainer(energyHandler);
        if (energyContainer != null) {
            //Note: Just directly interact with the containers as we want to change the entire access and don't care about splitting between multiple items
            energyContainer.setEnergy(energyContainer.getCapacityAsLong(), null);
        }
        //The item is now filled return it for convenience
        return itemAccess.getResource().toStack(itemAccess.getAmount());
    }

    @Nullable//TODO - 26.1: Evaluate usages and probably try to remove this method
    public static IEnergyContainer getEnergyContainer(ItemStack stack) {
        //TODO - 26.1: See which ones of these can be moved to the item access method with more specific item access values
        if (stack.isEmpty()) {
            //While getCapability will return null for an empty stack, we just short circuit here
            return null;
        }
        return getEnergyContainer(ItemAccess.forStack(stack));
    }

    @Nullable
    public static IEnergyContainer getEnergyContainer(ItemAccess itemAccess) {
        //TODO - 26.1: Re-evaluate callers
        //TODO - 26.1: Should we just remove this method all together? If the passed item access is stacked, then this will return a container that doesn't care about scaling
        EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(itemAccess);
        return EnergyUtils.getEnergyContainer(energyHandler);
    }

    public static double getEnergyRatio(ItemStack stack) {
        IEnergyContainer container = getEnergyContainer(stack);
        return container == null ? 0 : MathUtils.divideToLevel(container.getAmountAsLong(), container.getCapacityAsLong());
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

    public static int getBarWidth(double ratio) {
        return Mth.clamp(Math.round(Item.MAX_BAR_WIDTH * (float) ratio), 0, Item.MAX_BAR_WIDTH);
    }

    public static boolean isBarVisible(ItemStack stack) {
        //TODO - 26.1: Re-evaluate this, we now expose the capability when stacked, so we should potentially have the energy bar display
        //If we are currently stacked, don't display the bar as it will overlap the stack count
        if (stack.count() == 1) {
            //We also don't display the bar if there is nothing stored in any of the containers
            ItemAccess itemAccess = ItemAccess.forStack(stack);
            ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(itemAccess);
            if (handler != null && !ResourceHandlerUtil.isEmpty(handler)) {
                return true;
            }
            ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(itemAccess);
            return fluidHandler != null && !ResourceHandlerUtil.isEmpty(fluidHandler);
        }
        return false;
    }

    public static int getBarWidth(ItemStack stack) {
        double bestRatio = 0;
        ItemAccess itemAccess = ItemAccess.forStack(stack);
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(itemAccess);
        if (handler != null) {
            for (int chemTank = 0, chemTanks = handler.size(); chemTank < chemTanks; chemTank++) {
                ChemicalResource chemicalType = handler.getResource(chemTank);
                if (!chemicalType.isEmpty()) {
                    bestRatio = Math.max(bestRatio, MathUtils.divideToLevel(handler.getAmountAsLong(chemTank), handler.getCapacityAsLong(chemTank, chemicalType)));
                }
            }
        }
        ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(itemAccess);
        if (fluidHandler != null) {
            for (int tank = 0, tanks = fluidHandler.size(); tank < tanks; tank++) {
                FluidResource currentType = fluidHandler.getResource(tank);
                long stored = fluidHandler.getAmountAsLong(tank);
                long capacity = fluidHandler.getCapacityAsLong(tank, currentType);
                bestRatio = Math.max(bestRatio, MathUtils.divideToLevel(stored, capacity));
            }
        }
        return getBarWidth(bestRatio);
    }

    public static boolean isEnergyBarVisible(ItemStack stack) {
        //TODO - 26.1: Re-evaluate this, we now expose the capability when stacked, so we should potentially have the energy bar display
        //If we are currently stacked, don't display the bar as it will overlap the stack count
        if (stack.count() == 1) {
            //We also don't display the bar if there is nothing stored in any of the containers
            EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(ItemAccess.forStack(stack));
            if (energyHandler != null) {
                return energyHandler.getAmountAsLong() > 0;
            }
        }
        return false;
    }

    public static int getEnergyBarWidth(ItemStack stack) {
        EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(ItemAccess.forStack(stack));
        if (energyHandler == null) {
            return 0;
        }
        return getBarWidth(MathUtils.divideToLevel(energyHandler.getAmountAsLong(), energyHandler.getCapacityAsLong()));
    }

    public static void mergeEnergyContainers(@Nullable IEnergyContainer container, @Nullable IEnergyContainer mergeContainer, TransactionContext transaction) {
        if (container == null || mergeContainer == null) {
            //Nothing to do here
            //TODO: Do we want to error if they are different nullabilities?
            return;
        }
        container.setEnergy(MathUtils.addClamped(container.getAmountAsLong(), mergeContainer.getAmountAsLong()), transaction);
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