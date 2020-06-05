package mekanism.common.integration.theoneprobe;

import mekanism.api.math.FloatingLong;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class EnergyElement extends TOPElement {

    private final FloatingLong energy;
    private final FloatingLong maxEnergy;

    public EnergyElement(FloatingLong energy, FloatingLong maxEnergy) {
        super(0xFF000000, 0xFFFFFF);
        this.energy = energy;
        this.maxEnergy = maxEnergy;
    }

    public EnergyElement(PacketBuffer buf) {
        this(FloatingLong.readFromBuffer(buf), FloatingLong.readFromBuffer(buf));
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        energy.writeToBuffer(buf);
        maxEnergy.writeToBuffer(buf);
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
        return TOPProvider.ENERGY_ELEMENT_ID;
    }
}