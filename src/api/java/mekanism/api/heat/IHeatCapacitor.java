package mekanism.api.heat;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.NBTConstants;
import mekanism.api.math.FloatingLong;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IHeatCapacitor extends INBTSerializable<CompoundNBT> {

    /**
     * Returns the temperature in this capacitor.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link FloatingLong} <em>MUST NOT</em> be modified. This method is not for altering the internal temperature. Any implementers
     * who are able to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the value returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED FLOATING LONG</em></strong>
     * </p>
     *
     * @return Temperature of this capacitor. {@link FloatingLong#ZERO} if the capacitor has a temperature of absolute zero.
     */
    FloatingLong getTemperature();

    /**
     * Returns the inverse conduction coefficient of this capacitor. This value defines how much heat is allowed to be dissipated. The larger the number the less heat can
     * dissipate. The trade off is that it also allows for lower amounts of heat to be inserted.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link FloatingLong} <em>MUST NOT</em> be modified. This method is not for altering the conduction coefficient. Any implementers
     * who are able to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the value returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED FLOATING LONG</em></strong>
     * </p>
     *
     * @return Inverse conduction coefficient of this capacitor.
     *
     * @apiNote Must be greater than {@link FloatingLong#ZERO}
     */
    FloatingLong getInverseConduction();

    /**
     * Returns the inverse insulation coefficient for this. The larger the value the less heat dissipates into the environment.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link FloatingLong} <em>MUST NOT</em> be modified. This method is not for altering the conduction coefficient. Any implementers
     * who are able to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the value returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED FLOATING LONG</em></strong>
     * </p>
     *
     * @return Inverse insulation coefficient of this capacitor.
     */
    FloatingLong getInverseInsulation();

    /**
     * Returns the heat capacity of this capacitor. This number can be thought of as the specific heat of the capacitor.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link FloatingLong} <em>MUST NOT</em> be modified. This method is not for altering the heat capacity. Any implementers who are
     * able to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the value returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED FLOATING LONG</em></strong>
     * </p>
     *
     * @return Heat capacity of this capacitor.
     *
     * @apiNote Must be at least {@link FloatingLong#ONE}
     */
    FloatingLong getHeatCapacity();

    /**
     * Returns the heat stored in this capacitor.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link FloatingLong} <em>MUST NOT</em> be modified. This method is not for altering the internal stored heat. Any implementers
     * who are able to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the value returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED FLOATING LONG</em></strong>
     * </p>
     *
     * @return Heat stored in this capacitor. {@link FloatingLong#ZERO} if the capacitor has no heat stored.
     */
    FloatingLong getHeat();

    /**
     * Overrides the amount of heat in this {@link IHeatCapacitor}.
     *
     * @param heat Heat to set this capacitor's storage to (may be {@link FloatingLong#ZERO}).
     *
     * @throws RuntimeException if the handler is called in a way that the handler was not expecting. Such as if it was not expecting this to be called at all.
     * @implNote If the internal amount does get updated make sure to call {@link #onContentsChanged()}
     */
    void setHeat(FloatingLong heat);

    /**
     * Handles transferring a {@link HeatPacket} to this capacitor.
     *
     * @param transfer The {@link HeatPacket} being transferred.
     *
     * @implNote If the internal amount does get updated make sure to call {@link #onContentsChanged()}
     */
    void handleHeat(HeatPacket transfer);

    /**
     * Called when the contents of this capacitor changes.
     */
    void onContentsChanged();

    @Override
    default CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString(NBTConstants.STORED, getTemperature().toString());
        return nbt;
    }
}
