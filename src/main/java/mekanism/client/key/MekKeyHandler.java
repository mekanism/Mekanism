package mekanism.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.sdl.SDLMouse;

public class MekKeyHandler {

    private MekKeyHandler() {
    }

    //TODO: I think in theory we will eventually be able to replace this with: https://github.com/neoforged/NeoForge/pull/3331
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
            try {
                if (key.getType() == InputConstants.Type.KEYBOARD) {
                    return InputConstants.isKeyDown(keyCode);
                } else if (key.getType() == InputConstants.Type.MOUSE) {
                    int mouseState = SDLMouse.SDL_GetMouseState(null, null) & SDL_BUTTON_MASK(keyCode);
                    return mouseState != 0;
                }
            } catch (Exception _) {
            }
        }
        return false;
    }

    ///Copied from [SDLMouse#SDL_BUTTON_MASK(int)]
    private static int SDL_BUTTON_MASK(int x) {
        return 1 << (x - 1);
    }

    public static boolean isRadialPressed() {
        KeyMapping keyBinding = MekanismKeyHandler.handModeSwitchKey;
        if (keyBinding.isDown()) {
            return true;
        }
        IKeyConflictContext conflictContext = keyBinding.getKeyConflictContext();
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