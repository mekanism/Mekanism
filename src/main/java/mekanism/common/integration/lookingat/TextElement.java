package mekanism.common.integration.lookingat;

import com.mojang.serialization.MapCodec;
import mekanism.api.SerializationConstants;
import mekanism.common.Mekanism;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record TextElement(Component text) implements ILookingAtElement {

    private static final ResourceLocation NAME = Mekanism.rl(SerializationConstants.TEXT);
    public static final MapCodec<TextElement> CODEC = ComponentSerialization.CODEC.fieldOf(SerializationConstants.TEXT).xmap(TextElement::new, TextElement::text);
    public static final StreamCodec<RegistryFriendlyByteBuf, TextElement> STREAM_CODEC = ComponentSerialization.TRUSTED_STREAM_CODEC.map(TextElement::new, TextElement::text);

    @Override
    public ResourceLocation getID() {
        return NAME;
    }
}