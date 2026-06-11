package mekanism.client.sound;

import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.world.entity.player.Player;

public class FlamethrowerSoundActive extends PlayerSound {

    public FlamethrowerSoundActive(Player player) {
        super(player, MekanismSounds.FLAMETHROWER_ACTIVE);
    }

    @Override
    public boolean shouldPlaySound(Player player) {
        //If the item being used is a flamethrower, return that we should play the sound
        return player.isUsingItem() && player.getItemInHand(player.getUsedItemHand()).getItem() instanceof ItemFlamethrower;
    }
}