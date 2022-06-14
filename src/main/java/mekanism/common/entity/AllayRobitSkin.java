package mekanism.common.entity;

import mekanism.api.robit.RobitSkin;
import mekanism.common.Mekanism;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AllayRobitSkin extends RobitSkin {

    public AllayRobitSkin() {
        super(new ResourceLocation[]{
                Mekanism.rl( "allay"), Mekanism.rl("allay2")}
        );
    }

    @Nullable
    @Override
    public ResourceLocation getCustomModel() {
        return Mekanism.rl("item/robit_allay");
    }

    @Override
    public boolean isUnlocked(@NotNull Player player) {
        return false;
    }
}
