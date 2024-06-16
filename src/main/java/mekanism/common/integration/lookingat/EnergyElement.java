package mekanism.common.integration.lookingat;

import com.google.common.primitives.UnsignedLongs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import mekanism.api.SerializationConstants;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.Unsigned;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class EnergyElement extends LookingAtElement {

    public static final MapCodec<EnergyElement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
          Codec.LONG.fieldOf(SerializationConstants.ENERGY).forGetter(EnergyElement::getEnergy),
          Codec.LONG.fieldOf(SerializationConstants.MAX).forGetter(EnergyElement::getMaxEnergy)
    ).apply(instance, EnergyElement::new));
    public static final StreamCodec<ByteBuf, EnergyElement> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_LONG, EnergyElement::getEnergy,
          ByteBufCodecs.VAR_LONG, EnergyElement::getMaxEnergy,
          EnergyElement::new
    );

    protected final @Unsigned long energy;
    protected final @Unsigned long maxEnergy;

    public EnergyElement(@Unsigned long energy, @Unsigned long maxEnergy) {
        super(0xFF000000, 0xFFFFFF);
        this.energy = energy;
        this.maxEnergy = maxEnergy;
    }

    @Override
    public int getScaledLevel(int level) {
        if (energy == UnsignedLongs.MAX_VALUE) {
            return level;
        }
        return (int) (level * energy.divideToLevel(maxEnergy));
    }

    public @Unsigned long getEnergy() {
        return energy;
    }

    public @Unsigned long getMaxEnergy() {
        return maxEnergy;
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return MekanismRenderer.energyIcon;
    }

    @Override
    public Component getText() {
        return EnergyDisplay.of(energy, maxEnergy).getTextComponent();
    }

    @Override
    public ResourceLocation getID() {
        return LookingAtUtils.ENERGY;
    }
}