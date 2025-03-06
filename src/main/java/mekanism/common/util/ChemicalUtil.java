package mekanism.common.util;

import java.util.Collection;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.chemical.attribute.ChemicalFuel;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.network.distribution.ChemicalHandlerTarget;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.tier.ChemicalTankTier;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * @apiNote This class is called ChemicalUtil instead of ChemicalUtils so that it does not overlap with {@link mekanism.api.chemical.ChemicalUtils}
 */
@NothingNullByDefault
public class ChemicalUtil {

    private ChemicalUtil() {
    }

    /**
     * Creates and returns a full chemical tank with the specified chemical type.
     *
     * @param chemical - chemical to fill the tank with
     *
     * @return filled chemical tank
     */
    public static ItemStack getFullChemicalTank(ChemicalTankTier tier, @NotNull Holder<Chemical> chemical) {
        return getFilledVariant(getEmptyChemicalTank(tier), chemical);
    }

    /**
     * Retrieves an empty Chemical Tank.
     *
     * @return empty chemical tank
     */
    private static Holder<Item> getEmptyChemicalTank(ChemicalTankTier tier) {
        return (switch (tier) {
            case BASIC -> MekanismBlocks.BASIC_CHEMICAL_TANK;
            case ADVANCED -> MekanismBlocks.ADVANCED_CHEMICAL_TANK;
            case ELITE -> MekanismBlocks.ELITE_CHEMICAL_TANK;
            case ULTIMATE -> MekanismBlocks.ULTIMATE_CHEMICAL_TANK;
            case CREATIVE -> MekanismBlocks.CREATIVE_CHEMICAL_TANK;
        }).getItemHolder();
    }

    public static ItemStack getFilledVariant(Holder<Item> toFill, Holder<Chemical> provider) {
        return getFilledVariant(new ItemStack(toFill), provider);
    }

    public static ItemStack getFilledVariant(ItemStack toFill, Holder<Chemical> provider) {
        IMekanismChemicalHandler attachment = ContainerType.CHEMICAL.createHandler(toFill);
        if (attachment != null) {
            for (IChemicalTank tank : attachment.getChemicalTanks(null)) {
                long amount = tank.getCapacity();
                tank.setStack(new ChemicalStack(provider, amount));
            }
        }
        //The item is now filled return it for convenience
        return toFill;
    }

    public static int getRGBDurabilityForDisplay(ItemStack stack) {
        ChemicalStack chemicalStack = StorageUtils.getFirstChemicalFromAttachment(stack);
        return chemicalStack.isEmpty() ? 0 : chemicalStack.getChemicalColorRepresentation();
    }

    public static boolean hasAnyChemical(ItemStack stack) {
        return hasChemical(stack, ConstantPredicates.alwaysTrue());
    }

    public static boolean hasChemicalOfType(ItemStack stack, Chemical type) {
        return hasChemical(stack, s -> s.is(type));
    }

    public static boolean hasChemical(ItemStack stack, Predicate<ChemicalStack> validityCheck) {
        IChemicalHandler handler = stack.getCapability(Capabilities.CHEMICAL.item());
        if (handler != null) {
            for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                ChemicalStack chemicalStack = handler.getChemicalInTank(tank);
                if (!chemicalStack.isEmpty() && validityCheck.test(chemicalStack)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void emit(Collection<BlockCapabilityCache<IChemicalHandler, @Nullable Direction>> targets, IChemicalTank tank) {
        emit(targets, tank, tank.getCapacity());
    }

    public static void emit(Collection<BlockCapabilityCache<IChemicalHandler, @Nullable Direction>> targets, IChemicalTank tank, long maxOutput) {
        if (!tank.isEmpty() && maxOutput > 0) {
            tank.extract(emit(targets, ChemicalStack.EMPTY, tank, maxOutput), Action.EXECUTE, AutomationType.INTERNAL);
        }
    }

    /**
     * Emits chemical from a central block by splitting the received stack among the sides given.
     *
     * @param targets - the list of capabilities to output to
     * @param stack   - the stack to output
     *
     * @return the amount of chemical emitted
     */
    public static long emit(Collection<BlockCapabilityCache<IChemicalHandler, @Nullable Direction>> targets, @NotNull ChemicalStack stack) {
        return emit(targets, stack, null, Long.MAX_VALUE);
    }

    private static long emit(Collection<BlockCapabilityCache<IChemicalHandler, @Nullable Direction>> targets, @NotNull ChemicalStack stack,
          @UnknownNullability IChemicalTank tank, long maxOutput) {
        if (stack.isEmpty() && tank == null) {
            //Something went wrong in calling this method
            return 0;
        } else if (targets.isEmpty()) {
            return 0;
        }
        ChemicalHandlerTarget target = null;
        for (BlockCapabilityCache<IChemicalHandler, Direction> capability : targets) {
            //Insert to access side and collect the cap if it is present, and we can insert the type of the stack into it
            IChemicalHandler handler = capability.getCapability();
            if (handler != null) {
                //If we weren't given a stack by the caller, then we want to lazily try to extract from the tank to see how much we are trying to emit
                // so that we don't have to attempt an extraction if all our targets are actually not currently fluid handlers
                if (stack.isEmpty()) {
                    stack = tank.extract(maxOutput, Action.SIMULATE, AutomationType.INTERNAL);
                    if (stack.isEmpty()) {
                        //If we failed to extract from it, just exit early
                        return 0;
                    }
                }
                if (canInsert(handler, stack)) {
                    if (target == null) {
                        target = new ChemicalHandlerTarget(targets.size());
                    }
                    target.addHandler(handler);
                }
            }
        }
        return EmitUtils.sendToAcceptors(target, stack.getAmount(), stack.copy());
    }

    public static boolean canInsert(IChemicalHandler handler, @NotNull ChemicalStack stack) {
        return handler.insertChemical(stack, Action.SIMULATE).getAmount() < stack.getAmount();
    }

    public static long hydrogenEnergyDensity() {
        ChemicalFuel fuel = MekanismChemicals.HYDROGEN.getData(IMekanismDataMapTypes.INSTANCE.chemicalFuel());
        return fuel == null ? 0 : fuel.energyDensity();
    }

    public static long hydrogenEnergyPerTick() {
        ChemicalFuel fuel = MekanismChemicals.HYDROGEN.getData(IMekanismDataMapTypes.INSTANCE.chemicalFuel());
        return fuel == null ? 0 : fuel.energyPerTick();
    }

    public static Chemical chemical(ChemicalBuilder builder, @Nullable Integer colorRepresentation) {
        if (colorRepresentation == null) {
            return new Chemical(builder);
        }
        int color = colorRepresentation;
        return new Chemical(builder) {
            @Override
            public int getColorRepresentation() {
                return color;
            }
        };
    }
}