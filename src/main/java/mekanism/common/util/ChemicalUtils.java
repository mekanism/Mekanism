package mekanism.common.util;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.math.MathUtils;
import mekanism.client.MekanismClient;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;

public class ChemicalUtils {

    private ChemicalUtils() {
    }

    public static final int DEFAULT_HYDROGEN_ENERGY_DENSITY = 2;

    //TODO - 26.2: Evaluate callers and see if we can cache any of them
    public static ChemicalResource getResource(RegistryAccess registryAccess, ResourceKey<Chemical> key) {
        return registryAccess.get(key).map(ChemicalResource::of).orElse(ChemicalResource.EMPTY);
    }

    public static boolean hasChemicalOfType(ItemAccess itemAccess, Holder<Chemical> type) {
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(itemAccess);
        return handler != null && ResourceHandlerUtil.contains(handler, ChemicalResource.of(type));
    }

    public static boolean hasChemicalOfType(ItemAccess itemAccess, ResourceKey<Chemical> type) {
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(itemAccess);
        if (handler != null) {
            for (int index = 0, size = handler.size(); index < size; index++) {
                if (handler.getResource(index).is(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    //TODO - 26.2: Re-evaluate this method and the fact it falls back due to not having a registry access
    @Deprecated
    public static int hydrogenEnergyDensity() {
        RegistryAccess registryAccess;
        if (DatagenModLoader.isRunningDataGen()) {
            return DEFAULT_HYDROGEN_ENERGY_DENSITY;
        } else if (FMLEnvironment.getDist().isClient()) {
            Level level = MekanismClient.tryGetClientWorld();
            if (level == null) {
                return DEFAULT_HYDROGEN_ENERGY_DENSITY;
            }
            registryAccess = level.registryAccess();
        } else {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) {
                return DEFAULT_HYDROGEN_ENERGY_DENSITY;
            }
            registryAccess = server.registryAccess();
        }
        return getResource(registryAccess, ChemicalIds.HYDROGEN).fuelEnergyDensity(registryAccess);
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