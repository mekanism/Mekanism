package mekanism.api.gas;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//TODO: Convert this to a proper forge registry
public class GasRegistry {

    private static ArrayList<Gas> registeredGasses = new ArrayList<>();

    private static Logger LOG = LogManager.getLogger("Mekanism GasRegistry");

    /**
     * Register a new gas into GasRegistry. Call this BEFORE post-init.
     *
     * @param gas - Gas to register
     *
     * @return the gas that has been registered, pulled right out of GasRegistry
     */
    public static Gas register(Gas gas) {
        if (gas == null) {
            return null;
        }

        //TODO: Is this the proper way to check this
        if (EffectiveSide.get() == LogicalSide.CLIENT) {
            if (hasAlreadyStitched()) {
                gas.updateIcon(Minecraft.getInstance().getTextureMap());
                if (gas.getSpriteRaw() == null) {
                    LOG.error("Gas {} registered post texture stitch without valid sprite!", gas.getName());
                }
            }
        }
        registeredGasses.add(gas);
        return getGas(gas.getName());
    }

    /**
     * Gets the gas associated with the defined ID.
     *
     * @param id - ID to check
     *
     * @return gas associated with defined ID
     */
    public static Gas getGas(int id) {
        if (id == -1) {
            return null;
        }

        return registeredGasses.get(id);
    }

    /**
     * Gets the gas associated with the defined fluid.
     *
     * @param f - fluid to check
     *
     * @return the gas associated with the fluid
     */
    @Nullable
    public static Gas getGas(@Nonnull Fluid f) {
        if ( f == Fluids.EMPTY) {
            return null;
        }
        for (Gas gas : getRegisteredGasses()) {
            if (gas.hasFluid() && gas.getFluid() == f) {
                return gas;
            }
        }
        return null;
    }

    /**
     * Whether or not GasRegistry contains a gas with the specified name
     *
     * @param name - name to check
     *
     * @return if GasRegistry contains a gas with the defined name
     */
    public static boolean containsGas(String name) {
        return getGas(name) != null;
    }

    /**
     * Gets the list of all gasses registered in GasRegistry.
     *
     * @return a cloned list of all registered gasses
     */
    public static List<Gas> getRegisteredGasses() {
        return new ArrayList<>(registeredGasses);
    }

    /**
     * Gets the gas associated with the specified name.
     *
     * @param name - name of the gas to get
     *
     * @return gas associated with the name
     */
    public static Gas getGas(String name) {
        name = name.toLowerCase(Locale.ROOT);
        for (Gas gas : registeredGasses) {
            if (gas.getName().toLowerCase(Locale.ROOT).equals(name)) {
                return gas;
            }
        }
        return null;
    }

    /**
     * Gets the gas ID of a specified gas.
     *
     * @param gas - gas to get the ID from
     *
     * @return gas ID
     */
    public static int getGasID(Gas gas) {
        if (gas == null || !containsGas(gas.getName())) {
            return -1;
        }
        return registeredGasses.indexOf(gas);
    }

    private static boolean hasAlreadyStitched() {
        //TODO
        return false;//Loader.instance().getLoaderState().ordinal() > LoaderState.PREINITIALIZATION.ordinal();
    }
}