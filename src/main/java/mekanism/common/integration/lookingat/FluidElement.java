package mekanism.common.integration.lookingat;

import mekanism.api.math.MathUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public non-sealed class FluidElement extends LookingAtElement {

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidElement> STREAM_CODEC = StreamCodec.composite(
          FluidStack.OPTIONAL_STREAM_CODEC, FluidElement::getStored,
          ByteBufCodecs.VAR_INT, FluidElement::getCapacity,
          FluidElement::new
    );

    @NotNull
    protected final FluidStack stored;
    protected final int capacity;

    public FluidElement(@NotNull FluidStack stored, int capacity) {
        super(0xFF000000, 0xFFFFFFFF);
        this.stored = stored;
        this.capacity = capacity;
    }

    @Override
    public int getScaledLevel(int level) {
        if (capacity == 0 || stored.amount() == Integer.MAX_VALUE) {
            return level;
        }
        return MathUtils.clampToInt(level * MathUtils.divideToLevel(stored.amount(), capacity));
    }

    @NotNull
    public FluidStack getStored() {
        return stored;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return stored.isEmpty() ? null : MekanismRenderer.getFluidTexture(stored, FluidTextureType.STILL);
    }

    @Override
    public Component getText() {
        int amount = stored.amount();
        if (stored.isEmpty()) {
            return MekanismLang.EMPTY.translate();
        } else if (amount == Integer.MAX_VALUE) {
            return MekanismLang.GENERIC_STORED.translate(stored, MekanismLang.INFINITE);
        }
        return MekanismLang.GENERIC_STORED_MB.translate(stored, TextUtils.format(amount));
    }

    @Override
    protected int getRenderColor() {
        return MekanismRenderer.color(stored);
    }

    @Override
    public Identifier getID() {
        return LookingAtUtils.FLUID;
    }
}