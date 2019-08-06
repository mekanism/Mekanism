package mekanism.client;

import mekanism.common.Mekanism;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientPlayerTracker {

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
        Mekanism.playerState.clearPlayer(event.player.getUniqueID());
        Mekanism.freeRunnerOn.remove(event.player.getUniqueID());
    }
}