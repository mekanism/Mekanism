package mekanism.client;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.gameevent.TickEvent.Type;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MekanismKeyHandler extends MekKeyHandler
{
	public static final String keybindCategory = "key.mekanism.category";
	public static KeyBinding modeSwitchKey = new KeyBinding("Mekanism Mode Switch", Keyboard.KEY_M, keybindCategory);
	public static KeyBinding voiceKey = new KeyBinding("Mekanism Voice", Keyboard.KEY_U, keybindCategory);

	public MekanismKeyHandler()
	{
		super(new KeyBinding[] {modeSwitchKey, voiceKey}, new boolean[] {false, false});
	}

	@Override
	public void keyDown(Type types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {}

	@Override
	public void keyUp(Type types, KeyBinding kb, boolean tickEnd) {}
}
