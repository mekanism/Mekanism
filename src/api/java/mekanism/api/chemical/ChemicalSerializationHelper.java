package mekanism.api.chemical;

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
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.ExtraCodecs;

/// Helper class for dealing with [Chemical] (de)serialization.
///
/// @since 10.8.0
public class ChemicalSerializationHelper {

    private ChemicalSerializationHelper() {
    }

    /// Codec for (de)serializing chemicals inline.
    public static final Codec<Chemical> DIRECT_CODEC = MekanismAPI.CHEMICAL_SERIALIZER_REGISTRY.byNameCodec().dispatch(Chemical::codec, Function.identity());

    /// Codec for referring to chemicals by id in other datapack registry files. Can only be used with [net.minecraft.resources.RegistryOps].
    public static final Codec<Holder<Chemical>> REFERENCE_CODEC = RegistryFileCodec.create(MekanismAPI.CHEMICAL_REGISTRY_NAME, DIRECT_CODEC);

    /// Codec for referring to chemicals by id, list of id, or tags. Can only be used with [net.minecraft.resources.RegistryOps].
    public static final Codec<HolderSet<Chemical>> LIST_CODEC = RegistryCodecs.homogeneousList(MekanismAPI.CHEMICAL_REGISTRY_NAME, DIRECT_CODEC);

    /// Codec for sending [Chemical]'s over the network.
    ///
    /// @implNote This also happens to be the codec for serializing and deserializing [basic chemicals][BasicChemical].
    public static final MapCodec<Chemical> NETWORK_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          Identifier.CODEC.fieldOf(SerializationConstants.ICON).forGetter(Chemical::icon),
          ExtraCodecs.STRING_ARGB_COLOR.fieldOf(SerializationConstants.TINT).forGetter(Chemical::tint),
          ExtraCodecs.STRING_ARGB_COLOR.optionalFieldOf(SerializationConstants.COLOR_REPRESENTATION).forGetter(chemical -> {
              if (chemical.tint() == chemical.colorRepresentation()) {
                  return Optional.empty();
              }
              return Optional.of(chemical.colorRepresentation());
          })
    ).apply(builder, (icon, tint, colorRepresentation) -> new BasicChemical(icon, tint, colorRepresentation.orElse(tint))));
}