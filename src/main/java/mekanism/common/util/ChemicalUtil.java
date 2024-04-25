package mekanism.common.util;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasBuilder;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfuseTypeBuilder;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.MergedChemicalTank.Current;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentBuilder;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryBuilder;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.providers.IGasProvider;
import mekanism.api.providers.IInfuseTypeProvider;
import mekanism.api.providers.IPigmentProvider;
import mekanism.api.providers.ISlurryProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.AttachedChemicalTanks;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.content.network.distribution.ChemicalHandlerTarget;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tier.ChemicalTankTier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @apiNote This class is called ChemicalUtil instead of ChemicalUtils so that it does not overlap with {@link mekanism.api.chemical.ChemicalUtils}
 */
@NothingNullByDefault
public class ChemicalUtil {

    private ChemicalUtil() {
    }

    public static MultiTypeCapability<? extends IChemicalHandler<?, ?>> getCapabilityForChemical(ChemicalType chemicalType) {
        return switch (chemicalType) {
            case GAS -> Capabilities.GAS;
            case INFUSION -> Capabilities.INFUSION;
            case PIGMENT -> Capabilities.PIGMENT;
            case SLURRY -> Capabilities.SLURRY;
        };
    }

    @SuppressWarnings("unchecked")
    public static <CHEMICAL extends Chemical<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, ?>> MultiTypeCapability<HANDLER> getCapabilityForChemical(CHEMICAL chemical) {
        return (MultiTypeCapability<HANDLER>) getCapabilityForChemical(ChemicalType.getTypeFor(chemical));
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>>
    MultiTypeCapability<HANDLER> getCapabilityForChemical(STACK stack) {
        return getCapabilityForChemical(stack.getChemical());
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>>
    MultiTypeCapability<HANDLER> getCapabilityForChemical(IChemicalTank<CHEMICAL, STACK> tank) {
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
            throw new IllegalStateException("Unknown Chemical Type: " + stack.getChemical().getClass().getName());
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
    @SuppressWarnings("unchecked")
    public static <STACK extends ChemicalStack<?>> STACK copyWithAmount(STACK stack, long amount) {
        if (stack.isEmpty() || amount <= 0) {
            return getEmptyStack(stack);
        }
        return (STACK) stack.copyWithAmount(amount);
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
    public static ItemStack getFullChemicalTank(ChemicalTankTier tier, @NotNull Chemical<?> chemical) {
        return getFilledVariant(getEmptyChemicalTank(tier), chemical);
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

    public static ItemStack getFilledVariant(ItemStack toFill, IChemicalProvider<?> provider) {
        if (provider instanceof IGasProvider gasProvider) {
            return getFilledVariant(toFill, gasProvider, ContainerType.GAS);
        } else if (provider instanceof IInfuseTypeProvider infuseTypeProvider) {
            return getFilledVariant(toFill, infuseTypeProvider, ContainerType.INFUSION);
        } else if (provider instanceof IPigmentProvider pigmentProvider) {
            return getFilledVariant(toFill, pigmentProvider, ContainerType.PIGMENT);
        } else if (provider instanceof ISlurryProvider slurryProvider) {
            return getFilledVariant(toFill, slurryProvider, ContainerType.SLURRY);
        } else {
            throw new IllegalStateException("Unknown Chemical Type: " + provider.getChemical().getClass().getName());
        }
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> ItemStack getFilledVariant(
          ItemStack toFill, IChemicalProvider<CHEMICAL> provider, ContainerType<TANK, ? extends AttachedChemicalTanks<CHEMICAL, STACK, TANK>, ?> containerType) {
        AttachedChemicalTanks<CHEMICAL, STACK, TANK> attachment = containerType.getAttachment(toFill);
        if (attachment != null) {
            for (TANK tank : attachment.getChemicalTanks(null)) {
                tank.setStack(withAmount(provider, tank.getCapacity()));
            }
        }
        //The item is now filled return it for convenience
        return toFill;
    }

    public static int getRGBDurabilityForDisplay(ItemStack stack) {
        GasStack gasStack = StorageUtils.getStoredGasFromAttachment(stack);
        if (!gasStack.isEmpty()) {
            return gasStack.getChemicalColorRepresentation();
        }
        InfusionStack infusionStack = StorageUtils.getStoredInfusionFromAttachment(stack);
        if (!infusionStack.isEmpty()) {
            return infusionStack.getChemicalColorRepresentation();
        }
        PigmentStack pigmentStack = StorageUtils.getStoredPigmentFromAttachment(stack);
        if (!pigmentStack.isEmpty()) {
            return pigmentStack.getChemicalColorRepresentation();
        }
        SlurryStack slurryStack = StorageUtils.getStoredSlurryFromAttachment(stack);
        if (!slurryStack.isEmpty()) {
            return slurryStack.getChemicalColorRepresentation();
        }
        return 0;
    }

    public static boolean hasGas(ItemStack stack) {
        return hasChemical(stack, ConstantPredicates.alwaysTrue(), Capabilities.GAS.item());
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean hasChemical(ItemStack stack, CHEMICAL type) {
        MultiTypeCapability<IChemicalHandler<CHEMICAL, STACK>> capability = getCapabilityForChemical(type);
        return hasChemical(stack, s -> s.is(type), capability.item());
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> boolean hasChemical(
          ItemStack stack, Predicate<STACK> validityCheck, ItemCapability<HANDLER, Void> capability) {
        HANDLER handler = stack.getCapability(capability);
        if (handler != null) {
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
        for (ChemicalAttribute attr : chemical.getAttributes()) {
            attr.addTooltipText(tooltips);
        }
    }

    public static void addChemicalDataToTooltip(List<Component> tooltips, Chemical<?> chemical, boolean advanced) {
        if (!chemical.isEmptyType()) {
            addAttributeTooltips(tooltips, chemical);
            if (chemical instanceof Gas gas && gas.is(MekanismTags.Gases.WASTE_BARREL_DECAY_BLACKLIST)) {
                tooltips.add(MekanismLang.DECAY_IMMUNE.translateColored(EnumColor.AQUA));
            }
            if (advanced) {
                //If advanced tooltips are on, display the registry name
                tooltips.add(TextComponentUtil.build(ChatFormatting.DARK_GRAY, chemical.getRegistryName()));
            }
        }
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> void emit(Collection<BlockCapabilityCache<HANDLER, @Nullable Direction>> targets,
          IChemicalTank<CHEMICAL, STACK> tank) {
        emit(targets, tank, tank.getCapacity());
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> void emit(Collection<BlockCapabilityCache<HANDLER, @Nullable Direction>> targets,
          IChemicalTank<CHEMICAL, STACK> tank, long maxOutput) {
        if (!tank.isEmpty() && maxOutput > 0) {
            tank.extract(emit(targets, tank.extract(maxOutput, Action.SIMULATE, AutomationType.INTERNAL)), Action.EXECUTE, AutomationType.INTERNAL);
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
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> long emit(
          Collection<BlockCapabilityCache<HANDLER, @Nullable Direction>> targets, @NotNull STACK stack) {
        if (stack.isEmpty() || targets.isEmpty()) {
            return 0;
        }
        ChemicalHandlerTarget<CHEMICAL, STACK, HANDLER> target = new ChemicalHandlerTarget<>(stack, targets.size());
        for (BlockCapabilityCache<HANDLER, Direction> capability : targets) {
            //Insert to access side and collect the cap if it is present, and we can insert the type of the stack into it
            HANDLER handler = capability.getCapability();
            if (handler != null && canInsert(handler, stack)) {
                target.addHandler(handler);
            }
        }
        if (target.getHandlerCount() > 0) {
            return EmitUtils.sendToAcceptors(target, stack.getAmount(), ChemicalUtil.copy(stack));
        }
        return 0;
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> boolean canInsert(
          HANDLER handler, @NotNull STACK stack) {
        return handler.insertChemical(stack, Action.SIMULATE).getAmount() < stack.getAmount();
    }

    public static Gas gas(GasBuilder builder, @Nullable Integer colorRepresentation) {
        if (colorRepresentation == null) {
            return new Gas(builder);
        }
        int color = colorRepresentation;
        return new Gas(builder) {
            @Override
            public int getColorRepresentation() {
                return color;
            }
        };
    }

    public static InfuseType infuseType(InfuseTypeBuilder builder, @Nullable Integer colorRepresentation) {
        if (colorRepresentation == null) {
            return new InfuseType(builder);
        }
        int color = colorRepresentation;
        return new InfuseType(builder) {
            @Override
            public int getColorRepresentation() {
                return color;
            }
        };
    }

    public static Pigment pigment(PigmentBuilder builder, @Nullable Integer colorRepresentation) {
        if (colorRepresentation == null) {
            return new Pigment(builder);
        }
        int color = colorRepresentation;
        return new Pigment(builder) {
            @Override
            public int getColorRepresentation() {
                return color;
            }
        };
    }

    public static Slurry slurry(SlurryBuilder builder, @Nullable Integer colorRepresentation) {
        if (colorRepresentation == null) {
            return new Slurry(builder);
        }
        int color = colorRepresentation;
        return new Slurry(builder) {
            @Override
            public int getColorRepresentation() {
                return color;
            }
        };
    }
}