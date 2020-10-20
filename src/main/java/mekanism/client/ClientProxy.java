package mekanism.client;

import javax.annotation.Nullable;
import mekanism.common.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

/**
 * Client proxy for the Mekanism mod.
 *
 * @author AidanBrady
 */
public class ClientProxy extends CommonProxy {

    @Nullable
    @Override
    public World tryGetMainWorld() {
        return Minecraft.getInstance().world;
    }
}