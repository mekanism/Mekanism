package buildcraft.api.mj;

import java.text.DecimalFormat;

import javax.annotation.Nonnull;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import buildcraft.api.core.CapabilitiesHelper;

public class MjAPI {

    // ################################
    //
    // Useful constants (Public API)
    //
    // ################################

    /** A single minecraft joule, in micro joules (the power system base unit) */
    public static final long ONE_MINECRAFT_JOULE = 1 * 1000L * 1000L;
    /** The same as {@link #ONE_MINECRAFT_JOULE}, but a shorter field name */
    public static final long MJ = ONE_MINECRAFT_JOULE;

    /** The decimal format used to display values of MJ to the player. Note that this */
    public static final DecimalFormat MJ_DISPLAY_FORMAT = new DecimalFormat("#,##0.##");

    public static IMjEffectManager EFFECT_MANAGER = NullaryEffectManager.INSTANCE;

    // ###############
    //
    // Helpful methods
    //
    // ###############

    /** Formats a given MJ value to a player-oriented string. Note that this does not append "MJ" to the value. */
    public static String formatMj(long microMj) {
        return formatMjInternal(microMj / (double) MJ);
    }

    private static String formatMjInternal(double val) {
        return MJ_DISPLAY_FORMAT.format(val);
    }

    // ########################################
    //
    // Null based classes
    //
    // ########################################

    public enum NullaryEffectManager implements IMjEffectManager {
        INSTANCE;
        @Override public void createPowerLossEffect(World world, Vec3d center, long microJoulesLost) {}
        @Override public void createPowerLossEffect(World world, Vec3d center, EnumFacing direction, long microJoulesLost) {}
        @Override public void createPowerLossEffect(World world, Vec3d center, Vec3d direction, long microJoulesLost) {}
    }
    // @formatter:on

    // ###############
    //
    // Capabilities
    //
    // ###############

    @Nonnull
    public static final Capability<IMjConnector> CAP_CONNECTOR;

    @Nonnull
    public static final Capability<IMjReceiver> CAP_RECEIVER;

    @Nonnull
    public static final Capability<IMjRedstoneReceiver> CAP_REDSTONE_RECEIVER;

    @Nonnull
    public static final Capability<IMjReadable> CAP_READABLE;

    @Nonnull
    public static final Capability<IMjPassiveProvider> CAP_PASSIVE_PROVIDER;

    // ####################
    //
    // Internal API logic
    //
    // ###################

    // Private fields for the registrations -- this allows us to make the actual fields @Nonnull as we check later
    @CapabilityInject(IMjConnector.class)
    private static final Capability<IMjConnector> CAP_CONNECTOR_FIRST = null;

    @CapabilityInject(IMjReceiver.class)
    private static final Capability<IMjReceiver> CAP_RECEIVER_FIRST = null;

    @CapabilityInject(IMjRedstoneReceiver.class)
    private static final Capability<IMjRedstoneReceiver> CAP_REDSTONE_RECEIVER_FIRST = null;

    @CapabilityInject(IMjReadable.class)
    private static final Capability<IMjReadable> CAP_READABLE_FIRST = null;

    @CapabilityInject(IMjPassiveProvider.class)
    private static final Capability<IMjPassiveProvider> CAP_PASSIVE_PROVIDER_FIRST = null;

    static {
        CapabilitiesHelper.registerCapability(IMjConnector.class);
        CapabilitiesHelper.registerCapability(IMjReceiver.class);
        CapabilitiesHelper.registerCapability(IMjRedstoneReceiver.class);
        CapabilitiesHelper.registerCapability(IMjReadable.class);
        CapabilitiesHelper.registerCapability(IMjPassiveProvider.class);

        CAP_CONNECTOR = CapabilitiesHelper.ensureRegistration(CAP_CONNECTOR_FIRST, IMjConnector.class);
        CAP_RECEIVER = CapabilitiesHelper.ensureRegistration(CAP_RECEIVER_FIRST, IMjReceiver.class);
        CAP_REDSTONE_RECEIVER = CapabilitiesHelper.ensureRegistration(CAP_REDSTONE_RECEIVER_FIRST, IMjRedstoneReceiver.class);
        CAP_READABLE = CapabilitiesHelper.ensureRegistration(CAP_READABLE_FIRST, IMjReadable.class);
        CAP_PASSIVE_PROVIDER = CapabilitiesHelper.ensureRegistration(CAP_PASSIVE_PROVIDER_FIRST, IMjPassiveProvider.class);
    }
}
