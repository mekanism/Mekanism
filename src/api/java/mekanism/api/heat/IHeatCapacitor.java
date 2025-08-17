package mekanism.api.heat;

import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.util.INBTSerializable;

@NothingNullByDefault
public interface IHeatCapacitor extends INBTSerializable<CompoundTag>, IContentsListener {

    /**
     * Returns the temperature of this capacitor.
     *
     * @return Temperature of this capacitor. Always bounded by absolute zero (0 degrees kelvin).
     */
    double getTemperature();

    /**
     * Returns the inverse conduction coefficient of this capacitor. This value defines how much heat is allowed to be dissipated. The larger the number the less heat can
     * dissipate. The trade-off is that it also allows for lower amounts of heat to be inserted.
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

    /**
     * Checks if this heat capacitor is currently at the ambient temperature of its surroundings.
     *
     * @return {@code true} if this capacitor is currently at the ambient temperature of the environment it is in.
     *
     * @implNote This method should be overridden to take into account any variable ambient temperature data such as biomes.
     * @since 10.7.15
     */
    default boolean isAmbientTemperature() {
        return Mth.equal(getTemperature(), HeatAPI.AMBIENT_TEMP);
    }

    @Override
    default CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putDouble(SerializationConstants.STORED, getHeat());
        return nbt;
    }
}
