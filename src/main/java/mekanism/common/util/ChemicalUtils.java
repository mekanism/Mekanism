package mekanism.common.util;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.chemical.attribute.ChemicalFuel;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import net.minecraft.core.Holder;
import net.minecraft.util.ARGB;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ChemicalUtils {

    private ChemicalUtils() {
    }

    public static boolean hasChemicalOfType(ItemAccess itemAccess, Holder<Chemical> type) {
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(itemAccess);
        return handler != null && ResourceHandlerUtil.contains(handler, ChemicalResource.of(type));
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
        int color;
        if (ARGB.alpha(colorRepresentation) == 0) {
            if (FMLEnvironment.isProduction()) {
                color = ARGB.opaque(colorRepresentation);
            } else {
                throw new IllegalArgumentException("Chemical tint should now includes alpha.");
            }
        } else {
            color = colorRepresentation;
        }
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