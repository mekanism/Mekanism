package mekanism.common.config;

import java.util.EnumMap;
import mekanism.common.Tier;

/**
 * Created by Thiakil on 16/03/2019.
 */
public class TierConfig {

    public final DoubleOption EnergyCubeMaxEnergy;
    public final DoubleOption EnergyCubeOutput;
    public final DoubleOption InductionCellMaxEnergy;
    public final DoubleOption InductionProviderOutput;
    public final IntOption CableCapacity;
    public final IntOption PipeCapacity;
    public final IntOption PipePullAmount;
    public final IntOption TubeCapacity;
    public final IntOption TubePullAmount;
    public final IntOption TransporterPullAmount;
    public final IntOption TransporterSpeed;
    public final DoubleOption ConductorInverseConduction;
    public final DoubleOption ConductorHeatCapacity;
    public final DoubleOption ConductorConductionInsulation;
    public final IntOption FluidTankStorage;
    public final IntOption FluidTankOutput;
    public final IntOption GasTankStorage;
    public final IntOption GasTankOutput;
    public final IntOption BinStorage;
    private TierConfig(BaseConfig baseConfig, Tier.BaseTier tier) {
        EnergyCubeMaxEnergy = new DoubleOption(baseConfig, "tier", tier.getSimpleName() + "EnergyCubeMaxEnergy",
              Tier.EnergyCubeTier.values()[tier.ordinal()].baseMaxEnergy);
        EnergyCubeOutput = new DoubleOption(baseConfig, "tier", tier.getSimpleName() + "EnergyCubeOutput",
              Tier.EnergyCubeTier.values()[tier.ordinal()].baseOutput);
        FluidTankStorage = new IntOption(baseConfig, "tier", tier.getSimpleName() + "FluidTankStorage",
              Tier.FluidTankTier.values()[tier.ordinal()].baseStorage);
        FluidTankOutput = new IntOption(baseConfig, "tier", tier.getSimpleName() + "FluidTankOutput",
              Tier.FluidTankTier.values()[tier.ordinal()].baseOutput);
        GasTankStorage = new IntOption(baseConfig, "tier", tier.getSimpleName() + "GasTankStorage",
              Tier.GasTankTier.values()[tier.ordinal()].baseStorage);
        GasTankOutput = new IntOption(baseConfig, "tier", tier.getSimpleName() + "GasTankOutput",
              Tier.GasTankTier.values()[tier.ordinal()].baseOutput);
        BinStorage = new IntOption(baseConfig, "tier", tier.getSimpleName() + "BinStorage",
              Tier.BinTier.values()[tier.ordinal()].baseStorage);
        if (tier != Tier.BaseTier.CREATIVE) {
            InductionCellMaxEnergy = new DoubleOption(baseConfig, "tier",
                  tier.getSimpleName() + "InductionCellMaxEnergy",
                  Tier.InductionCellTier.values()[tier.ordinal()].baseMaxEnergy);
            InductionProviderOutput = new DoubleOption(baseConfig, "tier",
                  tier.getSimpleName() + "InductionProviderOutput",
                  Tier.InductionProviderTier.values()[tier.ordinal()].baseOutput);
            CableCapacity = new IntOption(baseConfig, "tier", tier.getSimpleName() + "CableCapacity",
                  Tier.CableTier.values()[tier.ordinal()].baseCapacity);
            PipeCapacity = new IntOption(baseConfig, "tier", tier.getSimpleName() + "PipeCapacity",
                  Tier.PipeTier.values()[tier.ordinal()].baseCapacity);
            PipePullAmount = new IntOption(baseConfig, "tier", tier.getSimpleName() + "PipePullAmount",
                  Tier.PipeTier.values()[tier.ordinal()].basePull);
            TubeCapacity = new IntOption(baseConfig, "tier", tier.getSimpleName() + "TubeCapacity",
                  Tier.TubeTier.values()[tier.ordinal()].baseCapacity);
            TubePullAmount = new IntOption(baseConfig, "tier", tier.getSimpleName() + "TubePullAmount",
                  Tier.TubeTier.values()[tier.ordinal()].basePull);
            TransporterPullAmount = new IntOption(baseConfig, "tier", tier.getSimpleName() + "TransporterPullAmount",
                  Tier.TransporterTier.values()[tier.ordinal()].basePull);
            TransporterSpeed = new IntOption(baseConfig, "tier", tier.getSimpleName() + "TransporterSpeed",
                  Tier.TransporterTier.values()[tier.ordinal()].baseSpeed);
            ConductorInverseConduction = new DoubleOption(baseConfig, "tier",
                  tier.getSimpleName() + "ConductorInverseConduction",
                  Tier.ConductorTier.values()[tier.ordinal()].baseConduction);
            ConductorHeatCapacity = new DoubleOption(baseConfig, "tier", tier.getSimpleName() + "ConductorHeatCapacity",
                  Tier.ConductorTier.values()[tier.ordinal()].baseHeatCapacity);
            ConductorConductionInsulation = new DoubleOption(baseConfig, "tier",
                  tier.getSimpleName() + "ConductorConductionInsulation",
                  Tier.ConductorTier.values()[tier.ordinal()].baseConductionInsulation);

        } else {
            InductionCellMaxEnergy = null;
            InductionProviderOutput = null;
            CableCapacity = null;
            PipeCapacity = null;
            PipePullAmount = null;
            TubeCapacity = null;
            TubePullAmount = null;
            TransporterPullAmount = null;
            TransporterSpeed = null;
            ConductorInverseConduction = null;
            ConductorHeatCapacity = null;
            ConductorConductionInsulation = null;
        }
    }

    public static EnumMap<Tier.BaseTier, TierConfig> create(BaseConfig baseConfig) {
        EnumMap<Tier.BaseTier, TierConfig> map = new EnumMap<>(Tier.BaseTier.class);

        for (Tier.BaseTier baseTier : Tier.BaseTier.values()) {
            map.put(baseTier, new TierConfig(baseConfig, baseTier));
        }

        return map;
    }
}
