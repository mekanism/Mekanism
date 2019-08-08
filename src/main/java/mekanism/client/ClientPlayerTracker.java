package mekanism.client;

import mekanism.common.Mekanism;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ClientPlayerTracker {

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
        Mekanism.playerState.clearPlayer(event.getPlayer().getUniqueID());
        Mekanism.freeRunnerOn.remove(event.getPlayer().getUniqueID());
    }
}