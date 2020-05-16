package mekanism.additions.client;

import mekanism.additions.common.AdditionsLang;
import mekanism.client.MekKeyHandler;
import mekanism.common.MekanismLang;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class AdditionsKeyHandler extends MekKeyHandler {

    public static final KeyBinding voiceKey = new KeyBinding(AdditionsLang.KEY_VOICE.getTranslationKey(), GLFW.GLFW_KEY_U, MekanismLang.MEKANISM.getTranslationKey());

    private static final Builder BINDINGS = new Builder(1)
          .addBinding(voiceKey, true);

    public AdditionsKeyHandler() {
        super(BINDINGS);
        ClientRegistry.registerKeyBinding(voiceKey);
        MinecraftForge.EVENT_BUS.addListener(this::onTick);
    }

    private void onTick(InputEvent.KeyInputEvent event) {
        keyTick();
    }

    @Override
    public void keyDown(KeyBinding kb, boolean isRepeat) {
    }

    @Override
    public void keyUp(KeyBinding kb) {
    }
}