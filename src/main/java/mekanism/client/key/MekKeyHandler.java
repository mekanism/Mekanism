package mekanism.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public class MekKeyHandler {

    private MekKeyHandler() {
    }

    //TODO - 26.2: I think in theory we will eventually be able to replace this with: https://github.com/neoforged/NeoForge/pull/3331
    public static boolean isKeyPressed(KeyMapping keyBinding) {
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

    private static boolean isKeyDown(KeyMapping keyBinding) {
        InputConstants.Key key = keyBinding.getKey();
        int keyCode = key.getValue();
        if (keyCode != InputConstants.UNKNOWN.getValue()) {
            Window window = Minecraft.getInstance().getWindow();
            try {
                if (key.getType() == InputConstants.Type.KEYSYM) {
                    return InputConstants.isKeyDown(window, keyCode);
                } else if (key.getType() == InputConstants.Type.MOUSE) {
                    //TODO - 26.2: Figure out how to replace this so that it doesn't need to directly access GLFW
                    return GLFW.glfwGetMouseButton(window.handle(), keyCode) == InputConstants.PRESS;
                }
            } catch (Exception _) {
            }
        }
        return false;
    }

    public static boolean isRadialPressed() {
        KeyMapping keyBinding = MekanismKeyHandler.handModeSwitchKey;
        if (keyBinding.isDown()) {
            return true;
        }
        IKeyConflictContext conflictContext = keyBinding.getKeyConflictContext();
        if (!conflictContext.isActive()) {
            //If the conflict context (game) isn't active try it as being a gui but without it normally actually conflicting with gui keybindings
            conflictContext = KeyConflictContext.GUI;
        }
        //If we have no modifier set on the radial key allow it to be "active" even if another modifier is pressed, as we only
        // check the radial menu at specific times, so we don't want to close it if the player hits shift or something
        if (conflictContext.isActive() && (keyBinding.getKeyModifier() == KeyModifier.NONE || keyBinding.getKeyModifier().isActive(conflictContext))) {
            //Manually check in case keyBinding#pressed just never got a chance to be updated
            return isKeyDown(keyBinding);
        }
        //If we failed, due to us being a key modifier as our key, check the old way
        return KeyModifier.isKeyCodeModifier(keyBinding.getKey()) && isKeyDown(keyBinding);
    }
}