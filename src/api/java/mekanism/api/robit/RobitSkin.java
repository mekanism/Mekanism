package mekanism.api.robit;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.providers.IRobitSkinProvider;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistryEntry;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RobitSkin extends ForgeRegistryEntry<RobitSkin> implements IRobitSkinProvider {

    private final List<ResourceLocation> textures;
    private String translationKey;

    /**
     * Creates a new Robit skin that makes use of the given textures when the Robit moves in the given order.
     *
     * @param textures Textures to use for the skin. If this is an empty array then {@link #getTextures()} must be overridden.
     */
    public RobitSkin(ResourceLocation... textures) {
        Objects.requireNonNull(textures, "Textures cannot be null.");
        if (textures.length == 0) {
            this.textures = Collections.emptyList();
        } else {
            this.textures = List.of(textures);
        }
    }

    /**
     * Gets the location of the custom json model for this skin. In general, it is probably a good idea to base it off the existing robit model's json except with any
     * small changes this skin requires. For an example of the syntax the default model's location would be {@code mekanism:item/robit}.
     *
     * @return Custom model or {@code null} if the default model should be used.
     *
     * @apiNote This is mostly untested currently so if you run into issues please report them.
     */
    @Nullable
    public ResourceLocation getCustomModel() {
        return null;
    }

    /**
     * Gets the list of textures that will be used for this skin. The textures should be located in the asset location: {@code
     * <namespace>/textures/entity/robit/<path>.png}
     * <br><br>
     * It is <strong>important</strong> that this list has at <strong>least ONE</strong> element in it.
     * <br><br>
     * Every three ticks of the robit being alive if it has moved, the selected texture of this skin is incremented to the next one in the list, and then it repeats from
     * the start. This allows skins to define "movement" changes such as how the Robit's treads appear to be moving in the base skin.
     *
     * @return Unmodifiable list of textures for this skin.
     */
    public List<ResourceLocation> getTextures() {
        return textures;
    }

    /**
     * Checks if the given player has access to select this skin.
     *
     * @param player Player to check.
     *
     * @return {@code true} if the player has access.
     */
    public boolean isUnlocked(@Nonnull Player player) {
        //TODO: Have some skins that are potentially locked as patreon rewards?
        return true;
    }

    @Nonnull
    @Override
    public final RobitSkin getSkin() {
        return this;
    }

    @Override
    public String getTranslationKey() {
        if (translationKey == null) {
            translationKey = Util.makeDescriptionId("robit_skin", getRegistryName());
        }
        return translationKey;
    }

    @Override
    public Component getTextComponent() {
        return TextComponentUtil.translate(getTranslationKey());
    }
}