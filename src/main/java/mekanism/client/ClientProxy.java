package mekanism.client;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.common.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

/**
 * Client proxy for the Mekanism mod.
 *
 * @author AidanBrady
 */
public class ClientProxy extends CommonProxy {

    @Override
    public PlayerEntity getPlayer(Supplier<Context> context) {
        if (context.get().getDirection().getReceptionSide().isServer()) {
            return super.getPlayer(context);
        }
        return Minecraft.getInstance().player;
    }

    @Nullable
    @Override
    public World tryGetMainWorld() {
        return Minecraft.getInstance().world;
    }
}