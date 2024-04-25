package mekanism.api.robit;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Prefab of a robit skin that is always unlocked.
 *
 * @param textures    Textures to use for the skin.
 * @param customModel Resource location of custom model relative to the "models" directory.
 *
 * @since 10.4.0
 */
@NothingNullByDefault
public record BasicRobitSkin(List<ResourceLocation> textures, @Nullable ResourceLocation customModel) implements RobitSkin {

    public BasicRobitSkin {
        Objects.requireNonNull(textures, "Textures cannot be null.");
        if (textures.isEmpty()) {
            throw new IllegalArgumentException("There must be at least one texture specified.");
        }
        textures = List.copyOf(textures);
    }

    /**
     * Prefab of a robit skin that is always unlocked.
     *
     * @param textures Textures to use for the skin.
     */
    public BasicRobitSkin(List<ResourceLocation> textures) {
        this(textures, null);
    }

    @Override
    public MapCodec<? extends RobitSkin> codec() {
        //Note: Make use of the implementation detail that the network codec is equivalent to the basic skin codec
        return RobitSkinSerializationHelper.NETWORK_CODEC;
    }
}