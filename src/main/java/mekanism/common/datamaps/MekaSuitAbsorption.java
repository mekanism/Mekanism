package mekanism.common.datamaps;

import com.mojang.serialization.Codec;

public record MekaSuitAbsorption(float absorption) {
    public static final Codec<MekaSuitAbsorption> CODEC = Codec.floatRange(0, 1)
            .xmap(MekaSuitAbsorption::new, MekaSuitAbsorption::absorption);
}
