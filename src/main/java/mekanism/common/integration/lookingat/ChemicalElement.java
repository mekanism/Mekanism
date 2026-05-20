package mekanism.common.integration.lookingat;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.MathUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ChemicalElement extends LookingAtElement {

    public static final MapCodec<ChemicalElement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
          ChemicalStack.OPTIONAL_CODEC.fieldOf(SerializationConstants.CHEMICAL).forGetter(ChemicalElement::getStored),
          SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(SerializationConstants.MAX).forGetter(ChemicalElement::getCapacity)
    ).apply(instance, ChemicalElement::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalElement> STREAM_CODEC = StreamCodec.composite(
          ChemicalStack.OPTIONAL_STREAM_CODEC, ChemicalElement::getStored,
          ByteBufCodecs.VAR_LONG, ChemicalElement::getCapacity,
          ChemicalElement::new
    );

    @NotNull
    protected final ChemicalStack stored;
    protected final long capacity;

    public ChemicalElement(@NotNull ChemicalStack stored, long capacity) {
        super(0xFF000000, 0xFFFFFF);
        this.stored = stored;
        this.capacity = capacity;
    }

    @Override
    public int getScaledLevel(int level) {
        if (capacity == 0 || stored.amount() == Long.MAX_VALUE) {
            return level;
        }
        return MathUtils.clampToInt(level * MathUtils.divideToLevel(stored.amount(), capacity));
    }

    @NotNull
    public ChemicalStack getStored() {
        return stored;
    }

    public long getCapacity() {
        return capacity;
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return stored.isEmpty() ? null : MekanismRenderer.getChemicalTexture(stored);
    }

    @Override
    public Component getText() {
        long amount = stored.amount();
        if (stored.isEmpty()) {
            return MekanismLang.EMPTY.translate();
        } else if (amount == Long.MAX_VALUE) {
            return MekanismLang.GENERIC_STORED.translate(stored, MekanismLang.INFINITE);
        }
        return MekanismLang.GENERIC_STORED_MB.translate(stored, TextUtils.format(amount));
    }

    @Override
    protected boolean applyRenderColor(GuiGraphicsExtractor guiGraphics) {
        MekanismRenderer.color(stored);
        return true;
    }

    @Override
    public Identifier getID() {
        return LookingAtUtils.CHEMICAL;
    }
}