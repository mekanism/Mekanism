package mekanism.additions.client;

import mekanism.additions.common.AdditionsLang;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.client.ClientRegistrationUtil;
import mekanism.client.key.MekKeyBindingBuilder;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class AdditionsKeyHandler {

    public static final KeyMapping voiceKey = new MekKeyBindingBuilder().description(AdditionsLang.KEY_VOICE).keyCode(GLFW.GLFW_KEY_U)
          .toggleable(() -> !MekanismAdditionsConfig.client.pushToTalk.getOrDefault()).build();

    public static void registerKeybindings(RegisterKeyMappingsEvent event) {
        ClientRegistrationUtil.registerKeyBindings(event, voiceKey);
    }
}