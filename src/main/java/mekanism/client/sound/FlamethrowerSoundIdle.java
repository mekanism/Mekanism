package mekanism.client.sound;

import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class FlamethrowerSoundIdle extends PlayerSound {

    public FlamethrowerSoundIdle(@NotNull Player player) {
        super(player, MekanismSounds.FLAMETHROWER_IDLE);
    }

    @Override
    public boolean shouldPlaySound(@NotNull Player player) {
        if (player.isUsingItem()) {
            InteractionHand usedHand = player.getUsedItemHand();
            if (player.getItemInHand(usedHand).getItem() instanceof ItemFlamethrower) {
                //Active item is a flamethrower, idle sound should not be played
                return false;
            }
            //If we the used item isn't a flamethrower, return that we should play the sound if the item in the other hand is an idle flamethrower
            return ItemFlamethrower.isIdleFlamethrower(player, usedHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        }
        //If the player isn't using an item, return that we should play the sound if they have an idle flamethrower in either hand
        return ItemFlamethrower.isIdleFlamethrower(player, InteractionHand.MAIN_HAND) || ItemFlamethrower.isIdleFlamethrower(player, InteractionHand.OFF_HAND);
    }
}