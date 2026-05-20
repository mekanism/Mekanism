package mekanism.common.util;

import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.chemical.attribute.ChemicalFuel;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.containers.ComponentBackedResourceHandler;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.tier.ChemicalTankTier;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ChemicalUtils {

    private ChemicalUtils() {
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
        ComponentBackedResourceHandler<ChemicalResource, IChemicalTank> attachment = ContainerType.CHEMICAL.createHandler(toFill);
        if (attachment != null) {
            ChemicalResource chemicalType = ChemicalResource.of(provider);
            for (IChemicalTank tank : attachment.getContainers()) {
                tank.setContents(chemicalType, tank.capacityAsLong(chemicalType), null);
            }
        }
        //The item is now filled return it for convenience
        return toFill;
    }

    public static int getRGBDurabilityForDisplay(ItemStack stack) {
        ChemicalResource chemicalType = StorageUtils.getFirstChemicalFromAttachment(stack);
        return chemicalType.isEmpty() ? 0 : chemicalType.getChemicalColorRepresentation();
    }

    public static boolean hasAnyChemical(ItemStack stack) {
        return hasChemical(stack, ConstantPredicates.alwaysTrue());
    }

    public static boolean hasChemicalOfType(ItemStack stack, Holder<Chemical> type) {
        ChemicalResource chemicalType = ChemicalResource.of(type);
        return hasChemical(stack, chemicalType::equals);
    }

    public static boolean hasChemical(ItemStack stack, Predicate<ChemicalResource> validityCheck) {
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(ItemAccess.forStack(stack));
        if (handler != null) {
            for (int tank = 0, size = handler.size(); tank < size; tank++) {
                ChemicalResource chemicalType = handler.getResource(tank);
                if (!chemicalType.isEmpty() && validityCheck.test(chemicalType)) {
                    return true;
                }
            }
        }
        return false;
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