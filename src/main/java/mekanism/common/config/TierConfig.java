package mekanism.common.config;

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
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.config.ModConfig.Type;

//TODO: Name things better
public class TierConfig implements IMekanismConfig {

    private static final String ENERGY_CUBE_CATEGORY = "energy_cubes";
    private static final String FLUID_TANK_CATEGORY = "fluid_tanks";
    private static final String GAS_TANK_CATEGORY = "gas_tanks";
    private static final String BIN_CATEGORY = "bins";
    private static final String INDUCTION_CATEGORY = "induction";
    private static final String TRANSMITTER_CATEGORY = "transmitters";
    private static final String ENERGY_CATEGORY = "energy";
    private static final String FLUID_CATEGORY = "fluid";
    private static final String GAS_CATEGORY = "gas";
    private static final String ITEMS_CATEGORY = "items";
    private static final String HEAT_CATEGORY = "heat";

    private final ForgeConfigSpec configSpec;

    TierConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Tier Config");
        addEnergyCubeCategory(builder);
        addFluidTankCategory(builder);
        addGasTankCategory(builder);
        addBinCategory(builder);
        addInductionCategory(builder);
        addTransmittersCategory(builder);
        configSpec = builder.build();
    }

    private void addEnergyCubeCategory(ForgeConfigSpec.Builder builder) {
        builder.comment("Energy Cubes").push(ENERGY_CUBE_CATEGORY);
        for (EnergyCubeTier tier : EnergyCubeTier.values()) {
            String tierName = tier.getBaseTier().getSimpleName();
            DoubleValue storageReference = builder.comment("Maximum number of Joules " + tierName + " energy cubes can store.")
                  .defineInRange(tierName.toLowerCase() + "Storage", tier.getBaseMaxEnergy(), 1, Double.MAX_VALUE);
            DoubleValue outputReference = builder.comment("Output rate in Joules of " + tierName + " energy cubes.")
                  .defineInRange(tierName.toLowerCase() + "Output", tier.getBaseOutput(), 1, Double.MAX_VALUE);
            tier.setConfigReference(storageReference, outputReference);
        }
        builder.pop();
    }

    private void addFluidTankCategory(ForgeConfigSpec.Builder builder) {
        builder.comment("Fluid Tanks").push(FLUID_TANK_CATEGORY);
        for (FluidTankTier tier : FluidTankTier.values()) {
            String tierName = tier.getBaseTier().getSimpleName();
            IntValue storageReference = builder.comment("Storage size of " + tierName + " fluid tanks in mB.")
                  .defineInRange(tierName.toLowerCase() + "Storage", tier.getBaseStorage(), 1, Integer.MAX_VALUE);
            IntValue outputReference = builder.comment("Output rate of " + tierName + " fluid tanks in mB.")
                  .defineInRange(tierName.toLowerCase() + "Output", tier.getBaseOutput(), 1, Integer.MAX_VALUE);
            tier.setConfigReference(storageReference, outputReference);
        }
        builder.pop();
    }

    private void addGasTankCategory(ForgeConfigSpec.Builder builder) {
        builder.comment("Gas Tanks").push(GAS_TANK_CATEGORY);
        for (GasTankTier tier : GasTankTier.values()) {
            String tierName = tier.getBaseTier().getSimpleName();
            IntValue storageReference = builder.comment("Storage size of " + tierName + " gas tanks in mB.")
                  .defineInRange(tierName.toLowerCase() + "Storage", tier.getBaseStorage(), 1, Integer.MAX_VALUE);
            IntValue outputReference = builder.comment("Output rate of " + tierName + " gas tanks in mB.")
                  .defineInRange(tierName.toLowerCase() + "Output", tier.getBaseOutput(), 1, Integer.MAX_VALUE);
            tier.setConfigReference(storageReference, outputReference);
        }
        builder.pop();
    }

    private void addBinCategory(ForgeConfigSpec.Builder builder) {
        builder.comment("Bins").push(BIN_CATEGORY);
        for (BinTier tier : BinTier.values()) {
            String tierName = tier.getBaseTier().getSimpleName();
            IntValue storageReference = builder.comment("The number of items " + tierName + " bins can store.")
                  .defineInRange(tierName.toLowerCase() + "Storage", tier.getBaseStorage(), 1, Integer.MAX_VALUE);
            tier.setConfigReference(storageReference);
        }
        builder.pop();
    }

    private void addInductionCategory(ForgeConfigSpec.Builder builder) {
        builder.comment("Induction").push(INDUCTION_CATEGORY);
        for (InductionCellTier tier : InductionCellTier.values()) {
            String tierName = tier.getBaseTier().getSimpleName();
            DoubleValue storageReference = builder.comment("Maximum number of Joules " + tierName + " induction cells can store.")
                  .defineInRange(tierName.toLowerCase() + "Storage", tier.getBaseMaxEnergy(), 1, Double.MAX_VALUE);
            tier.setConfigReference(storageReference);
        }
        for (InductionProviderTier tier : InductionProviderTier.values()) {
            String tierName = tier.getBaseTier().getSimpleName();
            DoubleValue outputReference = builder.comment("Maximum number of Joules " + tierName + " induction providers can output or accept.")
                  .defineInRange(tierName.toLowerCase() + "Output", tier.getBaseOutput(), 1, Double.MAX_VALUE);
            tier.setConfigReference(outputReference);
        }
        builder.pop();
    }

    private void addTransmittersCategory(ForgeConfigSpec.Builder builder) {
        builder.comment("Transmitters").push(TRANSMITTER_CATEGORY);
        addUniversalCableCategory(builder);
        addMechanicalPipeCategory(builder);
        addPressurizedTubesCategory(builder);
        addLogisticalTransportersCategory(builder);
        addThermodynamicConductorsCategory(builder);
        builder.pop();
    }

    private void addUniversalCableCategory(ForgeConfigSpec.Builder builder) {
        builder.comment("Universal Cables").push(ENERGY_CATEGORY);
        for (CableTier tier : CableTier.values()) {
            String tierName = tier.getBaseTier().getSimpleName();
            IntValue capacityReference = builder.comment("Internal buffer in Joules of each " + tierName + " universal cable.")
                  .defineInRange(tierName.toLowerCase() + "Capacity", tier.getBaseCapacity(), 1, Integer.MAX_VALUE);
            tier.setConfigReference(capacityReference);
        }
        builder.pop();
    }

    private void addMechanicalPipeCategory(ForgeConfigSpec.Builder builder) {
        builder.comment("Mechanical Pipes").push(FLUID_CATEGORY);
        for (PipeTier tier : PipeTier.values()) {
            String tierName = tier.getBaseTier().getSimpleName();
            IntValue capacityReference = builder.comment("Capacity of " + tierName + " mechanical pipes in mB.")
                  .defineInRange(tierName.toLowerCase() + "Capacity", tier.getBaseCapacity(), 1, Integer.MAX_VALUE);
            IntValue pullReference = builder.comment("Pump rate of " + tierName + " mechanical pipes in mB/t.")
                  .defineInRange(tierName.toLowerCase() + "PullAmount", tier.getBasePull(), 1, Integer.MAX_VALUE);
            tier.setConfigReference(capacityReference, pullReference);
        }
        builder.pop();
    }

    private void addPressurizedTubesCategory(ForgeConfigSpec.Builder builder) {
        builder.comment("Pressurized Tubes").push(GAS_CATEGORY);
        for (TubeTier tier : TubeTier.values()) {
            String tierName = tier.getBaseTier().getSimpleName();
            IntValue capacityReference = builder.comment("Capacity of " + tierName + " pressurized tubes in mB.")
                  .defineInRange(tierName.toLowerCase() + "Capacity", tier.getBaseCapacity(), 1, Integer.MAX_VALUE);
            IntValue pullReference = builder.comment("Pump rate of " + tierName + " pressurized tubes in mB/t.")
                  .defineInRange(tierName.toLowerCase() + "PullAmount", tier.getBasePull(), 1, Integer.MAX_VALUE);
            tier.setConfigReference(capacityReference, pullReference);
        }
        builder.pop();
    }

    private void addLogisticalTransportersCategory(ForgeConfigSpec.Builder builder) {
        builder.comment("Logistical Transporters").push(ITEMS_CATEGORY);
        for (TransporterTier tier : TransporterTier.values()) {
            String tierName = tier.getBaseTier().getSimpleName();
            IntValue pullReference = builder.comment("Item throughput rate of " + tierName + " logistical transporters in items/s.")
                  .defineInRange(tierName.toLowerCase() + "PullAmount", tier.getBasePull(), 1, Integer.MAX_VALUE);
            IntValue speedReference = builder.comment("Five times travel speed of " + tierName + " logistical transporter.")
                  .defineInRange(tierName.toLowerCase() + "Speed", tier.getBaseSpeed(), 1, Integer.MAX_VALUE);
            tier.setConfigReference(pullReference, speedReference);
        }
        builder.pop();
    }

    private void addThermodynamicConductorsCategory(ForgeConfigSpec.Builder builder) {
        builder.comment("Thermodynamic Conductors").push(HEAT_CATEGORY);
        for (ConductorTier tier : ConductorTier.values()) {
            String tierName = tier.getBaseTier().getSimpleName();
            DoubleValue conductionReference = builder.comment("Conduction value of " + tierName + " thermodynamic conductors.")
                  .defineInRange(tierName.toLowerCase() + "InverseConduction", tier.getBaseConduction(), 1, Double.MAX_VALUE);
            DoubleValue capacityReference = builder.comment("Heat capacity of " + tierName + " thermodynamic conductors.")
                  .defineInRange(tierName.toLowerCase() + "HeatCapacity", tier.getBaseHeatCapacity(), 1, Double.MAX_VALUE);
            DoubleValue insulationReference = builder.comment("Insulation value of " + tierName + " thermodynamic conductor.")
                  .defineInRange(tierName.toLowerCase() + "HeatCapacity", tier.getBaseConductionInsulation(), 1, Double.MAX_VALUE);
            tier.setConfigReference(conductionReference, capacityReference, insulationReference);
        }
        builder.pop();
    }

    @Override
    public String getFileName() {
        return "mekanism-tiers.toml";
    }

    @Override
    public ForgeConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.COMMON;
    }
}