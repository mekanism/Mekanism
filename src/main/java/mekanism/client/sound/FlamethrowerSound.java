package mekanism.client.sound;

import mekanism.client.ClientTickHandler;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class FlamethrowerSound extends PlayerSound {

    private final boolean active;

    private FlamethrowerSound(@NotNull Player player, boolean active) {
        super(player, active ? MekanismSounds.FLAMETHROWER_ACTIVE : MekanismSounds.FLAMETHROWER_IDLE);
        this.active = active;
    }

    @Override
    public boolean shouldPlaySound(@NotNull Player player) {
        if (!ClientTickHandler.hasFlamethrower(player)) {
            return false;
        }
        return ClientTickHandler.isFlamethrowerOn(player) == active;
    }

    public static class Active extends FlamethrowerSound {

        public Active(@NotNull Player player) {
            super(player, true);
        }
    }

    public static class Idle extends FlamethrowerSound {

        public Idle(@NotNull Player player) {
            super(player, false);
        }
    }
}