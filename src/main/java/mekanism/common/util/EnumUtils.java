package mekanism.common.util;

import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.BaseTier;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.entity.RobitPrideSkinData;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.resource.ore.OreType;
import mekanism.common.tier.BinTier;
import mekanism.common.tier.CableTier;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tier.PipeTier;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.qio.TileEntityQIODriveArray.DriveStatus;
import mekanism.common.util.UnitDisplayUtils.MeasurementUnit;
import net.minecraft.core.Direction;

public class EnumUtils {

    private EnumUtils() {
    }

    /// Cached value of [Direction#values()]. DO NOT MODIFY THIS LIST.
    public static final Direction[] DIRECTIONS = Direction.values();

    /// Cached value of the horizontal directions. DO NOT MODIFY THIS LIST.
    ///
    /// @implNote Index is ordinal() - 2, as the first two elements of [Direction] are [Direction#DOWN] and [Direction#UP]
    public static final Direction[] HORIZONTAL_DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    /// Cached value of [RelativeSide#values()]. DO NOT MODIFY THIS LIST.
    public static final RelativeSide[] SIDES = RelativeSide.values();

    /// Cached value of [MeasurementUnit#values()]. DO NOT MODIFY THIS LIST.
    public static final MeasurementUnit[] MEASUREMENT_UNITS = MeasurementUnit.values();

    /// Cached value of [TransmissionType#values()]. DO NOT MODIFY THIS LIST.
    public static final TransmissionType[] TRANSMISSION_TYPES = TransmissionType.values();

    /// Cached value of [BaseTier#values()]. DO NOT MODIFY THIS LIST.
    public static final BaseTier[] TIERS = BaseTier.values();

    /// Cached value of [CableTier#values()]. DO NOT MODIFY THIS LIST.
    public static final CableTier[] CABLE_TIERS = CableTier.values();

    /// Cached value of [TransporterTier#values()]. DO NOT MODIFY THIS LIST.
    public static final TransporterTier[] TRANSPORTER_TIERS = TransporterTier.values();

    /// Cached value of [ConductorTier#values()]. DO NOT MODIFY THIS LIST.
    public static final ConductorTier[] CONDUCTOR_TIERS = ConductorTier.values();

    /// Cached value of [TubeTier#values()]. DO NOT MODIFY THIS LIST.
    public static final TubeTier[] TUBE_TIERS = TubeTier.values();

    /// Cached value of [PipeTier#values()]. DO NOT MODIFY THIS LIST.
    public static final PipeTier[] PIPE_TIERS = PipeTier.values();

    /// Cached value of [ChemicalTankTier#values()]. DO NOT MODIFY THIS LIST.
    public static final ChemicalTankTier[] CHEMICAL_TANK_TIERS = ChemicalTankTier.values();

    /// Cached value of [FluidTankTier#values()]. DO NOT MODIFY THIS LIST.
    public static final FluidTankTier[] FLUID_TANK_TIERS = FluidTankTier.values();

    /// Cached value of [BinTier#values()]. DO NOT MODIFY THIS LIST.
    public static final BinTier[] BIN_TIERS = BinTier.values();

    /// Cached value of [EnergyCubeTier#values()]. DO NOT MODIFY THIS LIST.
    public static final EnergyCubeTier[] ENERGY_CUBE_TIERS = EnergyCubeTier.values();

    /// Cached value of [InductionCellTier#values()]. DO NOT MODIFY THIS LIST.
    public static final InductionCellTier[] INDUCTION_CELL_TIERS = InductionCellTier.values();

    /// Cached value of [InductionProviderTier#values()]. DO NOT MODIFY THIS LIST.
    public static final InductionProviderTier[] INDUCTION_PROVIDER_TIERS = InductionProviderTier.values();

    /// Cached value of [FactoryTier#values()]. DO NOT MODIFY THIS LIST.
    public static final FactoryTier[] FACTORY_TIERS = FactoryTier.values();

    /// Cached value of [FactoryType#values()]. DO NOT MODIFY THIS LIST.
    public static final FactoryType[] FACTORY_TYPES = FactoryType.values();

    /// Cached value of [OreType#values()]. DO NOT MODIFY THIS LIST.
    public static final OreType[] ORE_TYPES = OreType.values();

    /// Cached value of [PrimaryResource#values()]. DO NOT MODIFY THIS LIST.
    public static final PrimaryResource[] PRIMARY_RESOURCES = PrimaryResource.values();

    /// Cached value of [ResourceType#values()]. DO NOT MODIFY THIS LIST.
    public static final ResourceType[] RESOURCE_TYPES = ResourceType.values();

    /// Cached value of [EnumColor#values()]. DO NOT MODIFY THIS LIST.
    public static final EnumColor[] COLORS = EnumColor.values();

    /// Cached value of [RobitPrideSkinData#values()]. DO NOT MODIFY THIS LIST.
    public static final RobitPrideSkinData[] PRIDE_SKINS = RobitPrideSkinData.values();

    /// Cached value of [DriveStatus#values()]. DO NOT MODIFY THIS LIST.
    public static final DriveStatus[] DRIVE_STATUSES = DriveStatus.values();
}