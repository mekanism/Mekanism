package mekanism.additions.client;

import com.mojang.blaze3d.platform.InputConstants;
import mekanism.additions.common.AdditionsLang;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.client.ClientRegistrationUtil;
import mekanism.client.key.MekKeyBindingBuilder;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public class AdditionsKeyHandler {

    public static final KeyMapping voiceKey = new MekKeyBindingBuilder().description(AdditionsLang.KEY_VOICE).keyCode(InputConstants.KEY_U)
          .toggleable(() -> !MekanismAdditionsConfig.client.pushToTalk.getOrDefault()).build();

    public static void registerKeybindings(RegisterKeyMappingsEvent event) {
        ClientRegistrationUtil.registerKeyBindings(event, voiceKey);
    }
}