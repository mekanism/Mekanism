package mekanism.common.entity.skins;

import mekanism.api.robit.RobitSkin;
import mekanism.common.Mekanism;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AllayRobitSkin extends RobitSkin {

    private static final ResourceLocation MODEL = Mekanism.rl("item/robit_allay");

    public AllayRobitSkin() {
        super(Mekanism.rl( "allay"), Mekanism.rl("allay2"));
    }

    @Nullable
    @Override
    public ResourceLocation getCustomModel() {
        return MODEL;
    }

    @Override
    public boolean isUnlocked(@NotNull Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            //TODO: Do we eventually want to make a system for announcing unlocks, maybe using toast notifications
            Advancement advancement = serverPlayer.getServer().getAdvancements().getAdvancement(new ResourceLocation("husbandry/allay_deliver_item_to_player"));
            return advancement != null && serverPlayer.getAdvancements().getOrStartProgress(advancement).isDone();
        }
        //Fallback, as currently the client does not validate if a skin is unlocked
        return true;
    }
}
