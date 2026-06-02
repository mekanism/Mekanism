package mekanism.common.integration.lookingat;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.resource.LargeResourceStack;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public non-sealed class FluidElement extends ResourceElement<FluidResource> {

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidElement> STREAM_CODEC = StreamCodec.composite(
          LargeResourceStack.FLUID_HELPER.streamCodec(), FluidElement::getStored,
          ByteBufCodecs.VAR_LONG, FluidElement::getCapacity,
          FluidElement::new
    );

    public FluidElement(FluidResource fluidType, long stored, long capacity) {
        this(LargeResourceStack.FLUID_HELPER.createStack(fluidType, stored), capacity);
    }

    public FluidElement(LargeResourceStack<FluidResource> stored, long capacity) {
        super(stored, capacity);
    }

    @Nullable
    @Override
    public TextureAtlasSprite getIcon() {
        return stored.isEmpty() ? null : MekanismRenderer.getFluidTexture(stored.resource(), FluidTextureType.STILL);
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