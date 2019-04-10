package mekanism.common.config;

import java.util.EnumMap;
import mekanism.common.Tier;
import mekanism.common.config.options.DoubleOption;
import mekanism.common.config.options.IntOption;

/**
 * Created by Thiakil on 16/03/2019.
 */
public class TierConfig {

    public static EnumMap<Tier.BaseTier, TierConfig> create(BaseConfig baseConfig) {
        EnumMap<Tier.BaseTier, TierConfig> map = new EnumMap<>(Tier.BaseTier.class);

        for (Tier.BaseTier baseTier : Tier.BaseTier.values()) {
            map.put(baseTier, new TierConfig(baseConfig, baseTier));
        }

        return map;
    }

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
        String name = tier.getSimpleName();
        EnergyCubeMaxEnergy = new DoubleOption(baseConfig, "tier", name + "EnergyCubeMaxEnergy",
              Tier.EnergyCubeTier.values()[tier.ordinal()].getBaseMaxEnergy(),
              "Maximum number of Joules a " + name + " energy cube can store.");
        EnergyCubeOutput = new DoubleOption(baseConfig, "tier", name + "EnergyCubeOutput",
              Tier.EnergyCubeTier.values()[tier.ordinal()].getBaseOutput(),
              "Output rate in Joules of a " + name + " energy cube.");
        FluidTankStorage = new IntOption(baseConfig, "tier", name + "FluidTankStorage",
              Tier.FluidTankTier.values()[tier.ordinal()].getBaseStorage(),
              "Storage size of " + name + " gas tank in mB.");
        FluidTankOutput = new IntOption(baseConfig, "tier", name + "FluidTankOutput",
              Tier.FluidTankTier.values()[tier.ordinal()].getBaseOutput(),
              "Output rate of " + name + " gas tank in mB.");
        GasTankStorage = new IntOption(baseConfig, "tier", name + "GasTankStorage",
              Tier.GasTankTier.values()[tier.ordinal()].getBaseStorage(),
              "Storage size of " + name + " gas tank in mB.");
        GasTankOutput = new IntOption(baseConfig, "tier", name + "GasTankOutput",
              Tier.GasTankTier.values()[tier.ordinal()].getBaseOutput(), "Output rate of " + name + " gas tank in mB.");
        BinStorage = new IntOption(baseConfig, "tier", name + "BinStorage",
              Tier.BinTier.values()[tier.ordinal()].getBaseStorage(),
              "The number of items a " + name + " bin can store.");
        if (tier != Tier.BaseTier.CREATIVE) {
            InductionCellMaxEnergy = new DoubleOption(baseConfig, "tier", name + "InductionCellMaxEnergy",
                  Tier.InductionCellTier.values()[tier.ordinal()].getBaseMaxEnergy(),
                  "Maximum number of Joules a " + name + " induction cell can store.");
            InductionProviderOutput = new DoubleOption(baseConfig, "tier", name + "InductionProviderOutput",
                  Tier.InductionProviderTier.values()[tier.ordinal()].getBaseOutput(),
                  "Maximum number of Joules a " + name + " induction provider can output or accept.");
            CableCapacity = new IntOption(baseConfig, "tier", name + "CableCapacity",
                  Tier.CableTier.values()[tier.ordinal()].getBaseCapacity(),
                  "Internal buffer in Joules of each " + name + " universal cable.");
            PipeCapacity = new IntOption(baseConfig, "tier", name + "PipeCapacity",
                  Tier.PipeTier.values()[tier.ordinal()].getBaseCapacity(),
                  "Capacity of " + name + " mechanical pipe in mB.");
            PipePullAmount = new IntOption(baseConfig, "tier", name + "PipePullAmount",
                  Tier.PipeTier.values()[tier.ordinal()].getBasePull(),
                  "Pump rate of " + name + " mechanical pipe in mB/t.");
            TubeCapacity = new IntOption(baseConfig, "tier", name + "TubeCapacity",
                  Tier.TubeTier.values()[tier.ordinal()].getBaseCapacity(),
                  "Capacity of " + name + " pressurized tube in mB.");
            TubePullAmount = new IntOption(baseConfig, "tier", name + "TubePullAmount",
                  Tier.TubeTier.values()[tier.ordinal()].getBasePull(),
                  "Pump rate of " + name + " pressurized tube in mB/t.");
            TransporterPullAmount = new IntOption(baseConfig, "tier", name + "TransporterPullAmount",
                  Tier.TransporterTier.values()[tier.ordinal()].getBasePull(),
                  "Item throughput rate of " + name + " logistical transporter in items/s.");
            TransporterSpeed = new IntOption(baseConfig, "tier", name + "TransporterSpeed",
                  Tier.TransporterTier.values()[tier.ordinal()].getBaseSpeed(),
                  "Five times travel speed of " + name + " logistical transporter.");
            ConductorInverseConduction = new DoubleOption(baseConfig, "tier",
                  name + "ConductorInverseConduction",
                  Tier.ConductorTier.values()[tier.ordinal()].getBaseConduction(),
                  "Conduction value of " + name + " thermodynamic conductor.");
            ConductorHeatCapacity = new DoubleOption(baseConfig, "tier", name + "ConductorHeatCapacity",
                  Tier.ConductorTier.values()[tier.ordinal()].getBaseHeatCapacity(),
                  "Heat capacity of " + name + " thermodynamic conductor.");
            ConductorConductionInsulation = new DoubleOption(baseConfig, "tier",
                  name + "ConductorConductionInsulation",
                  Tier.ConductorTier.values()[tier.ordinal()].getBaseConductionInsulation(),
                  "Insulation value of " + name + " thermodynamic conductor.");

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
}