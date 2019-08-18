package mekanism.api.block;

import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

public interface IBlockDisableable {

    boolean isEnabled();

    /**
     * @apiNote Should only be used from the configs
     */
    void setEnabledConfigReference(BooleanValue enabledReference);
}