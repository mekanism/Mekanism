package mekanism.common.lib.frequency;

import java.util.List;
import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.security.ISecurityTile;
import net.minecraft.nbt.CompoundNBT;

public interface IFrequencyHandler extends ISecurityTile {

    TileComponentFrequency getFrequencyComponent();

    default <FREQ extends Frequency> FREQ getFrequency(FrequencyType<FREQ> type) {
        return getFrequencyComponent().getFrequency(type);
    }

    /**
     * Sets the frequency from a stored super saturated identifier.
     *
     * @param frequencyCompound Frequency identity super saturated with the owner of the frequency.
     */
    default void setFrequency(FrequencyType<?> type, CompoundNBT frequencyCompound) {
        FrequencyIdentity freq = FrequencyIdentity.load(type, frequencyCompound);
        if (freq != null) {
            UUID owner;
            if (frequencyCompound.hasUUID(NBTConstants.OWNER_UUID)) {
                //TODO - 1.18: Require the compound to actually have an owner uuid stored as well
                // having a fallback to the tile's owner is mostly for properly loading legacy data
                owner = frequencyCompound.getUUID(NBTConstants.OWNER_UUID);
            } else {
                owner = getOwnerUUID();
            }
            setFrequency(type, freq, owner);
        }
    }

    /**
     * Sets or creates a frequency from the data using the given player as the frequency owner.
     */
    default void setFrequency(FrequencyType<?> type, FrequencyIdentity data, UUID player) {
        getFrequencyComponent().setFrequencyFromData(type, data, player);
    }

    /**
     * Removes a frequency of the given type if it exists and the given player is the frequency owner.
     */
    default void removeFrequency(FrequencyType<?> type, FrequencyIdentity data, UUID player) {
        getFrequencyComponent().removeFrequencyFromData(type, data, player);
    }

    default <FREQ extends Frequency> List<FREQ> getPublicCache(FrequencyType<FREQ> type) {
        return getFrequencyComponent().getPublicCache(type);
    }

    default <FREQ extends Frequency> List<FREQ> getPrivateCache(FrequencyType<FREQ> type) {
        return getFrequencyComponent().getPrivateCache(type);
    }
}