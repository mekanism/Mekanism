package mekanism.common.integration.lookingat;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import mekanism.api.NBTConstants;
import mekanism.api.math.FloatingLong;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class EnergyElement extends LookingAtElement {

    public static final MapCodec<EnergyElement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
          FloatingLong.CODEC.fieldOf(NBTConstants.ENERGY_STORED).forGetter(EnergyElement::getEnergy),
          FloatingLong.CODEC.fieldOf(NBTConstants.MAX).forGetter(EnergyElement::getMaxEnergy)
    ).apply(instance, EnergyElement::new));
    public static final StreamCodec<ByteBuf, EnergyElement> STREAM_CODEC = StreamCodec.composite(
          FloatingLong.STREAM_CODEC, EnergyElement::getEnergy,
          FloatingLong.STREAM_CODEC, EnergyElement::getMaxEnergy,
          EnergyElement::new
    );

    protected final FloatingLong energy;
    protected final FloatingLong maxEnergy;

    public EnergyElement(FloatingLong energy, FloatingLong maxEnergy) {
        super(0xFF000000, 0xFFFFFF);
        this.energy = energy;
        this.maxEnergy = maxEnergy;
    }

    @Override
    public int getScaledLevel(int level) {
        if (energy.equals(FloatingLong.MAX_VALUE)) {
            return level;
        }
        return (int) (level * energy.divideToLevel(maxEnergy));
    }

    public FloatingLong getEnergy() {
        return energy;
    }

    public FloatingLong getMaxEnergy() {
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