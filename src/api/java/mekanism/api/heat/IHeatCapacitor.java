package mekanism.api.heat;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IHeatCapacitor extends INBTSerializable<CompoundNBT>, IContentsListener {

    /**
     * Returns the temperature of this capacitor.
     *
     * @return Temperature of this capacitor. Always bounded by absolute zero (0 degrees kelvin).
     */
    double getTemperature();

    /**
     * Returns the inverse conduction coefficient of this capacitor. This value defines how much heat is allowed to be dissipated. The larger the number the less heat can
     * dissipate. The trade off is that it also allows for lower amounts of heat to be inserted.
     *
     * @return Inverse conduction coefficient of this capacitor.
     *
     * @apiNote Must be greater than {@code 0}
     */
    double getInverseConduction();

    /**
     * Returns the inverse insulation coefficient for this. The larger the value the less heat dissipates into the environment.
     *
     * @return Inverse insulation coefficient of this capacitor.
     */
    double getInverseInsulation();

    /**
     * Returns the heat capacity of this capacitor. This number can be thought of as specific heat x mass of the capacitor itself.
     *
     * @return Heat capacity of this capacitor.
     *
     * @apiNote Must be at least {@code 1}
     */
    double getHeatCapacity();

    /**
     * Returns the heat stored in this capacitor.
     *
     * @return Heat stored in this capacitor.
     */
    double getHeat();

    /**
     * Overrides the amount of heat in this {@link IHeatCapacitor}.
     *
     * @param heat Heat to set this capacitor's storage to (may be {@code 0}).
     *
     * @throws RuntimeException if the handler is called in a way that the handler was not expecting. Such as if it was not expecting this to be called at all.
     * @implNote If the internal amount does get updated make sure to call {@link #onContentsChanged()}
     */
    void setHeat(double heat);

    /**
     * Handles a change of heat in this capacitor. Can be positive or negative.
     *
     * @param transfer The amount being transferred.
     *
     * @implNote If the internal amount does get updated make sure to call {@link #onContentsChanged()}
     */
    void handleHeat(double transfer);

    @Override
    default CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putDouble(NBTConstants.STORED, getHeat());
        return nbt;
    }
}
