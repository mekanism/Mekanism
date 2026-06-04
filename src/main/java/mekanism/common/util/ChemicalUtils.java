package mekanism.common.util;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.chemical.attribute.ChemicalFuel;
import mekanism.api.math.MathUtils;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
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
    public static ItemStack getFullChemicalTank(ChemicalTankTier tier, Holder<Chemical> chemical, @Nullable TransactionContext transaction) {
        return getFilledVariant(getEmptyChemicalTank(tier), chemical, transaction);
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

    public static ItemStack getFilledVariant(Holder<Item> toFill, Holder<Chemical> chemical, @Nullable TransactionContext transaction) {
        ItemAccess itemAccess = ItemAccessUtils.queryOnlyAccess(ItemResource.of(toFill));
        return ContainerType.CHEMICAL.getFilledVariant(itemAccess, ChemicalResource.of(chemical), transaction);
    }

    public static int getRGBDurabilityForDisplay(ItemAccess itemAccess) {
        ChemicalResource chemicalType = ContainerType.CHEMICAL.getFirstResourceFromAttachment(itemAccess);
        return chemicalType.isEmpty() ? 0 : chemicalType.getChemicalColorRepresentation();
    }

    public static boolean hasChemicalOfType(TypedInstance<Item> stack, Holder<Chemical> type) {
        return hasChemicalOfType(ItemAccessUtils.queryOnlyAccess(stack), type);
    }

    public static boolean hasChemicalOfType(ItemAccess itemAccess, Holder<Chemical> type) {
        ChemicalResource typeToCheck = ChemicalResource.of(type);
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(itemAccess);
        if (handler != null) {
            for (int tank = 0, size = handler.size(); tank < size; tank++) {
                ChemicalResource chemicalType = handler.getResource(tank);
                if (!chemicalType.isEmpty() && chemicalType.equals(typeToCheck)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int hydrogenEnergyDensity() {
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

    public static void dump(IChemicalTank chemicalTank, GasMode dumpMode, long dumpingAmount) {
        dump(chemicalTank, dumpMode, dumpingAmount, dumpingAmount);
    }

    public static void dump(IChemicalTank chemicalTank, GasMode dumpMode, long dumpingAmount, long dumpExcessRate) {
        if (dumpMode == GasMode.IDLE || chemicalTank.isEmpty()) {
            return;
        }
        ChemicalResource chemicalType = chemicalTank.resource();
        long amount = chemicalTank.amountAsLong();
        long toDump = 0;
        if (dumpMode == GasMode.DUMPING) {
            toDump = dumpingAmount;
        } else {//DUMPING_EXCESS
            //Don't allow dumping more than the configured amount
            long targetLevel = MathUtils.clampToLong(chemicalTank.capacityAsLong(chemicalType) * MekanismConfig.general.dumpExcessKeepRatio.get());
            if (targetLevel < amount) {
                toDump = Math.min(amount - targetLevel, dumpExcessRate);
            }
        }
        if (toDump > 0) {
            chemicalTank.setContents(chemicalType, amount - toDump, null);
        }
    }
}