package mekanism.common;

import mekanism.common.item.ItemJetpack;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author ThatGamerBlue
 */
public class CommonEventHandler {

    @EventHandler
    public void onPlayerTick(TickEvent.PlayerTickEvent event){
        if(event.phase == TickEvent.Phase.START && event.side == Side.SERVER){
            if(!(event.player.inventory.armorInventory[1].getItem() instanceof ItemJetpack)){
                event.player.capabilities.allowFlying = false;
            } else {
                event.player.capabilities.allowFlying = true;
            }
        }
    }
}
