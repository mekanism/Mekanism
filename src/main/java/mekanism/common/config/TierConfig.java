package mekanism.common.config;

import java.util.EnumMap;
import mekanism.common.config.options.DoubleOption;
import mekanism.common.config.options.IntOption;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.BinTier;
import mekanism.common.tier.CableTier;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tier.GasTankTier;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tier.PipeTier;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tier.TubeTier;

/**
 * Created by Thiakil on 16/03/2019.
 */
public class TierConfig {

    public static EnumMap<BaseTier, TierConfig> create(BaseConfig baseConfig) {
        EnumMap<BaseTier, TierConfig> map = new EnumMap<>(BaseTier.class);
        for (BaseTier baseTier : BaseTier.values()) {
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

    private TierConfig(BaseConfig baseConfig, BaseTier tier) {
        String name = tier.getSimpleName();
        EnergyCubeMaxEnergy = new DoubleOption(baseConfig, "tier", name + "EnergyCubeMaxEnergy", EnergyCubeTier.values()[tier.ordinal()].getBaseMaxEnergy(),
              "Maximum number of Joules a " + name + " energy cube can store.");
        EnergyCubeOutput = new DoubleOption(baseConfig, "tier", name + "EnergyCubeOutput", EnergyCubeTier.values()[tier.ordinal()].getBaseOutput(),
              "Output rate in Joules of a " + name + " energy cube.");
        FluidTankStorage = new IntOption(baseConfig, "tier", name + "FluidTankStorage", FluidTankTier.values()[tier.ordinal()].getBaseStorage(),
              "Storage size of " + name + " gas tank in mB.");
        FluidTankOutput = new IntOption(baseConfig, "tier", name + "FluidTankOutput", FluidTankTier.values()[tier.ordinal()].getBaseOutput(),
              "Output rate of " + name + " gas tank in mB.");
        GasTankStorage = new IntOption(baseConfig, "tier", name + "GasTankStorage", GasTankTier.values()[tier.ordinal()].getBaseStorage(),
              "Storage size of " + name + " gas tank in mB.");
        GasTankOutput = new IntOption(baseConfig, "tier", name + "GasTankOutput", GasTankTier.values()[tier.ordinal()].getBaseOutput(),
              "Output rate of " + name + " gas tank in mB.");
        BinStorage = new IntOption(baseConfig, "tier", name + "BinStorage", BinTier.values()[tier.ordinal()].getBaseStorage(),
              "The number of items a " + name + " bin can store.");
        if (tier != BaseTier.CREATIVE) {
            InductionCellMaxEnergy = new DoubleOption(baseConfig, "tier", name + "InductionCellMaxEnergy", InductionCellTier.values()[tier.ordinal()].getBaseMaxEnergy(),
                  "Maximum number of Joules a " + name + " induction cell can store.");
            InductionProviderOutput = new DoubleOption(baseConfig, "tier", name + "InductionProviderOutput", InductionProviderTier.values()[tier.ordinal()].getBaseOutput(),
                  "Maximum number of Joules a " + name + " induction provider can output or accept.");
            CableCapacity = new IntOption(baseConfig, "tier", name + "CableCapacity", CableTier.values()[tier.ordinal()].getBaseCapacity(),
                  "Internal buffer in Joules of each " + name + " universal cable.");
            PipeCapacity = new IntOption(baseConfig, "tier", name + "PipeCapacity", PipeTier.values()[tier.ordinal()].getBaseCapacity(),
                  "Capacity of " + name + " mechanical pipe in mB.");
            PipePullAmount = new IntOption(baseConfig, "tier", name + "PipePullAmount", PipeTier.values()[tier.ordinal()].getBasePull(),
                  "Pump rate of " + name + " mechanical pipe in mB/t.");
            TubeCapacity = new IntOption(baseConfig, "tier", name + "TubeCapacity", TubeTier.values()[tier.ordinal()].getBaseCapacity(),
                  "Capacity of " + name + " pressurized tube in mB.");
            TubePullAmount = new IntOption(baseConfig, "tier", name + "TubePullAmount", TubeTier.values()[tier.ordinal()].getBasePull(),
                  "Pump rate of " + name + " pressurized tube in mB/t.");
            TransporterPullAmount = new IntOption(baseConfig, "tier", name + "TransporterPullAmount", TransporterTier.values()[tier.ordinal()].getBasePull(),
                  "Item throughput rate of " + name + " logistical transporter in items/s.");
            TransporterSpeed = new IntOption(baseConfig, "tier", name + "TransporterSpeed", TransporterTier.values()[tier.ordinal()].getBaseSpeed(),
                  "Five times travel speed of " + name + " logistical transporter.");
            ConductorInverseConduction = new DoubleOption(baseConfig, "tier", name + "ConductorInverseConduction", ConductorTier.values()[tier.ordinal()].getBaseConduction(),
                  "Conduction value of " + name + " thermodynamic conductor.");
            ConductorHeatCapacity = new DoubleOption(baseConfig, "tier", name + "ConductorHeatCapacity", ConductorTier.values()[tier.ordinal()].getBaseHeatCapacity(),
                  "Heat capacity of " + name + " thermodynamic conductor.");
            ConductorConductionInsulation = new DoubleOption(baseConfig, "tier", name + "ConductorConductionInsulation", ConductorTier.values()[tier.ordinal()].getBaseConductionInsulation(),
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