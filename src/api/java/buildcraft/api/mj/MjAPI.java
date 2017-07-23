package buildcraft.api.mj;

import java.text.DecimalFormat;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import buildcraft.api.core.APIHelper;

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
    public static final DecimalFormat MJ_DISPLAY_FORMAT = new DecimalFormat("###0.##");

    public static final IMjEffectManager EFFECT_MANAGER = APIHelper.getInstance("", IMjEffectManager.class, NullaryEffectManager.INSTANCE);

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

    // ###############
    //
    // Helpful methods
    //
    // ###############

    /** Formats a given MJ value to a player-oriented string. Note that this does not append "MJ" to the value. */
    public static String formatMj(long microMj) {
        return formatMjInternal(microMj / (double) MJ);
    }

    /** Formats a given MJ value to a player-oriented string. Note that this DOES append "*MJ" to the value. This does
     * however shorten it down to a small length, and displays "µ", "m", "K" or "M" or "G" before the MJ depending on
     * how big or small the value is. */
    public static String formatMjShort(long microJoules) {
        if (microJoules == 0) {
            return "0 Mj";
        }
        long limit = 1;
        final long nextUnitCap = 800;
        if (microJoules < nextUnitCap * limit) {// micro MJ
            return formatMjInternal(microJoules) + " µMJ";
        }
        limit *= 1000;
        if (microJoules < nextUnitCap * limit) { // milli MJ
            return formatMjInternal(microJoules / (double) limit) + " mMJ";
        }
        limit *= 1000;
        if (microJoules < nextUnitCap * limit) { // MJ
            return formatMjInternal(microJoules / (double) limit) + " MJ";
        }
        limit *= 1000;
        if (microJoules < nextUnitCap * limit) {// kilo MJ
            return formatMjInternal(microJoules / (double) limit) + " KMJ";
        }
        limit *= 1000;
        if (microJoules < nextUnitCap * limit) {// mega MJ
            return formatMjInternal(microJoules / (double) limit) + " MMJ";
        }
        limit *= 1000;
        return formatMjInternal(microJoules / (double) limit) + " GMJ";
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
        registerCapability(IMjConnector.class);
        registerCapability(IMjReceiver.class);
        registerCapability(IMjRedstoneReceiver.class);
        registerCapability(IMjReadable.class);
        registerCapability(IMjPassiveProvider.class);

        CAP_CONNECTOR = ensureRegistration(CAP_CONNECTOR_FIRST, IMjConnector.class);
        CAP_RECEIVER = ensureRegistration(CAP_RECEIVER_FIRST, IMjReceiver.class);
        CAP_REDSTONE_RECEIVER = ensureRegistration(CAP_REDSTONE_RECEIVER_FIRST, IMjRedstoneReceiver.class);
        CAP_READABLE = ensureRegistration(CAP_READABLE_FIRST, IMjReadable.class);
        CAP_PASSIVE_PROVIDER = ensureRegistration(CAP_PASSIVE_PROVIDER_FIRST, IMjPassiveProvider.class);
    }

    private static <T> void registerCapability(Class<T> clazz) {
        CapabilityManager.INSTANCE.register(clazz, new VoidStorage<T>(), new Callable<T>() {
            // No lambda because of java 6... :(
            @Override
            public T call() throws Exception {
                throw new IllegalStateException("You must create your own instances!");
            }
        });
    }

    @Nonnull
    private static <T> Capability<T> ensureRegistration(Capability<T> cap, Class<T> clazz) {
        if (cap == null) {
            throw new Error("Capability registration failed for " + clazz);
        }
        return cap;
    }

    private static class VoidStorage<T> implements Capability.IStorage<T> {
        @Override
        public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
            throw new IllegalStateException("You must create your own instances!");
        }

        @Override
        public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {
            throw new IllegalStateException("You must create your own instances!");
        }
    }

    private static String formatMjInternal(double val) {
        return MJ_DISPLAY_FORMAT.format(val);
    }
}
