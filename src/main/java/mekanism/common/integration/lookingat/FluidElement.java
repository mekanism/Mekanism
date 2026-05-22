package mekanism.common.integration.lookingat;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.resource.LargeResourceStack;
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
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FluidElement extends LookingAtElement {

    public static final MapCodec<FluidElement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
          LargeResourceStack.FLUID_HELPER.optionalCodec().fieldOf(SerializationConstants.FLUID).forGetter(FluidElement::getStored),
          ExtraCodecs.NON_NEGATIVE_LONG.fieldOf(SerializationConstants.MAX).forGetter(FluidElement::getCapacity)
    ).apply(instance, FluidElement::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidElement> STREAM_CODEC = StreamCodec.composite(
          LargeResourceStack.FLUID_HELPER.streamCodec(), FluidElement::getStored,
          ByteBufCodecs.VAR_LONG, FluidElement::getCapacity,
          FluidElement::new
    );

    protected final LargeResourceStack<FluidResource> stored;
    protected final long capacity;

    public FluidElement(FluidResource fluidType, long stored, long capacity) {
        this(LargeResourceStack.FLUID_HELPER.createStack(fluidType, stored), capacity);
    }

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
    protected int getRenderColor() {
        return MekanismRenderer.color(stored.resource());
    }

    @Override
    public Identifier getID() {
        return LookingAtUtils.FLUID;
    }
}