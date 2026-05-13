package mekanism.common.integration.lookingat;

import com.google.common.primitives.Ints;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.LargeResourceStack;
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
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FluidElement extends LookingAtElement {

    public static final MapCodec<FluidElement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
          SerializerHelper.OPTIONAL_FLUID_RESOURCE_STACK_CODEC.fieldOf(SerializationConstants.FLUID).forGetter(FluidElement::getStored),
          SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(SerializationConstants.MAX).forGetter(FluidElement::getCapacity)
    ).apply(instance, FluidElement::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidElement> STREAM_CODEC = StreamCodec.composite(
          SerializerHelper.FLUID_RESOURCE_STACK_STREAM_CODEC, FluidElement::getStored,
          ByteBufCodecs.VAR_LONG, FluidElement::getCapacity,
          FluidElement::new
    );

    protected final LargeResourceStack<FluidResource> stored;
    protected final long capacity;

    //TODO - 26.1: Replace the fluid stack with this
    public FluidElement(LargeResourceStack<FluidResource> stored, long capacity) {
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

    public LargeResourceStack<FluidResource> getStored() {
        return stored;
    }

    public long getCapacity() {
        return capacity;
    }

    @Nullable
    @Override
    public TextureAtlasSprite getIcon() {
        return stored.isEmpty() ? null : MekanismRenderer.getFluidTexture(stored.resource(), FluidTextureType.STILL);
    }

    @Override
    public Component getText() {
        if (stored.isEmpty()) {
            return MekanismLang.EMPTY.translate();
        } else if (stored.amount() == Long.MAX_VALUE) {
            return MekanismLang.GENERIC_STORED.translate(stored.resource(), MekanismLang.INFINITE);
        }
        return MekanismLang.GENERIC_STORED_MB.translate(stored.resource(), TextUtils.format(stored.amount()));
    }

    @Override
    protected boolean applyRenderColor(GuiGraphicsExtractor guiGraphics) {
        MekanismRenderer.color(stored.resource().toStack(Ints.saturatedCast(stored.amount())));
        return true;
    }

    @Override
    public Identifier getID() {
        return LookingAtUtils.FLUID;
    }
}