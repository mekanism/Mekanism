package mekanism.common.integration.lookingat;

import mekanism.api.SerializationConstants;
import mekanism.common.Mekanism;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record TextElement(Component text) implements ILookingAtElement {

    private static final Identifier NAME = Mekanism.rl(SerializationConstants.TEXT);
    public static final StreamCodec<RegistryFriendlyByteBuf, TextElement> STREAM_CODEC = ComponentSerialization.TRUSTED_STREAM_CODEC.map(TextElement::new, TextElement::text);

    @Override
    public Identifier getID() {
        return NAME;
    }
}