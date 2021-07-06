package mekanism.additions.client;

import mekanism.additions.common.AdditionsLang;
import mekanism.client.ClientRegistrationUtil;
import mekanism.client.key.MekKeyBinding.MekBindingBuilder;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class AdditionsKeyHandler {

    //TODO - 10.1: Re-evaluate why this is repeating when it doesn't have an onDown or onUp. Also figure out if this should use the ToggleableKeyBinding concept
    public static final KeyBinding voiceKey = new MekBindingBuilder().description(AdditionsLang.KEY_VOICE).keyCode(GLFW.GLFW_KEY_U).repeating().build();

    public static void registerKeybindings() {
        ClientRegistrationUtil.registerKeyBindings(voiceKey);
    }
}