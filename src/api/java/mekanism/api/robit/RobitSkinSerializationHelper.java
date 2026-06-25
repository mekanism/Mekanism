package mekanism.api.robit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.MekanismRegistries;
import mekanism.api.SerializationConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.ExtraCodecs;

/// Helper class for dealing with [`Robit Skin`][RobitSkin] (de)serialization.
///
/// @since 10.4.0
public class RobitSkinSerializationHelper {

    private RobitSkinSerializationHelper() {
    }

    /// Codec for (de)serializing robit skins inline.
    public static final Codec<RobitSkin> DIRECT_CODEC = MekanismRegistries.ROBIT_SKIN_SERIALIZERS.byNameCodec().dispatch(RobitSkin::codec, Function.identity());

    /// Codec for referring to robit skins by id in other datapack registry files. Can only be used with [net.minecraft.resources.RegistryOps].
    public static final Codec<Holder<RobitSkin>> REFERENCE_CODEC = RegistryFileCodec.create(MekanismRegistries.Keys.ROBIT_SKINS, DIRECT_CODEC);

    /// Codec for referring to robit skins by id, list of id, or tags. Can only be used with [net.minecraft.resources.RegistryOps].
    public static final Codec<HolderSet<RobitSkin>> LIST_CODEC = RegistryCodecs.homogeneousList(MekanismRegistries.Keys.ROBIT_SKINS, DIRECT_CODEC);

    /// Codec for sending [RobitSkin]'s over the network.
    ///
    /// @implNote This also happens to be the codec for serializing and deserializing [BasicRobitSkin]s as the client doesn't require knowledge about unlock conditions.
    public static final MapCodec<RobitSkin> NETWORK_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          ExtraCodecs.nonEmptyList(Identifier.CODEC.listOf()).fieldOf(SerializationConstants.TEXTURES).forGetter(RobitSkin::textures),
          Identifier.CODEC.optionalFieldOf(SerializationConstants.CUSTOM_MODEL).forGetter(skin -> Optional.ofNullable(skin.customModel()))
    ).apply(builder, (textures, model) -> new BasicRobitSkin(textures, model.orElse(null))));
    /// Codec for serializing and deserializing [AdvancementBasedRobitSkin]'s over the network.
    public static final MapCodec<AdvancementBasedRobitSkin> ADVANCEMENT_BASED_ROBIT_SKIN_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          ExtraCodecs.nonEmptyList(Identifier.CODEC.listOf()).fieldOf(SerializationConstants.TEXTURES).forGetter(RobitSkin::textures),
          Identifier.CODEC.optionalFieldOf(SerializationConstants.CUSTOM_MODEL).forGetter(skin -> Optional.ofNullable(skin.customModel())),
          Identifier.CODEC.fieldOf(SerializationConstants.ADVANCEMENT).forGetter(AdvancementBasedRobitSkin::advancement)
    ).apply(builder, (textures, model, advancement) -> new AdvancementBasedRobitSkin(textures, model.orElse(null), advancement)));
}