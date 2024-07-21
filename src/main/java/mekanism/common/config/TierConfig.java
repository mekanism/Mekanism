package mekanism.common.config;

import java.util.Locale;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.tier.BinTier;
import mekanism.common.tier.CableTier;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tier.PipeTier;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tier.TubeTier;
import mekanism.common.util.EnumUtils;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class TierConfig extends BaseMekanismConfig {

    private static final String ENERGY_CUBE_CATEGORY = "energy_cubes";
    private static final String FLUID_TANK_CATEGORY = "fluid_tanks";
    private static final String CHEMICAL_TANK_CATEGORY = "chemical_tanks";
    private static final String BIN_CATEGORY = "bins";
    private static final String INDUCTION_CATEGORY = "induction";
    private static final String TRANSMITTER_CATEGORY = "transmitters";
    private static final String ENERGY_CATEGORY = "energy";
    private static final String FLUID_CATEGORY = "fluid";
    private static final String CHEMICAL_CATEGORY = "chemical";
    private static final String ITEMS_CATEGORY = "items";
    private static final String HEAT_CATEGORY = "heat";

    private final ModConfigSpec configSpec;

    TierConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Tier Config. This config is synced from server to client.").push("tier");
        addEnergyCubeCategory(builder);
        addFluidTankCategory(builder);
        addGasTankCategory(builder);
        addBinCategory(builder);
        addInductionCategory(builder);
        addTransmittersCategory(builder);

        builder.pop();
        configSpec = builder.build();
    }

    private void addEnergyCubeCategory(ModConfigSpec.Builder builder) {
        builder.comment("Energy Cubes").push(ENERGY_CUBE_CATEGORY);
        for (EnergyCubeTier tier : EnumUtils.ENERGY_CUBE_TIERS) {
            String tierName = tier.getBaseTier().getSimpleName();
            CachedLongValue storageReference = CachedLongValue.wrap(this, builder.comment("Maximum number of Joules " + tierName + " energy cubes can store.")
                  .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Storage", tier.getBaseMaxEnergy(), 1, Long.MAX_VALUE));
            CachedLongValue outputReference = CachedLongValue.wrap(this, builder.comment("Output rate in Joules of " + tierName + " energy cubes.")
                  .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Output", tier.getBaseOutput(), 1, Long.MAX_VALUE));
            tier.setConfigReference(storageReference, outputReference);
        }
        builder.pop();
    }

    private void addFluidTankCategory(ModConfigSpec.Builder builder) {
        builder.comment("Fluid Tanks").push(FLUID_TANK_CATEGORY);
        for (FluidTankTier tier : EnumUtils.FLUID_TANK_TIERS) {
            String tierName = tier.getBaseTier().getSimpleName();
            CachedIntValue storageReference = CachedIntValue.wrap(this, builder.comment("Storage size of " + tierName + " fluid tanks in mB.")
                  .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Storage", tier.getBaseStorage(), 1, Integer.MAX_VALUE));
            CachedIntValue outputReference = CachedIntValue.wrap(this, builder.comment("Output rate of " + tierName + " fluid tanks in mB.")
                  .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Output", tier.getBaseOutput(), 1, Integer.MAX_VALUE));
            tier.setConfigReference(storageReference, outputReference);
        }
        builder.pop();
    }

    private void addGasTankCategory(ModConfigSpec.Builder builder) {
        builder.comment("Chemical Tanks").push(CHEMICAL_TANK_CATEGORY);
        for (ChemicalTankTier tier : EnumUtils.CHEMICAL_TANK_TIERS) {
            String tierName = tier.getBaseTier().getSimpleName();
            CachedLongValue storageReference = CachedLongValue.wrap(this, builder.comment("Storage size of " + tierName + " chemical tanks in mB.")
                  .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Storage", tier.getBaseStorage(), 1, Long.MAX_VALUE));
            CachedLongValue outputReference = CachedLongValue.wrap(this, builder.comment("Output rate of " + tierName + " chemical tanks in mB.")
                  .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Output", tier.getBaseOutput(), 1, Long.MAX_VALUE));
            tier.setConfigReference(storageReference, outputReference);
        }
        builder.pop();
    }

    private void addBinCategory(ModConfigSpec.Builder builder) {
        builder.comment("Bins").push(BIN_CATEGORY);
        for (BinTier tier : EnumUtils.BIN_TIERS) {
            String tierName = tier.getBaseTier().getSimpleName();
            CachedIntValue storageReference = CachedIntValue.wrap(this, builder.comment("The number of items " + tierName + " bins can store.")
                  .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Storage", tier.getBaseStorage(), 1, Integer.MAX_VALUE));
            tier.setConfigReference(storageReference);
        }
        builder.pop();
    }

    private void addInductionCategory(ModConfigSpec.Builder builder) {
        builder.comment("Induction").push(INDUCTION_CATEGORY);
        for (InductionCellTier tier : EnumUtils.INDUCTION_CELL_TIERS) {
            String tierName = tier.getBaseTier().getSimpleName();
            CachedLongValue storageReference = CachedLongValue.wrap(this, builder.comment("Maximum number of Joules " + tierName + " induction cells can store.")
                  .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Storage", tier.getBaseMaxEnergy(), 1, Long.MAX_VALUE));
            tier.setConfigReference(storageReference);
        }
        for (InductionProviderTier tier : EnumUtils.INDUCTION_PROVIDER_TIERS) {
            String tierName = tier.getBaseTier().getSimpleName();
            CachedLongValue outputReference = CachedLongValue.wrap(this, builder.comment("Maximum number of Joules " + tierName + " induction providers can output or accept.")
                  .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Output", tier.getBaseOutput(), 1, Long.MAX_VALUE));
            tier.setConfigReference(outputReference);
        }
        builder.pop();
    }

    private void addTransmittersCategory(ModConfigSpec.Builder builder) {
        builder.comment("Transmitters").push(TRANSMITTER_CATEGORY);
        addUniversalCableCategory(builder);
        addMechanicalPipeCategory(builder);
        addPressurizedTubesCategory(builder);
        addLogisticalTransportersCategory(builder);
        addThermodynamicConductorsCategory(builder);
        builder.pop();
    }

    private void addUniversalCableCategory(ModConfigSpec.Builder builder) {
        builder.comment("Universal Cables").push(ENERGY_CATEGORY);
        for (CableTier tier : EnumUtils.CABLE_TIERS) {
            String tierName = tier.getBaseTier().getSimpleName();
            CachedLongValue capacityReference = CachedLongValue.wrap(this, builder.comment("Internal buffer in Joules of each " + tierName + " universal cable.")
                  .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Capacity", tier.getBaseCapacity(), 1, Long.MAX_VALUE));
            tier.setConfigReference(capacityReference);
        }
        builder.pop();
    }

    private void addMechanicalPipeCategory(ModConfigSpec.Builder builder) {
        builder.comment("Mechanical Pipes").push(FLUID_CATEGORY);
        for (PipeTier tier : EnumUtils.PIPE_TIERS) {
            String tierName = tier.getBaseTier().getSimpleName();
            CachedIntValue capacityReference = CachedIntValue.wrap(this, builder.comment("Capacity of " + tierName + " mechanical pipes in mB.")
                  .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Capacity", tier.getBaseCapacity(), 1, Integer.MAX_VALUE));
            CachedIntValue pullReference = CachedIntValue.wrap(this, builder.comment("Pump rate of " + tierName + " mechanical pipes in mB/t.")
                  .defineInRange(tierName.toLowerCase(Locale.ROOT) + "PullAmount", tier.getBasePull(), 1, Integer.MAX_VALUE));
            tier.setConfigReference(capacityReference, pullReference);
        }
        builder.pop();
    }

    private void addPressurizedTubesCategory(ModConfigSpec.Builder builder) {
        builder.comment("Pressurized Tubes").push(CHEMICAL_CATEGORY);
        for (TubeTier tier : EnumUtils.TUBE_TIERS) {
            String tierName = tier.getBaseTier().getSimpleName();
            CachedLongValue capacityReference = CachedLongValue.wrap(this, builder.comment("Capacity of " + tierName + " pressurized tubes in mB.")
                  .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Capacity", tier.getBaseCapacity(), 1, Long.MAX_VALUE));
            CachedLongValue pullReference = CachedLongValue.wrap(this, builder.comment("Pump rate of " + tierName + " pressurized tubes in mB/t.")
                  .defineInRange(tierName.toLowerCase(Locale.ROOT) + "PullAmount", tier.getBasePull(), 1, Long.MAX_VALUE));
            tier.setConfigReference(capacityReference, pullReference);
        }
        builder.pop();
    }

    private void addLogisticalTransportersCategory(ModConfigSpec.Builder builder) {
        builder.comment("Logistical Transporters").push(ITEMS_CATEGORY);
        for (TransporterTier tier : EnumUtils.TRANSPORTER_TIERS) {
            String tierName = tier.getBaseTier().getSimpleName();
            CachedIntValue pullReference = CachedIntValue.wrap(this, builder.comment("Item throughput rate of " + tierName + " logistical transporters in items/half second. This value assumes a target tick rate of 20 ticks per second.")
                  .defineInRange(tierName.toLowerCase(Locale.ROOT) + "PullAmount", tier.getBasePull(), 1, Integer.MAX_VALUE));
            CachedIntValue speedReference = CachedIntValue.wrap(this, builder.comment("Five times the travel speed in m/s of " + tierName + " logistical transporter. This value assumes a target tick rate of 20 ticks per second.")
                  .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Speed", tier.getBaseSpeed(), 1, Integer.MAX_VALUE));
            tier.setConfigReference(pullReference, speedReference);
        }
        builder.pop();
    }

    private void addThermodynamicConductorsCategory(ModConfigSpec.Builder builder) {
        builder.comment("Thermodynamic Conductors").push(HEAT_CATEGORY);
        for (ConductorTier tier : EnumUtils.CONDUCTOR_TIERS) {
            String tierName = tier.getBaseTier().getSimpleName();
            CachedDoubleValue conductionReference = CachedDoubleValue.wrap(this, builder.comment("Conduction value of " + tierName + " thermodynamic conductors.")
                  .defineInRange(tierName.toLowerCase(Locale.ROOT) + "InverseConduction", tier.getBaseConduction(), 1, Double.MAX_VALUE));
            CachedDoubleValue capacityReference = CachedDoubleValue.wrap(this, builder.comment("Heat capacity of " + tierName + " thermodynamic conductors.")
                  .defineInRange(tierName.toLowerCase(Locale.ROOT) + "HeatCapacity", tier.getBaseHeatCapacity(), 1, Double.MAX_VALUE));
            CachedDoubleValue insulationReference = CachedDoubleValue.wrap(this, builder.comment("Insulation value of " + tierName + " thermodynamic conductor.")
                  .defineInRange(tierName.toLowerCase(Locale.ROOT) + "Insulation", tier.getBaseConductionInsulation(), 0, Double.MAX_VALUE));
            tier.setConfigReference(conductionReference, capacityReference, insulationReference);
        }
        builder.pop();
    }

    @Override
    public String getFileName() {
        return "tiers";
    }

    @Override
    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }
}