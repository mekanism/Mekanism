package mekanism.client.key;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public class MekKeyHandler {

    private MekKeyHandler() {
    }

    public static boolean getIsKeyPressed(KeyBinding keyBinding) {
        if (keyBinding.isDown()) {
            return true;
        }
        if (keyBinding.getKeyConflictContext().isActive() && keyBinding.getKeyModifier().isActive(keyBinding.getKeyConflictContext())) {
            //Manually check in case keyBinding#pressed just never got a chance to be updated
            return isKeyDown(keyBinding);
        }
        //If we failed, due to us being a key modifier as our key, check the old way
        return KeyModifier.isKeyCodeModifier(keyBinding.getKey()) && isKeyDown(keyBinding);
    }

    public static boolean isKeyDown(KeyBinding keyBinding) {
        InputMappings.Input key = keyBinding.getKey();
        int keyCode = key.getValue();
        if (keyCode != InputMappings.UNKNOWN.getValue()) {
            long windowHandle = Minecraft.getInstance().getWindow().getWindow();
            try {
                if (key.getType() == InputMappings.Type.KEYSYM) {
                    return InputMappings.isKeyDown(windowHandle, keyCode);
                } else if (key.getType() == InputMappings.Type.MOUSE) {
                    return GLFW.glfwGetMouseButton(windowHandle, keyCode) == GLFW.GLFW_PRESS;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}