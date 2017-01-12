package mekanism.common;

import mekanism.common.item.ItemJetpack;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author ThatGamerBlue
 */
public class CommonEventHandler {

    private boolean canFly = false;

    @EventHandler
    public void onPlayerTick(TickEvent.PlayerTickEvent event){
        if(event.phase == TickEvent.Phase.START && event.side == Side.SERVER){
            if(canFly){
                if(!(event.player.inventory.armorInventory[1].getItem() instanceof ItemJetpack)){
                    canFly = false;
                    event.player.capabilities.allowFlying = canFly;
                }
            } else {
                if(event.player.inventory.armorInventory[1].getItem() instanceof ItemJetpack){
                    canFly = true;
                    event.player.capabilities.allowFlying = canFly;
                }
            }
        }
    }

}
