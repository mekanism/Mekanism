package mekanism.common.integration.lookingat;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.SerializationConstants;
import mekanism.api.math.MathUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class FluidElement extends LookingAtElement {

    public static final MapCodec<FluidElement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
          FluidStack.OPTIONAL_CODEC.fieldOf(SerializationConstants.FLUID).forGetter(FluidElement::getStored),
          ExtraCodecs.NON_NEGATIVE_INT.fieldOf(SerializationConstants.MAX).forGetter(FluidElement::getCapacity)
    ).apply(instance, FluidElement::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidElement> STREAM_CODEC = StreamCodec.composite(
          FluidStack.OPTIONAL_STREAM_CODEC, FluidElement::getStored,
          ByteBufCodecs.VAR_INT, FluidElement::getCapacity,
          FluidElement::new
    );

    @NotNull
    protected final FluidStack stored;
    protected final int capacity;

    public FluidElement(@NotNull FluidStack stored, int capacity) {
        super(0xFF000000, 0xFFFFFF);
        this.stored = stored;
        this.capacity = capacity;
    }

    @Override
    public int getScaledLevel(int level) {
        if (capacity == 0 || stored.getAmount() == Integer.MAX_VALUE) {
            return level;
        }
        return MathUtils.clampToInt(level * MathUtils.divideToLevel(stored.getAmount(), capacity));
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
        int amount = stored.getAmount();
        if (stored.isEmpty()) {
            return MekanismLang.EMPTY.translate();
        } else if (amount == Integer.MAX_VALUE) {
            return MekanismLang.GENERIC_STORED.translate(stored, MekanismLang.INFINITE);
        }
        return MekanismLang.GENERIC_STORED_MB.translate(stored, TextUtils.format(amount));
    }

    @Override
    protected boolean applyRenderColor(GuiGraphics guiGraphics) {
        MekanismRenderer.color(guiGraphics, stored);
        return true;
    }

    @Override
    public ResourceLocation getID() {
        return LookingAtUtils.FLUID;
    }
}