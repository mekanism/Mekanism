package mekanism.common.integration.lookingat;

import io.netty.buffer.ByteBuf;
import mekanism.api.math.MathUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public non-sealed class EnergyElement extends LookingAtElement {

    public static final StreamCodec<ByteBuf, EnergyElement> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_LONG, EnergyElement::getEnergy,
          ByteBufCodecs.VAR_LONG, EnergyElement::getMaxEnergy,
          EnergyElement::new
    );

    protected final long energy;
    protected final long maxEnergy;

    public EnergyElement(long energy, long maxEnergy) {
        super(0xFF000000, 0xFFFFFFFF);
        this.energy = energy;
        this.maxEnergy = maxEnergy;
    }

    @Override
    public int getScaledLevel(int level) {
        if (energy == Long.MAX_VALUE) {
            return level;
        }
        return (int) (level * MathUtils.divideToLevel(energy, maxEnergy));
    }

    public long getEnergy() {
        return energy;
    }

    public long getMaxEnergy() {
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
    public Identifier getID() {
        return LookingAtUtils.ENERGY;
    }
}