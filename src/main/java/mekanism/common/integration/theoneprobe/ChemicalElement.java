package mekanism.common.integration.theoneprobe;

import javax.annotation.Nonnull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public abstract class ChemicalElement<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends TOPElement {

    @Nonnull
    protected final STACK stored;
    protected final int capacity;

    protected ChemicalElement(@Nonnull STACK stored, int capacity) {
        super(0xFF000000, 0xFFFFFF);
        this.stored = stored;
        this.capacity = capacity;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        ChemicalUtils.writeChemicalStack(buf, stored);
        buf.writeVarInt(capacity);
    }

    @Override
    public int getScaledLevel(int level) {
        if (capacity == 0 || stored.getAmount() == Integer.MAX_VALUE) {
            return level;
        }
        return stored.getAmount() * level / capacity;
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return stored.isEmpty() ? null : MekanismRenderer.getChemicalTexture(stored.getType());
    }

    protected abstract ILangEntry getStoredFormat();

    @Override
    public ITextComponent getText() {
        int amount = stored.getAmount();
        if (amount == Integer.MAX_VALUE) {
            return MekanismLang.GENERIC_STORED.translate(stored.getType(), MekanismLang.INFINITE);
        }
        return getStoredFormat().translate(stored.getType(), amount);
    }

    @Override
    protected boolean applyRenderColor() {
        MekanismRenderer.color(stored.getType());
        return true;
    }
}