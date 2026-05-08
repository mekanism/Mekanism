package mekanism.common.integration.lookingat;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FluidElement extends LookingAtElement {

    public static final MapCodec<FluidElement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
          FluidResource.OPTIONAL_CODEC.fieldOf(SerializationConstants.FLUID).forGetter(FluidElement::getFluidType),
          ExtraCodecs.NON_NEGATIVE_INT.fieldOf(SerializationConstants.AMOUNT).forGetter(FluidElement::getStored),
          ExtraCodecs.NON_NEGATIVE_INT.fieldOf(SerializationConstants.MAX).forGetter(FluidElement::getCapacity)
    ).apply(instance, FluidElement::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidElement> STREAM_CODEC = StreamCodec.composite(
          FluidResource.STREAM_CODEC, FluidElement::getFluidType,
          ByteBufCodecs.VAR_INT, FluidElement::getStored,
          ByteBufCodecs.VAR_INT, FluidElement::getCapacity,
          FluidElement::new
    );

    protected final FluidResource fluidType;
    protected final int stored;
    protected final int capacity;

    //TODO - 26.1: Replace the fluid stack with this
    public FluidElement(FluidResource fluidType, int stored, int capacity) {
        super(0xFF000000, 0xFFFFFF);
        this.fluidType = fluidType;
        this.stored = stored;
        this.capacity = capacity;
    }

    @Override
    public int getScaledLevel(int level) {
        if (capacity == 0 || stored == Integer.MAX_VALUE) {
            return level;
        }
        return MathUtils.clampToInt(level * MathUtils.divideToLevel(stored, capacity));
    }

    public FluidResource getFluidType() {
        return fluidType;
    }

    public int getStored() {
        return stored;
    }

    public int getCapacity() {
        return capacity;
    }

    @Nullable
    @Override
    public TextureAtlasSprite getIcon() {
        return fluidType.isEmpty() ? null : MekanismRenderer.getFluidTexture(fluidType, FluidTextureType.STILL);
    }

    @Override
    public Component getText() {
        if (fluidType.isEmpty() || stored == 0) {
            return MekanismLang.EMPTY.translate();
        } else if (stored == Integer.MAX_VALUE) {
            return MekanismLang.GENERIC_STORED.translate(fluidType, MekanismLang.INFINITE);
        }
        return MekanismLang.GENERIC_STORED_MB.translate(fluidType, TextUtils.format(stored));
    }

    @Override
    protected boolean applyRenderColor(GuiGraphicsExtractor guiGraphics) {
        MekanismRenderer.color(fluidType.toStack(stored));
        return true;
    }

    @Override
    public Identifier getID() {
        return LookingAtUtils.FLUID;
    }
}