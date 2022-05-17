package mekanism.common.util;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.MergedChemicalTank.Current;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.providers.IGasProvider;
import mekanism.api.providers.IInfuseTypeProvider;
import mekanism.api.providers.IPigmentProvider;
import mekanism.api.providers.ISlurryProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.network.distribution.ChemicalHandlerTarget;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tier.ChemicalTankTier;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;

/**
 * @apiNote This class is called ChemicalUtil instead of ChemicalUtils so that it does not overlap with {@link mekanism.api.chemical.ChemicalUtils}
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChemicalUtil {

    private ChemicalUtil() {
    }

    @SuppressWarnings("unchecked")
    public static <CHEMICAL extends Chemical<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, ?>> Capability<HANDLER> getCapabilityForChemical(CHEMICAL chemical) {
        if (chemical instanceof Gas) {
            return (Capability<HANDLER>) Capabilities.GAS_HANDLER_CAPABILITY;
        } else if (chemical instanceof InfuseType) {
            return (Capability<HANDLER>) Capabilities.INFUSION_HANDLER_CAPABILITY;
        } else if (chemical instanceof Pigment) {
            return (Capability<HANDLER>) Capabilities.PIGMENT_HANDLER_CAPABILITY;
        } else if (chemical instanceof Slurry) {
            return (Capability<HANDLER>) Capabilities.SLURRY_HANDLER_CAPABILITY;
        } else {
            throw new IllegalStateException("Unknown Chemical Type: " + chemical.getClass().getName());
        }
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> Capability<HANDLER>
    getCapabilityForChemical(STACK stack) {
        return getCapabilityForChemical(stack.getType());
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> Capability<HANDLER>
    getCapabilityForChemical(IChemicalTank<CHEMICAL, STACK> tank) {
        //Note: We just use getEmptyStack as it still has enough information
        return getCapabilityForChemical(tank.getEmptyStack());
    }

    /**
     * Gets the empty stack matching the type of the input stack type
     */
    public static ChemicalStack<?> getEmptyStack(ChemicalType chemicalType) {
        return switch (chemicalType) {
            case GAS -> GasStack.EMPTY;
            case INFUSION -> InfusionStack.EMPTY;
            case PIGMENT -> PigmentStack.EMPTY;
            case SLURRY -> SlurryStack.EMPTY;
        };
    }

    /**
     * Gets the empty stack matching the type of the input stack type
     */
    @SuppressWarnings("unchecked")
    public static <STACK extends ChemicalStack<?>> STACK getEmptyStack(STACK stack) {
        if (stack instanceof GasStack) {
            return (STACK) GasStack.EMPTY;
        } else if (stack instanceof InfusionStack) {
            return (STACK) InfusionStack.EMPTY;
        } else if (stack instanceof PigmentStack) {
            return (STACK) PigmentStack.EMPTY;
        } else if (stack instanceof SlurryStack) {
            return (STACK) SlurryStack.EMPTY;
        } else {
            throw new IllegalStateException("Unknown Chemical Type: " + stack.getType().getClass().getName());
        }
    }

    /**
     * Compares a {@link ChemicalType} with the current type of merged chemical tank.
     */
    public static boolean compareTypes(ChemicalType chemicalType, Current current) {
        return current == switch (chemicalType) {
            case GAS -> Current.GAS;
            case INFUSION -> Current.INFUSION;
            case PIGMENT -> Current.PIGMENT;
            case SLURRY -> Current.SLURRY;
        };
    }

    /**
     * Helper to copy a chemical stack when we don't know what implementation it is.
     *
     * @param stack Stack to copy
     *
     * @return Copy of the input stack with the desired size
     *
     * @apiNote Should only be called if we know that copy returns STACK
     */
    @SuppressWarnings("unchecked")
    public static <STACK extends ChemicalStack<?>> STACK copy(STACK stack) {
        return (STACK) stack.copy();
    }

    /**
     * Helper to resize a chemical stack when we don't know what implementation it is.
     *
     * @param stack  Stack to copy
     * @param amount Desired size
     *
     * @return Copy of the input stack with the desired size
     */
    public static <STACK extends ChemicalStack<?>> STACK copyWithAmount(STACK stack, long amount) {
        if (stack.isEmpty()) {
            return getEmptyStack(stack);
        }
        STACK result = copy(stack);
        result.setAmount(amount);
        return result;
    }

    /**
     * Helper to get a chemical stack of the proper type.
     *
     * @param provider Chemical Provider
     * @param amount   Desired size
     */
    @SuppressWarnings("unchecked")
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK withAmount(IChemicalProvider<CHEMICAL> provider, long amount) {
        return (STACK) provider.getStack(amount);
    }

    /**
     * Creates and returns a full chemical tank with the specified chemical type.
     *
     * @param chemical - chemical to fill the tank with
     *
     * @return filled chemical tank
     */
    public static ItemStack getFullChemicalTank(ChemicalTankTier tier, @Nonnull Chemical<?> chemical) {
        return getFilledVariant(getEmptyChemicalTank(tier), tier.getStorage(), chemical);
    }

    /**
     * Retrieves an empty Chemical Tank.
     *
     * @return empty chemical tank
     */
    private static ItemStack getEmptyChemicalTank(ChemicalTankTier tier) {
        return (switch (tier) {
            case BASIC -> MekanismBlocks.BASIC_CHEMICAL_TANK;
            case ADVANCED -> MekanismBlocks.ADVANCED_CHEMICAL_TANK;
            case ELITE -> MekanismBlocks.ELITE_CHEMICAL_TANK;
            case ULTIMATE -> MekanismBlocks.ULTIMATE_CHEMICAL_TANK;
            case CREATIVE -> MekanismBlocks.CREATIVE_CHEMICAL_TANK;
        }).getItemStack();
    }

    public static ItemStack getFilledVariant(ItemStack toFill, long capacity, IChemicalProvider<?> provider) {
        if (provider instanceof IGasProvider gasProvider) {
            return getFilledVariant(toFill, ChemicalTankBuilder.GAS, capacity, gasProvider, NBTConstants.GAS_TANKS);
        } else if (provider instanceof IInfuseTypeProvider infuseTypeProvider) {
            return getFilledVariant(toFill, ChemicalTankBuilder.INFUSION, capacity, infuseTypeProvider, NBTConstants.INFUSION_TANKS);
        } else if (provider instanceof IPigmentProvider pigmentProvider) {
            return getFilledVariant(toFill, ChemicalTankBuilder.PIGMENT, capacity, pigmentProvider, NBTConstants.PIGMENT_TANKS);
        } else if (provider instanceof ISlurryProvider slurryProvider) {
            return getFilledVariant(toFill, ChemicalTankBuilder.SLURRY, capacity, slurryProvider, NBTConstants.SLURRY_TANKS);
        } else {
            throw new IllegalStateException("Unknown Chemical Type: " + provider.getChemical().getClass().getName());
        }
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
    ItemStack getFilledVariant(ItemStack toFill, ChemicalTankBuilder<CHEMICAL, STACK, TANK> tankBuilder, long capacity, IChemicalProvider<CHEMICAL> provider, String key) {
        TANK dummyTank = tankBuilder.createDummy(capacity);
        //Manually handle filling it as capabilities are not necessarily loaded yet (at least not on the first call to this, which is made via fillItemGroup)
        dummyTank.setStack(withAmount(provider, dummyTank.getCapacity()));
        ItemDataUtils.writeContainers(toFill, key, Collections.singletonList(dummyTank));
        //The item is now filled return it for convenience
        return toFill;
    }

    public static int getRGBDurabilityForDisplay(ItemStack stack) {
        GasStack gasStack = StorageUtils.getStoredGasFromNBT(stack);
        if (!gasStack.isEmpty()) {
            return gasStack.getChemicalColorRepresentation();
        }
        InfusionStack infusionStack = StorageUtils.getStoredInfusionFromNBT(stack);
        if (!infusionStack.isEmpty()) {
            return infusionStack.getChemicalColorRepresentation();
        }
        PigmentStack pigmentStack = StorageUtils.getStoredPigmentFromNBT(stack);
        if (!pigmentStack.isEmpty()) {
            return pigmentStack.getChemicalColorRepresentation();
        }
        SlurryStack slurryStack = StorageUtils.getStoredSlurryFromNBT(stack);
        if (!slurryStack.isEmpty()) {
            return slurryStack.getChemicalColorRepresentation();
        }
        return 0;
    }

    public static boolean hasGas(ItemStack stack) {
        return hasChemical(stack, s -> true, Capabilities.GAS_HANDLER_CAPABILITY);
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean hasChemical(ItemStack stack, CHEMICAL type) {
        Capability<IChemicalHandler<CHEMICAL, STACK>> capability = getCapabilityForChemical(type);
        return hasChemical(stack, s -> s.isTypeEqual(type), capability);
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> boolean hasChemical(
          ItemStack stack, Predicate<STACK> validityCheck, Capability<HANDLER> capability) {
        Optional<HANDLER> cap = stack.getCapability(capability).resolve();
        if (cap.isPresent()) {
            HANDLER handler = cap.get();
            for (int tank = 0; tank < handler.getTanks(); tank++) {
                STACK chemicalStack = handler.getChemicalInTank(tank);
                if (!chemicalStack.isEmpty() && validityCheck.test(chemicalStack)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void addAttributeTooltips(List<Component> tooltips, Chemical<?> chemical) {
        chemical.getAttributes().forEach(attr -> attr.addTooltipText(tooltips));
    }

    public static void addChemicalDataToTooltip(List<Component> tooltips, Chemical<?> chemical, boolean advanced) {
        if (!chemical.isEmptyType()) {
            addAttributeTooltips(tooltips, chemical);
            if (chemical instanceof Gas gas && MekanismTags.Gases.WASTE_BARREL_DECAY_LOOKUP.contains(gas)) {
                tooltips.add(MekanismLang.DECAY_IMMUNE.translateColored(EnumColor.AQUA));
            }
            if (advanced) {
                //If advanced tooltips are on, display the registry name
                tooltips.add(TextComponentUtil.build(ChatFormatting.DARK_GRAY, chemical.getRegistryName()));
            }
        }
    }

    public static void emit(IChemicalTank<?, ?> tank, BlockEntity from) {
        emit(EnumSet.allOf(Direction.class), tank, from);
    }

    public static void emit(Set<Direction> outputSides, IChemicalTank<?, ?> tank, BlockEntity from) {
        emit(outputSides, tank, from, tank.getCapacity());
    }

    public static void emit(Set<Direction> outputSides, IChemicalTank<?, ?> tank, BlockEntity from, long maxOutput) {
        if (!tank.isEmpty() && maxOutput > 0) {
            tank.extract(emit(outputSides, tank.extract(maxOutput, Action.SIMULATE, AutomationType.INTERNAL), from), Action.EXECUTE, AutomationType.INTERNAL);
        }
    }

    /**
     * Emits chemical from a central block by splitting the received stack among the sides given.
     *
     * @param sides - the list of sides to output from
     * @param stack - the stack to output
     * @param from  - the TileEntity to output from
     *
     * @return the amount of chemical emitted
     */
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> long emit(Set<Direction> sides, @Nonnull STACK stack, BlockEntity from) {
        if (stack.isEmpty() || sides.isEmpty()) {
            return 0;
        }
        Capability<IChemicalHandler<CHEMICAL, STACK>> capability = getCapabilityForChemical(stack);
        ChemicalHandlerTarget<CHEMICAL, STACK, IChemicalHandler<CHEMICAL, STACK>> target = new ChemicalHandlerTarget<>(stack, 6);
        EmitUtils.forEachSide(from.getLevel(), from.getBlockPos(), sides, (acceptor, side) -> {
            //Insert to access side and collect the cap if it is present, and we can insert the type of the stack into it
            CapabilityUtils.getCapability(acceptor, capability, side.getOpposite()).ifPresent(handler -> {
                if (canInsert(handler, stack)) {
                    target.addHandler(handler);
                }
            });
        });
        if (target.getHandlerCount() > 0) {
            return EmitUtils.sendToAcceptors(target, stack.getAmount(), ChemicalUtil.copy(stack));
        }
        return 0;
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> boolean canInsert(
          HANDLER handler, @Nonnull STACK stack) {
        return handler.insertChemical(stack, Action.SIMULATE).getAmount() < stack.getAmount();
    }
}