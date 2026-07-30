package mekanism.common.util;

import java.util.List;
import java.util.function.Consumer;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.tier.IStorageTier;
import mekanism.common.util.text.TextUtils;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.resource.RegisteredResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class StorageUtils {//TODO - 26.2: Re-evaluate which of these methods are the same and can be deduplicated and moved to ResourceUtils or the corresponding container type

    private StorageUtils() {
    }

    public static void addCapacity(Consumer<Component> builder, IStorageTier tier, ILangEntry capacityLang) {
        if (tier.isCreative()) {
            builder.accept(MekanismLang.CAPACITY.translateColored(tier.getTextColor(), EnumColor.GRAY, MekanismLang.INFINITE));
        } else {
            builder.accept(capacityLang.translateColored(tier.getTextColor(), EnumColor.GRAY, TextUtils.format(tier.getCapacity())));
        }
    }

    public static <TYPE, RESOURCE extends RegisteredResource<TYPE>> long getContainedResource(@Nullable ResourceHandler<RESOURCE> handler, ResourceKey<TYPE> type) {
        if (handler != null) {
            for (int tank = 0, tanks = handler.size(); tank < tanks; tank++) {
                RESOURCE resource = handler.getResource(tank);
                if (resource.is(type)) {
                    return handler.getAmountAsLong(tank);
                }
            }
        }
        return 0;
    }

    public static double getEnergyRatio(TypedInstance<Item> stack) {
        return getEnergyRatio(ItemAccessUtils.sideEffectFreeAccess(stack));
    }

    public static double getEnergyRatio(ItemAccess itemAccess) {
        EnergyHandler handler = Capabilities.ENERGY.getCapability(itemAccess);
        return handler == null ? 0 : ContainerType.ENERGY.divideToLevel(handler);
    }

    //TODO - 26.2: Should this method be used anywhere? Or is coloring happening elsewhere
    public static Component getEnergyPercent(TypedInstance<Item> stack, boolean colorText) {
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
        return Math.clamp(Math.round(Item.MAX_BAR_WIDTH * (float) ratio), 0, Item.MAX_BAR_WIDTH);
    }

    public static boolean isBarVisible(ItemStack stack) {
        //TODO - 26.2: Re-evaluate this, we now expose the capability when stacked, so we should potentially have the energy bar display
        //If we are currently stacked, don't display the bar as it will overlap the stack count
        if (stack.count() == 1) {
            //We also don't display the bar if there is nothing stored in any of the containers
            ItemAccess itemAccess = ItemAccessUtils.sideEffectFreeAccess(stack);
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
        ItemAccess itemAccess = ItemAccessUtils.sideEffectFreeAccess(stack);
        return getBarWidth(Math.max(getBarRatio(Capabilities.CHEMICAL.getCapability(itemAccess)), getBarRatio(Capabilities.FLUID.getCapability(itemAccess))));
    }

    private static <RESOURCE extends Resource> double getBarRatio(@Nullable ResourceHandler<RESOURCE> handler) {
        double bestRatio = 0;
        if (handler != null) {
            for (int tank = 0, tanks = handler.size(); tank < tanks; tank++) {
                RESOURCE currentType = handler.getResource(tank);
                long stored = handler.getAmountAsLong(tank);
                long capacity = handler.getCapacityAsLong(tank, currentType);
                bestRatio = Math.max(bestRatio, MathUtils.divideToLevel(stored, capacity));
            }
        }
        return bestRatio;
    }

    public static boolean isEnergyBarVisible(ItemStack stack) {
        //TODO - 26.2: Re-evaluate this, we now expose the capability when stacked, so we should potentially have the energy bar display
        //If we are currently stacked, don't display the bar as it will overlap the stack count
        if (stack.count() == 1) {
            //We also don't display the bar if there is nothing stored in any of the containers
            EnergyHandler energyHandler = Capabilities.ENERGY.getQueryOnlyCapability(stack);
            if (energyHandler != null) {
                return energyHandler.getAmountAsLong() > 0;
            }
        }
        return false;
    }

    public static int getEnergyBarWidth(ItemStack stack) {
        return getBarWidth(getEnergyRatio(stack));
    }

    public static void mergeEnergyContainers(@Nullable IEnergyContainer container, @Nullable IEnergyContainer mergeContainer, TransactionContext transaction) {
        if (container == null || mergeContainer == null) {
            //Nothing to do here
            //TODO: Do we want to error if they are different nullabilities?
            return;
        }
        container.setEnergy(MathUtils.addClamped(container.getAmountAsLong(), mergeContainer.getAmountAsLong()), transaction);
    }

    public static void mergeHeatCapacitors(@Nullable IHeatCapacitor capacitor, @Nullable IHeatCapacitor mergeCapacitor, TransactionContext transaction) {
        if (capacitor == null || mergeCapacitor == null) {
            //Nothing to do here
            //TODO: Do we want to error if they are different nullabilities?
            return;
        }
        capacitor.setHeatAndCapacity(capacitor.getHeat() + mergeCapacitor.getHeat(), capacitor.getHeatCapacity() + mergeCapacitor.getHeatCapacity(), transaction);
    }

    public static <T> void validateSizeMatches(List<T> base, List<T> toAdd, String type) {
        if (base.size() != toAdd.size()) {
            throw new IllegalArgumentException("Mismatched " + type + " count, orig: " + base.size() + ", toAdd: " + toAdd.size());
        }
    }
}