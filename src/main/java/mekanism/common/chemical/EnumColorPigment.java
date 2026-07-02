package mekanism.common.chemical;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalSerializer;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismChemicals;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Range;

public record EnumColorPigment(Identifier icon, EnumColor color, @Range(from = 0, to = 15) int lightLevel) implements Chemical {

    private static final Identifier PIGMENT_ICON = Mekanism.rl("mek_chemical/pigment/base");
    private static final int LIGHT_LEVEL = 0;

    public static final MapCodec<EnumColorPigment> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          Identifier.CODEC.optionalFieldOf(SerializationConstants.ICON, PIGMENT_ICON).forGetter(Chemical::icon),
          EnumColor.CODEC.fieldOf(SerializationConstants.COLOR).forGetter(EnumColorPigment::color),
          ExtraCodecs.intRange(0, Level.MAX_BRIGHTNESS).optionalFieldOf(SerializationConstants.LIGHT_LEVEL, LIGHT_LEVEL).forGetter(Chemical::lightLevel)
    ).apply(builder, EnumColorPigment::new));

    public EnumColorPigment(EnumColor color) {
        this(PIGMENT_ICON, color, LIGHT_LEVEL);
    }

    @Override
    public int tint() {
        return color.getPackedColor();
    }

    @Override
    public ChemicalSerializer serializer() {
        return MekanismChemicals.ENUM_COLOR_PIGMENT.get();
    }
}