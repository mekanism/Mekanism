package mekanism.common;

import javax.annotation.Nullable;
import mekanism.client.ClientProxy;
import net.minecraft.world.World;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

/**
 * Common proxy for the Mekanism mod.
 *
 * @author AidanBrady
 */
public class CommonProxy {

    public static CommonProxy createClientProxy() {
        return new ClientProxy();
    }

    @Nullable
    public World tryGetMainWorld() {
        return ServerLifecycleHooks.getCurrentServer().func_241755_D_();
    }
}