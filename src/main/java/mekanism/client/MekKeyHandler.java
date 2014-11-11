package mekanism.client;

import java.util.EnumSet;

import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.common.gameevent.TickEvent.Type;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public abstract class MekKeyHandler
{
	public KeyBinding[] keyBindings;
	public boolean[] keyDown;
	public boolean[] repeatings;
	public boolean isDummy;

	/**
	 * Pass an array of keybindings and a repeat flag for each one
	 *
	 * @param keyBindings
	 * @param repeatings
	 */
	public MekKeyHandler(KeyBinding[] bindings, boolean[] rep)
	{
		assert keyBindings.length == repeatings.length : "You need to pass two arrays of identical length";
		keyBindings = bindings;
		repeatings = rep;
		keyDown = new boolean[keyBindings.length];
	}

	/**
	 * Register the keys into the system. You will do your own keyboard
	 * management elsewhere. No events will fire if you use this method
	 *
	 * @param keyBindings
	 */
	public MekKeyHandler(KeyBinding[] bindings)
	{
		keyBindings = bindings;
		isDummy = true;
	}

	public static boolean getIsKeyPressed(KeyBinding keyBinding)
	{
		int keyCode = keyBinding.getKeyCode();
		return keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode);
	}

	public KeyBinding[] getKeyBindings ()
	{
		return keyBindings;
	}

	public void keyTick(Type type, boolean tickEnd)
	{
		for(int i = 0; i < keyBindings.length; i++)
		{
			KeyBinding keyBinding = keyBindings[i];
			int keyCode = keyBinding.getKeyCode();
			boolean state = (keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode));
			
			if(state != keyDown[i] || (state && repeatings[i]))
			{
				if(state)
				{
					keyDown(type, keyBinding, tickEnd, state != keyDown[i]);
				}
				else {
					keyUp(type, keyBinding, tickEnd);
				}
				
				if(tickEnd)
				{
					keyDown[i] = state;
				}
			}
		}
	}

	/**
	 * Called when the key is first in the down position on any tick from the
	 * {@link #ticks()} set. Will be called subsequently with isRepeat set to
	 * true
	 *
	 * @see #keyUp(EnumSet, KeyBinding, boolean)
	 *
	 * @param types
	 * the type(s) of tick that fired when this key was first down
	 * @param tickEnd
	 * was it an end or start tick which fired the key
	 * @param isRepeat
	 * is it a repeat key event
	 */
	public abstract void keyDown(Type types, KeyBinding kb, boolean tickEnd, boolean isRepeat);

	/**
	 * Fired once when the key changes state from down to up
	 *
	 * @see #keyDown(EnumSet, KeyBinding, boolean, boolean)
	 *
	 * @param types
	 * the type(s) of tick that fired when this key was first down
	 * @param tickEnd
	 * was it an end or start tick which fired the key
	 */
	public abstract void keyUp(Type types, KeyBinding kb, boolean tickEnd);
}