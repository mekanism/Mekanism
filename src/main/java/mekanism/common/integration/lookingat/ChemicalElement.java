package mekanism.common.integration.lookingat;

import mekanism.api.chemical.ChemicalResource;
import mekanism.api.resource.LargeResourceStack;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public non-sealed class ChemicalElement extends ResourceElement<ChemicalResource> {

    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalElement> STREAM_CODEC = StreamCodec.composite(
          LargeResourceStack.CHEMICAL_HELPER.streamCodec(), ChemicalElement::getStored,
          ByteBufCodecs.VAR_LONG, ChemicalElement::getCapacity,
          ChemicalElement::new
    );

    public ChemicalElement(@NotNull ChemicalResource chemicalType, long stored, long capacity) {
        this(LargeResourceStack.CHEMICAL_HELPER.createStack(chemicalType, stored), capacity);
    }

    public ChemicalElement(@NotNull LargeResourceStack<ChemicalResource> stored, long capacity) {
        super(stored, capacity);
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return stored.isEmpty() ? null : MekanismRenderer.getChemicalTexture(stored.resource());
    }

    @Override
    protected int getRenderColor() {
        return MekanismRenderer.color(stored.resource());
    }

    @Override
    public Identifier getID() {
        return LookingAtUtils.CHEMICAL;
    }
}