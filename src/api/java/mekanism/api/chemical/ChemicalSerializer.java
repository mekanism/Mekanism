package mekanism.api.chemical;

import com.mojang.serialization.MapCodec;

/// @param codec        the codec which serializes and deserializes a [chemical][Chemical].
/// @param networkCodec the codec which serializes and deserializes a [chemical][Chemical] for use in sending over the network.
///
/// @implNote The serializer should be registered in the [chemical serializer registry][mekanism.api.MekanismRegistries#CHEMICAL_SERIALIZERS].
/// @since 10.8.0
public record ChemicalSerializer(MapCodec<? extends Chemical> codec, MapCodec<? extends Chemical> networkCodec) {

    /// @param codec the codec which serializes and deserializes a [chemical][Chemical].
    ///
    /// @implNote [ChemicalSerializationHelper#DEFAULT_NETWORK_CODEC] will be used for serializing the [chemical][Chemical] over the network.
    public static ChemicalSerializer defaultNetwork(MapCodec<? extends Chemical> codec) {
        return new ChemicalSerializer(codec, ChemicalSerializationHelper.DEFAULT_NETWORK_CODEC);
    }

    /// @param codec the codec which serializes and deserializes a [chemical][Chemical]. This codec will also be used for serializing over the network.
    public static ChemicalSerializer both(MapCodec<? extends Chemical> codec) {
        return new ChemicalSerializer(codec, codec);
    }
}