package mekanism.common;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.client.ClientProxy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;
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

    public PlayerEntity getPlayer(Supplier<Context> context) {
        return context.get().getSender();
    }

    @Nullable
    public World tryGetMainWorld() {
        return ServerLifecycleHooks.getCurrentServer().func_241755_D_();
    }
}