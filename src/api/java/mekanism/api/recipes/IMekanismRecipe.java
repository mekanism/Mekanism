package mekanism.api.recipes;

import net.minecraft.network.PacketBuffer;

public interface IMekanismRecipe {

    /**
     * Writes this recipe to a PacketBuffer.
     * @param buffer The buffer to write to.
     */
    void write(PacketBuffer buffer);
}