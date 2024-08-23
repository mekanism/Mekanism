package mekanism.common.config;

import java.util.Locale;
import mekanism.common.config.MekanismConfigTranslations.TierTranslations;
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

    private final ModConfigSpec configSpec;

    TierConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        addEnergyCubeCategory(builder);
        addFluidTankCategory(builder);
        addChemicalTankCategory(builder);
        addBinCategory(builder);
        addInductionCategory(builder);
        addTransmittersCategory(builder);

        configSpec = builder.build();
    }

    private void addEnergyCubeCategory(ModConfigSpec.Builder builder) {
        MekanismConfigTranslations.TIER_ENERGY_CUBE.applyToBuilder(builder).push("energy_cubes");
        for (EnergyCubeTier tier : EnumUtils.ENERGY_CUBE_TIERS) {
            TierTranslations translations = TierTranslations.create(tier);
            String tierName = tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT);
            CachedLongValue storageReference = CachedLongValue.wrap(this, translations.first().applyToBuilder(builder)
                  .defineInRange(tierName + "Capacity", tier.getBaseMaxEnergy(), 1, Long.MAX_VALUE));
            CachedLongValue outputReference = CachedLongValue.wrap(this, translations.second().applyToBuilder(builder)
                  .defineInRange(tierName + "Output", tier.getBaseOutput(), 1, Long.MAX_VALUE));
            tier.setConfigReference(storageReference, outputReference);
        }
        builder.pop();
    }

    private void addFluidTankCategory(ModConfigSpec.Builder builder) {
        MekanismConfigTranslations.TIER_FLUID_TANK.applyToBuilder(builder).push("fluid_tanks");
        for (FluidTankTier tier : EnumUtils.FLUID_TANK_TIERS) {
            TierTranslations translations = TierTranslations.create(tier);
            String tierName = tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT);
            CachedIntValue storageReference = CachedIntValue.wrap(this, translations.first().applyToBuilder(builder)
                  .defineInRange(tierName + "Capacity", tier.getBaseStorage(), 1, Integer.MAX_VALUE));
            CachedIntValue outputReference = CachedIntValue.wrap(this, translations.second().applyToBuilder(builder)
                  .defineInRange(tierName + "Output", tier.getBaseOutput(), 1, Integer.MAX_VALUE));
            tier.setConfigReference(storageReference, outputReference);
        }
        builder.pop();
    }

    private void addChemicalTankCategory(ModConfigSpec.Builder builder) {
        MekanismConfigTranslations.TIER_CHEMICAL_TANK.applyToBuilder(builder).push("chemical_tanks");
        for (ChemicalTankTier tier : EnumUtils.CHEMICAL_TANK_TIERS) {
            TierTranslations translations = TierTranslations.create(tier);
            String tierName = tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT);
            CachedLongValue storageReference = CachedLongValue.wrap(this, translations.first().applyToBuilder(builder)
                  .defineInRange(tierName + "Capacity", tier.getBaseStorage(), 1, Long.MAX_VALUE));
            CachedLongValue outputReference = CachedLongValue.wrap(this, translations.second().applyToBuilder(builder)
                  .defineInRange(tierName + "Output", tier.getBaseOutput(), 1, Long.MAX_VALUE));
            tier.setConfigReference(storageReference, outputReference);
        }
        builder.pop();
    }

    private void addBinCategory(ModConfigSpec.Builder builder) {
        MekanismConfigTranslations.TIER_BIN.applyToBuilder(builder).push("bins");
        for (BinTier tier : EnumUtils.BIN_TIERS) {
            TierTranslations translations = TierTranslations.create(tier);
            String tierName = tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT);
            CachedIntValue storageReference = CachedIntValue.wrap(this, translations.first().applyToBuilder(builder)
                  .defineInRange(tierName + "Capacity", tier.getBaseStorage(), 1, Integer.MAX_VALUE));
            tier.setConfigReference(storageReference);
        }
        builder.pop();
    }

    private void addInductionCategory(ModConfigSpec.Builder builder) {
        MekanismConfigTranslations.TIER_INDUCTION.applyToBuilder(builder).push("induction");
        for (InductionCellTier tier : EnumUtils.INDUCTION_CELL_TIERS) {
            TierTranslations translations = TierTranslations.create(tier);
            String tierName = tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT);
            CachedLongValue storageReference = CachedLongValue.wrap(this, translations.first().applyToBuilder(builder)
                  .defineInRange(tierName + "Capacity", tier.getBaseMaxEnergy(), 1, Long.MAX_VALUE));
            tier.setConfigReference(storageReference);
        }
        for (InductionProviderTier tier : EnumUtils.INDUCTION_PROVIDER_TIERS) {
            TierTranslations translations = TierTranslations.create(tier);
            String tierName = tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT);
            CachedLongValue outputReference = CachedLongValue.wrap(this, translations.second().applyToBuilder(builder)
                  .defineInRange(tierName + "Output", tier.getBaseOutput(), 1, Long.MAX_VALUE));
            tier.setConfigReference(outputReference);
        }
        builder.pop();
    }

    private void addTransmittersCategory(ModConfigSpec.Builder builder) {
        MekanismConfigTranslations.TIER_TRANSMITTERS.applyToBuilder(builder).push("transmitters");
        addUniversalCableCategory(builder);
        addMechanicalPipeCategory(builder);
        addPressurizedTubesCategory(builder);
        addLogisticalTransportersCategory(builder);
        addThermodynamicConductorsCategory(builder);
        builder.pop();
    }

    private void addUniversalCableCategory(ModConfigSpec.Builder builder) {
        MekanismConfigTranslations.TIER_TRANSMITTERS_ENERGY.applyToBuilder(builder).push("energy");
        for (CableTier tier : EnumUtils.CABLE_TIERS) {
            TierTranslations translations = TierTranslations.create(tier);
            String tierName = tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT);
            CachedLongValue capacityReference = CachedLongValue.wrap(this, translations.first().applyToBuilder(builder)
                  .defineInRange(tierName + "Capacity", tier.getBaseCapacity(), 1, Long.MAX_VALUE));
            tier.setConfigReference(capacityReference);
        }
        builder.pop();
    }

    private void addMechanicalPipeCategory(ModConfigSpec.Builder builder) {
        MekanismConfigTranslations.TIER_TRANSMITTERS_FLUID.applyToBuilder(builder).push("fluid");
        for (PipeTier tier : EnumUtils.PIPE_TIERS) {
            TierTranslations translations = TierTranslations.create(tier);
            String tierName = tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT);
            CachedIntValue capacityReference = CachedIntValue.wrap(this, translations.first().applyToBuilder(builder)
                  .defineInRange(tierName + "Capacity", tier.getBaseCapacity(), 1, Integer.MAX_VALUE));
            CachedIntValue pullReference = CachedIntValue.wrap(this, translations.second().applyToBuilder(builder)
                  .defineInRange(tierName + "PullAmount", tier.getBasePull(), 1, Integer.MAX_VALUE));
            tier.setConfigReference(capacityReference, pullReference);
        }
        builder.pop();
    }

    private void addPressurizedTubesCategory(ModConfigSpec.Builder builder) {
        MekanismConfigTranslations.TIER_TRANSMITTERS_CHEMICAL.applyToBuilder(builder).push("chemical");
        for (TubeTier tier : EnumUtils.TUBE_TIERS) {
            TierTranslations translations = TierTranslations.create(tier);
            String tierName = tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT);
            CachedLongValue capacityReference = CachedLongValue.wrap(this, translations.first().applyToBuilder(builder)
                  .defineInRange(tierName + "Capacity", tier.getBaseCapacity(), 1, Long.MAX_VALUE));
            CachedLongValue pullReference = CachedLongValue.wrap(this, translations.second().applyToBuilder(builder)
                  .defineInRange(tierName + "PullAmount", tier.getBasePull(), 1, Long.MAX_VALUE));
            tier.setConfigReference(capacityReference, pullReference);
        }
        builder.pop();
    }

    private void addLogisticalTransportersCategory(ModConfigSpec.Builder builder) {
        MekanismConfigTranslations.TIER_TRANSMITTERS_ITEM.applyToBuilder(builder).push("items");
        for (TransporterTier tier : EnumUtils.TRANSPORTER_TIERS) {
            TierTranslations translations = TierTranslations.create(tier);
            String tierName = tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT);
            CachedIntValue pullReference = CachedIntValue.wrap(this, translations.first().applyToBuilder(builder)
                  .defineInRange(tierName + "PullAmount", tier.getBasePull(), 1, Integer.MAX_VALUE));
            CachedIntValue speedReference = CachedIntValue.wrap(this, translations.second().applyToBuilder(builder)
                  .defineInRange(tierName + "Speed", tier.getBaseSpeed(), 1, Integer.MAX_VALUE));
            tier.setConfigReference(pullReference, speedReference);
        }
        builder.pop();
    }

    private void addThermodynamicConductorsCategory(ModConfigSpec.Builder builder) {
        MekanismConfigTranslations.TIER_TRANSMITTERS_HEAT.applyToBuilder(builder).push("heat");
        for (ConductorTier tier : EnumUtils.CONDUCTOR_TIERS) {
            TierTranslations translations = TierTranslations.create(tier);
            String tierName = tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT);
            CachedDoubleValue conductionReference = CachedDoubleValue.wrap(this, translations.first().applyToBuilder(builder)
                  .defineInRange(tierName + "InverseConduction", tier.getBaseConduction(), 1, Double.MAX_VALUE));
            CachedDoubleValue capacityReference = CachedDoubleValue.wrap(this, translations.second().applyToBuilder(builder)
                  .defineInRange(tierName + "HeatCapacity", tier.getBaseHeatCapacity(), 1, Double.MAX_VALUE));
            CachedDoubleValue insulationReference = CachedDoubleValue.wrap(this, translations.third().applyToBuilder(builder)
                  .defineInRange(tierName + "Insulation", tier.getBaseConductionInsulation(), 0, Double.MAX_VALUE));
            tier.setConfigReference(conductionReference, capacityReference, insulationReference);
        }
        builder.pop();
    }

    @Override
    public String getFileName() {
        return "tiers";
    }

    @Override
    public String getTranslation() {
        return "Tier Config";
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