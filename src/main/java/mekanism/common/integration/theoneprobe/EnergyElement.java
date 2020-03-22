package mekanism.common.integration.theoneprobe;

import io.netty.buffer.ByteBuf;
import mekanism.api.math.FloatingLong;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class EnergyElement extends TOPElement {

    public static int ID;

    private final FloatingLong energy;
    private final FloatingLong maxEnergy;

    public EnergyElement(FloatingLong energy, FloatingLong maxEnergy) {
        super(0xFF000000, 0xFFFFFF);
        this.energy = energy;
        this.maxEnergy = maxEnergy;
    }

    public static EnergyElement fromBuffer(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        return new EnergyElement(FloatingLong.fromBuffer(buffer), FloatingLong.fromBuffer(buffer));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        energy.writeToBuffer(buffer);
        maxEnergy.writeToBuffer(buffer);
    }

    @Override
    public int getScaledLevel(int level) {
        if (energy.equals(FloatingLong.MAX_VALUE)) {
            return level;
        }
        return (int) (level * energy.divideToLevel(maxEnergy));
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return MekanismRenderer.energyIcon;
    }

    @Override
    public ITextComponent getText() {
        return EnergyDisplay.of(energy, maxEnergy).getTextComponent();
    }

    @Override
    public int getID() {
        return ID;
    }
}