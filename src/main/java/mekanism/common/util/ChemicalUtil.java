package mekanism.common.util;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.MekanismAPITags;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.network.distribution.ChemicalHandlerTarget;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.ChemicalTankTier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public static ItemStack getFullChemicalTank(ChemicalTankTier tier, @NotNull Chemical chemical) {
        return getFilledVariant(getEmptyChemicalTank(tier), chemical);
    }

    /**
     * Retrieves an empty Chemical Tank.
     *
     * @return empty chemical tank
     */
    private static ItemLike getEmptyChemicalTank(ChemicalTankTier tier) {
        return switch (tier) {
            case BASIC -> MekanismBlocks.BASIC_CHEMICAL_TANK;
            case ADVANCED -> MekanismBlocks.ADVANCED_CHEMICAL_TANK;
            case ELITE -> MekanismBlocks.ELITE_CHEMICAL_TANK;
            case ULTIMATE -> MekanismBlocks.ULTIMATE_CHEMICAL_TANK;
            case CREATIVE -> MekanismBlocks.CREATIVE_CHEMICAL_TANK;
        };
    }

    public static ItemStack getFilledVariant(ItemLike toFill, IChemicalProvider provider) {
        return getFilledVariant(new ItemStack(toFill), provider);
    }

    public static ItemStack getFilledVariant(ItemStack toFill, IChemicalProvider provider) {
        IMekanismChemicalHandler attachment = ContainerType.CHEMICAL.createHandler(toFill);
        if (attachment != null) {
            for (IChemicalTank tank : attachment.getChemicalTanks(null)) {
                long amount = tank.getCapacity();
                tank.setStack(provider.getStack(amount));
            }
        }
        //The item is now filled return it for convenience
        return toFill;
    }

    public static int getRGBDurabilityForDisplay(ItemStack stack) {
        ChemicalStack chemicalStack = StorageUtils.getStoredChemicalFromAttachment(stack);
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

    public static void addAttributeTooltips(List<Component> tooltips, Chemical chemical) {
        for (ChemicalAttribute attr : chemical.getAttributes()) {
            attr.addTooltipText(tooltips);
        }
    }

    public static void addChemicalDataToTooltip(List<Component> tooltips, Chemical chemical, boolean advanced) {
        if (!chemical.isEmptyType()) {
            addAttributeTooltips(tooltips, chemical);
            if (chemical.is(MekanismAPITags.Chemicals.WASTE_BARREL_DECAY_BLACKLIST)) {
                tooltips.add(MekanismLang.DECAY_IMMUNE.translateColored(EnumColor.AQUA));
            }
            if (advanced) {
                //If advanced tooltips are on, display the registry name
                tooltips.add(TextComponentUtil.build(ChatFormatting.DARK_GRAY, chemical.getRegistryName()));
            }
        }
    }

    public static void emit(Collection<BlockCapabilityCache<IChemicalHandler, @Nullable Direction>> targets, IChemicalTank tank) {
        emit(targets, tank, tank.getCapacity());
    }

    public static void emit(Collection<BlockCapabilityCache<IChemicalHandler, @Nullable Direction>> targets, IChemicalTank tank, long maxOutput) {
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
    public static long emit(Collection<BlockCapabilityCache<IChemicalHandler, @Nullable Direction>> targets, @NotNull ChemicalStack stack) {
        if (stack.isEmpty() || targets.isEmpty()) {
            return 0;
        }
        ChemicalHandlerTarget target = new ChemicalHandlerTarget(stack, targets.size());
        for (BlockCapabilityCache<IChemicalHandler, Direction> capability : targets) {
            //Insert to access side and collect the cap if it is present, and we can insert the type of the stack into it
            IChemicalHandler handler = capability.getCapability();
            if (handler != null && canInsert(handler, stack)) {
                target.addHandler(handler);
            }
        }
        if (target.getHandlerCount() > 0) {
            return EmitUtils.sendToAcceptors(target, stack.getAmount(), stack.copy());
        }
        return 0;
    }

    public static boolean canInsert(IChemicalHandler handler, @NotNull ChemicalStack stack) {
        return handler.insertChemical(stack, Action.SIMULATE).getAmount() < stack.getAmount();
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