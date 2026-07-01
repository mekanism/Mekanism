package mekanism.api.chemical;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mekanism.api.MekanismRegistries;
import mekanism.api.SerializationConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;

/// Helper class for dealing with [Chemical] (de)serialization.
///
/// @since 10.8.0
public class ChemicalSerializationHelper {

    private ChemicalSerializationHelper() {
    }

    /// Codec for (de)serializing chemicals inline.
    public static final Codec<Chemical> DIRECT_CODEC = MekanismRegistries.CHEMICAL_SERIALIZERS.byNameCodec().dispatch(Chemical::serializer, ChemicalSerializer::codec);

    /// Codec for referring to chemicals by id in other datapack registry files. Can only be used with [net.minecraft.resources.RegistryOps].
    public static final Codec<Holder<Chemical>> REFERENCE_CODEC = RegistryFileCodec.create(MekanismRegistries.Keys.CHEMICAL, DIRECT_CODEC);

    /// Codec for referring to chemicals by id, list of id, or tags. Can only be used with [net.minecraft.resources.RegistryOps].
    public static final Codec<HolderSet<Chemical>> LIST_CODEC = RegistryCodecs.homogeneousList(MekanismRegistries.Keys.CHEMICAL, DIRECT_CODEC);

    /// Default codec for sending [Chemical]'s over the network.
    ///
    /// @implNote This also happens to be the codec for serializing and deserializing [basic chemicals][BasicChemical].
    public static final MapCodec<Chemical> DEFAULT_NETWORK_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          Identifier.CODEC.fieldOf(SerializationConstants.ICON).forGetter(Chemical::icon),
          ExtraCodecs.STRING_ARGB_COLOR.fieldOf(SerializationConstants.TINT).forGetter(Chemical::tint),
          ExtraCodecs.STRING_ARGB_COLOR.optionalFieldOf(SerializationConstants.COLOR_REPRESENTATION).forGetter(chemical -> {
              if (chemical.tint() == chemical.colorRepresentation()) {
                  return Optional.empty();
              }
              return Optional.of(chemical.colorRepresentation());
          }),
          ExtraCodecs.intRange(0, Level.MAX_BRIGHTNESS).optionalFieldOf(SerializationConstants.LIGHT_LEVEL, 0).forGetter(Chemical::lightLevel)
    ).apply(builder, (icon, tint, colorRepresentation, lightLevel) -> new BasicChemical(icon, tint, colorRepresentation.orElse(tint), lightLevel)));

    /// Codec for sending [Chemical]'s over the network.
    public static final Codec<Chemical> NETWORK_CODEC = MekanismRegistries.CHEMICAL_SERIALIZERS.byNameCodec().dispatch(Chemical::serializer, ChemicalSerializer::networkCodec);
}