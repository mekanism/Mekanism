package mekanism.client;

import java.util.EnumSet;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MekanismKeyHandler extends KeyHandler
{
	public static KeyBinding modeSwitchKey = new KeyBinding("Mekanism Mode Switch", Keyboard.KEY_M);
	public static KeyBinding voiceKey = new KeyBinding("Mekanism Voice", Keyboard.KEY_U);

	public MekanismKeyHandler()
	{
		super(new KeyBinding[] {modeSwitchKey, voiceKey}, new boolean[] {false, false});
	}

	@Override
	public String getLabel()
	{
		return "MekanismKey";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {}

	@Override
	public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.CLIENT);
	}
}
