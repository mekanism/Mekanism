package mekanism.api.robit;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Prefab of a robit skin that requires a given advancement to be unlocked to use it.
 *
 * @param textures    Textures to use for the skin.
 * @param customModel Resource location of custom model relative to the "models" directory.
 * @param advancement Advancement to check a player for to see if they have this robit skin unlocked.
 *
 * @since 10.4.0
 */
@NothingNullByDefault
public record AdvancementBasedRobitSkin(List<ResourceLocation> textures, @Nullable ResourceLocation customModel, ResourceLocation advancement) implements RobitSkin {

    public AdvancementBasedRobitSkin {
        Objects.requireNonNull(advancement, "Required advancement cannot be null.");
        Objects.requireNonNull(textures, "Textures cannot be null.");
        if (textures.isEmpty()) {
            throw new IllegalArgumentException("There must be at least one texture specified.");
        }
        textures = List.copyOf(textures);
    }

    /**
     * Prefab of a robit skin that requires a given advancement to be unlocked to use it.
     *
     * @param textures    Textures to use for the skin.
     * @param advancement Advancement to check a player for to see if they have this robit skin unlocked.
     */
    public AdvancementBasedRobitSkin(List<ResourceLocation> textures, ResourceLocation advancement) {
        this(textures, null, advancement);
    }

    @Override
    public MapCodec<? extends RobitSkin> codec() {
        return RobitSkinSerializationHelper.ADVANCEMENT_BASED_ROBIT_SKIN_CODEC;
    }

    @Override
    public boolean isUnlocked(@NotNull Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = serverPlayer.getServer();
            if (server != null) {
                //TODO: Do we eventually want to make a system for announcing unlocks, maybe using toast notifications
                AdvancementHolder advancement = server.getAdvancements().get(advancement());
                return advancement != null && serverPlayer.getAdvancements().getOrStartProgress(advancement).isDone();
            }
        }
        //Fallback, as the client does not validate if a skin is unlocked
        return true;
    }
}