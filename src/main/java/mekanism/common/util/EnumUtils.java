package mekanism.common.util;

import mekanism.api.RelativeSide;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.gear.ItemFlamethrower.FlamethrowerMode;
import mekanism.common.item.gear.ItemFreeRunners.FreeRunnerMode;
import mekanism.common.item.gear.ItemJetpack.JetpackMode;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.tier.BinTier;
import mekanism.common.tier.CableTier;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tier.GasTankTier;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tier.PipeTier;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.tile.TileEntityLaserAmplifier.RedstoneOutput;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import mekanism.common.util.UnitDisplayUtils.EnergyType;
import mekanism.common.util.UnitDisplayUtils.MeasurementUnit;
import mekanism.common.util.UnitDisplayUtils.TempType;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;

public class EnumUtils {

    /**
     * Cached value of {@link Direction#values()}. DO NOT MODIFY THIS LIST.
     */
    //TODO: Replace some calls to this with Direction.byIndex?
    public static final Direction[] DIRECTIONS = Direction.values();

    /**
     * Cached value of {@link RelativeSide#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final RelativeSide[] SIDES = RelativeSide.values();

    /**
     * Cached value of {@link AxisDirection#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final AxisDirection[] AXIS_DIRECTIONS = AxisDirection.values();

    /**
     * Cached value of {@link RedstoneControl#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final RedstoneControl[] REDSTONE_CONTROLS = RedstoneControl.values();

    /**
     * Cached value of {@link RedstoneOutput#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final RedstoneOutput[] REDSTONE_OUTPUTS = RedstoneOutput.values();

    /**
     * Cached value of {@link GasMode#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final GasMode[] GAS_MODES = GasMode.values();

    /**
     * Cached value of {@link ConfiguratorMode#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final ConfiguratorMode[] CONFIGURATOR_MODES = ConfiguratorMode.values();

    /**
     * Cached value of {@link TemperatureUnit#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final TemperatureUnit[] TEMPERATURE_UNITS = TemperatureUnit.values();

    /**
     * Cached value of {@link MeasurementUnit#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final MeasurementUnit[] MEASUREMENT_UNITS = MeasurementUnit.values();

    /**
     * Cached value of {@link EnergyType#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final EnergyType[] ENERGY_TYPES = EnergyType.values();

    /**
     * Cached value of {@link TempType#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final TempType[] TEMP_TYPES = TempType.values();

    /**
     * Cached value of {@link ConnectionType#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final ConnectionType[] CONNECTION_TYPES = ConnectionType.values();

    /**
     * Cached value of {@link TransmissionType#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final TransmissionType[] TRANSMISSION_TYPES = TransmissionType.values();

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
     * Cached value of {@link GasTankTier#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final GasTankTier[] GAS_TANK_TIERS = GasTankTier.values();

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
     * Cached value of {@link SecurityMode#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final SecurityMode[] SECURITY_MODES = SecurityMode.values();

    /**
     * Cached value of {@link ContainerEditMode#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final ContainerEditMode[] CONTAINER_EDIT_MODES = ContainerEditMode.values();

    /**
     * Cached value of {@link FlamethrowerMode#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final FlamethrowerMode[] FLAMETHROWER_MODES = FlamethrowerMode.values();

    /**
     * Cached value of {@link JetpackMode#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final JetpackMode[] JETPACK_MODES = JetpackMode.values();

    /**
     * Cached value of {@link FreeRunnerMode#values()}. DO NOT MODIFY THIS LIST.
     */
    public static final FreeRunnerMode[] FREE_RUNNER_MODES = FreeRunnerMode.values();
}