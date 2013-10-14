package mekanism.client;

import java.util.EnumSet;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;

public class MekanismKeyHandler extends KeyHandler
{
	public static KeyBinding modeSwitch = new KeyBinding("Mekanism Mode Switch", Keyboard.KEY_M);
	public static KeyBinding voice = new KeyBinding("Mekanism Voice", Keyboard.KEY_U);
	
	public static boolean modeSwitchDown = false;
	public static boolean voiceDown = false;
	
	public MekanismKeyHandler()
	{
		super(new KeyBinding[] {modeSwitch, voice}, new boolean[] {false, false});
	}

	@Override
	public String getLabel() 
	{
		return "MekanismKey";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat)
	{
		if(kb.keyCode == modeSwitch.keyCode)
		{
			modeSwitchDown = true;
		}
		else if(kb.keyCode == voice.keyCode)
		{
			voiceDown = true;
		}
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) 
	{
		if(kb.keyCode == modeSwitch.keyCode)
		{
			modeSwitchDown = false;
		}
		else if(kb.keyCode == voice.keyCode)
		{
			voiceDown = false;
		}
	}

	@Override
	public EnumSet<TickType> ticks() 
	{
		return EnumSet.of(TickType.CLIENT);
	}
}
