package mekanism.common.util;

import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;

public class EnumUtils {

    private EnumUtils() {
    }

    /**
     * Cached collection of armor slot positions from EquipmentSlotType. DO NOT MODIFY THIS LIST.
     */
    public static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    /**
     * Cached collection of hand slot positions from EquipmentSlotType. DO NOT MODIFY THIS LIST.
     */
    public static final EquipmentSlot[] HAND_SLOTS = {EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};

    /**
     * Cached value of {@link Direction#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final Direction[] DIRECTIONS = Direction.values();

    /**
     * Cached value of the horizontal directions. DO NOT MODIFY THIS LIST.
     *
     * @implNote Index is ordinal() - 2, as the first two elements of {@link Direction} are {@link Direction#DOWN} and {@link Direction#UP}
     */
    public static final Direction[] HORIZONTAL_DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    /**
     * Cached value of {@link RelativeSide#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final RelativeSide[] SIDES = RelativeSide.values();

    /**
     * Cached value of {@link MeasurementUnit#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final MeasurementUnit[] MEASUREMENT_UNITS = MeasurementUnit.values();

    /**
     * Cached value of {@link TransmissionType#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final TransmissionType[] TRANSMISSION_TYPES = TransmissionType.values();

    /**
     * Cached value of {@link BaseTier#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final BaseTier[] TIERS = BaseTier.values();

    /**
     * Cached value of {@link CableTier#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final CableTier[] CABLE_TIERS = CableTier.values();

    /**
     * Cached value of {@link TransporterTier#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final TransporterTier[] TRANSPORTER_TIERS = TransporterTier.values();

    /**
     * Cached value of {@link ConductorTier#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final ConductorTier[] CONDUCTOR_TIERS = ConductorTier.values();

    /**
     * Cached value of {@link TubeTier#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final TubeTier[] TUBE_TIERS = TubeTier.values();

    /**
     * Cached value of {@link PipeTier#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final PipeTier[] PIPE_TIERS = PipeTier.values();

    /**
     * Cached value of {@link ChemicalTankTier#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final ChemicalTankTier[] CHEMICAL_TANK_TIERS = ChemicalTankTier.values();

    /**
     * Cached value of {@link FluidTankTier#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final FluidTankTier[] FLUID_TANK_TIERS = FluidTankTier.values();

    /**
     * Cached value of {@link BinTier#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final BinTier[] BIN_TIERS = BinTier.values();

    /**
     * Cached value of {@link EnergyCubeTier#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final EnergyCubeTier[] ENERGY_CUBE_TIERS = EnergyCubeTier.values();

    /**
     * Cached value of {@link InductionCellTier#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final InductionCellTier[] INDUCTION_CELL_TIERS = InductionCellTier.values();

    /**
     * Cached value of {@link InductionProviderTier#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final InductionProviderTier[] INDUCTION_PROVIDER_TIERS = InductionProviderTier.values();

    /**
     * Cached value of {@link FactoryTier#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final FactoryTier[] FACTORY_TIERS = FactoryTier.values();

    /**
     * Cached value of {@link FactoryType#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final FactoryType[] FACTORY_TYPES = FactoryType.values();

    /**
     * Cached value of {@link Upgrade#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final Upgrade[] UPGRADES = Upgrade.values();

    /**
     * Cached value of {@link OreType#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final OreType[] ORE_TYPES = OreType.values();

    /**
     * Cached value of {@link PrimaryResource#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final PrimaryResource[] PRIMARY_RESOURCES = PrimaryResource.values();

    /**
     * Cached value of {@link ResourceType#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final ResourceType[] RESOURCE_TYPES = ResourceType.values();

    /**
     * Cached value of {@link EquipmentSlot#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final EquipmentSlot[] EQUIPMENT_SLOT_TYPES = EquipmentSlot.values();

    /**
     * Cached value of {@link EnumColor#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final EnumColor[] COLORS = EnumColor.values();

    /**
     * Cached value of {@link RobitPrideSkinData#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final RobitPrideSkinData[] PRIDE_SKINS = RobitPrideSkinData.values();

    /**
     * Cached value of {@link DriveStatus#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final DriveStatus[] DRIVE_STATUSES = DriveStatus.values();

    /**
     * Cached value of {@link ArmorItem.Type#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final ArmorItem.Type[] ARMOR_TYPES = ArmorItem.Type.values();
}