package mekanism.api.datamaps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import net.minecraft.resources.ResourceLocation;

/**
 * A {@link net.minecraft.core.registries.Registries#DAMAGE_TYPE damage type} data map that allows changing how much damage of a given type the meka suit should absorb.
 *
 * @param absorption how much damage will be absorbed. Must be between zero and one inclusive.
 *
 * @since 10.5.0
 */
public record MekaSuitAbsorption(float absorption) {

    /**
     * The ID of the data map.
     *
     * @see mekanism.api.datamaps.IMekanismDataMapTypes#mekaSuitAbsorption()
     */
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "mekasuit_absorption");

    private static final Codec<Float> ABSORPTION_CODEC = Codec.floatRange(0, 1);
    /**
     * Codec for serializing and deserializing MekaSuit damage absorption values.
     */
    public static final Codec<MekaSuitAbsorption> CODEC = Codec.withAlternative(RecordCodecBuilder.create(in -> in.group(
                ABSORPTION_CODEC.fieldOf(SerializationConstants.ABSORPTION).forGetter(MekaSuitAbsorption::absorption)
          ).apply(in, MekaSuitAbsorption::new)
    ), ABSORPTION_CODEC.xmap(MekaSuitAbsorption::new, MekaSuitAbsorption::absorption));

    public MekaSuitAbsorption {
        if (absorption < 0 || absorption > 1) {
            throw new IllegalArgumentException("Damage absorption ratio must be between zero and one inclusive");
        }
    }
}
