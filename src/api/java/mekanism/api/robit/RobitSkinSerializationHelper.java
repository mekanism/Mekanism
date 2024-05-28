package mekanism.api.robit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

/**
 * Helper class for dealing with {@link RobitSkin Robit Skin} (de)serialization.
 *
 * @since 10.4.0
 */
public class RobitSkinSerializationHelper {

    private RobitSkinSerializationHelper() {
    }

    /**
     * Codec for (de)serializing robit skins inline.
     */
    public static final Codec<RobitSkin> DIRECT_CODEC = MekanismAPI.ROBIT_SKIN_SERIALIZER_REGISTRY.byNameCodec().dispatch(RobitSkin::codec, Function.identity());

    /**
     * Codec for referring to robit skins by id in other datapack registry files. Can only be used with {@link net.minecraft.resources.RegistryOps}.
     */
    public static final Codec<Holder<RobitSkin>> REFERENCE_CODEC = RegistryFileCodec.create(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME, DIRECT_CODEC);

    /**
     * Codec for referring to robit skins by id, list of id, or tags. Can only be used with {@link net.minecraft.resources.RegistryOps}.
     */
    public static final Codec<HolderSet<RobitSkin>> LIST_CODEC = RegistryCodecs.homogeneousList(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME, DIRECT_CODEC);

    /**
     * Codec for sending {@link RobitSkin}'s over the network.
     *
     * @implNote This also happens to be the codec for serializing and deserializing {@link BasicRobitSkin}s as the client doesn't require knowledge about unlock
     * conditions.
     */
    public static final MapCodec<RobitSkin> NETWORK_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          ExtraCodecs.nonEmptyList(ResourceLocation.CODEC.listOf()).fieldOf(SerializationConstants.TEXTURES).forGetter(RobitSkin::textures),
          ResourceLocation.CODEC.optionalFieldOf(SerializationConstants.CUSTOM_MODEL).forGetter(skin -> Optional.ofNullable(skin.customModel()))
    ).apply(builder, (textures, model) -> new BasicRobitSkin(textures, model.orElse(null))));
    /**
     * Codec for serializing and deserializing {@link AdvancementBasedRobitSkin}'s over the network.
     */
    public static final MapCodec<AdvancementBasedRobitSkin> ADVANCEMENT_BASED_ROBIT_SKIN_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          ExtraCodecs.nonEmptyList(ResourceLocation.CODEC.listOf()).fieldOf(SerializationConstants.TEXTURES).forGetter(RobitSkin::textures),
          ResourceLocation.CODEC.optionalFieldOf(SerializationConstants.CUSTOM_MODEL).forGetter(skin -> Optional.ofNullable(skin.customModel())),
          ResourceLocation.CODEC.fieldOf(SerializationConstants.ADVANCEMENT).forGetter(AdvancementBasedRobitSkin::advancement)
    ).apply(builder, (textures, model, advancement) -> new AdvancementBasedRobitSkin(textures, model.orElse(null), advancement)));
}