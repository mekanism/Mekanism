package mekanism.client;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public abstract class MekKeyHandler {

    /**
     * KeyBinding instances
     */
    private final KeyBinding[] keyBindings;

    /**
     * Track which keys have been seen as pressed currently
     */
    private final BitSet keyDown;

    /**
     * Whether keys send repeated KeyDown pseudo-messages
     */
    private final BitSet repeatings;

    /**
     * Pass an array of keybindings and a repeat flag for each one
     *
     * @param bindings Bindings to set
     */
    public MekKeyHandler(Builder bindings) {
        keyBindings = bindings.getBindings();
        repeatings = bindings.getRepeatFlags();
        keyDown = new BitSet();
    }

    public static boolean getIsKeyPressed(KeyBinding keyBinding) {
        if (keyBinding.isKeyDown()) {
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
        int keyCode = key.getKeyCode();
        if (keyCode != InputMappings.INPUT_INVALID.getKeyCode()) {
            long windowHandle = Minecraft.getInstance().getMainWindow().getHandle();
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

    public void keyTick() {
        for (int i = 0; i < keyBindings.length; i++) {
            KeyBinding keyBinding = keyBindings[i];
            boolean state = keyBinding.isKeyDown();
            boolean lastState = keyDown.get(i);
            if (state != lastState || (state && repeatings.get(i))) {
                if (state) {
                    keyDown(keyBinding, lastState);
                } else {
                    keyUp(keyBinding);
                }
                keyDown.set(i, state);
            }
        }
    }

    public abstract void keyDown(KeyBinding kb, boolean isRepeat);

    public abstract void keyUp(KeyBinding kb);

    public static class Builder {

        private final List<KeyBinding> bindings;
        private final BitSet repeatFlags = new BitSet();

        public Builder(int expectedCapacity) {
            this.bindings = new ArrayList<>(expectedCapacity);
        }

        /**
         * Add a keybinding to the list
         *
         * @param k          the KeyBinding to add
         * @param repeatFlag true if keyDown pseudo-events continue to be sent while key is held
         */
        public Builder addBinding(KeyBinding k, boolean repeatFlag) {
            repeatFlags.set(bindings.size(), repeatFlag);
            bindings.add(k);
            return this;
        }

        protected BitSet getRepeatFlags() {
            return repeatFlags;
        }

        protected KeyBinding[] getBindings() {
            return bindings.toArray(new KeyBinding[0]);
        }
    }
}