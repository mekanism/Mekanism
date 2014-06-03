package mekanism.client;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
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
		
		FMLCommonHandler.instance().bus().register(this);
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event)
	{
		if(event.side == Side.CLIENT)
		{
			if(event.phase == Phase.START)
			{
				keyTick(event.type, false);
			}
			else if(event.phase == Phase.END)
			{
				keyTick(event.type, true);
			}
		}
	}

	@Override
	public void keyDown(Type types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {}

	@Override
	public void keyUp(Type types, KeyBinding kb, boolean tickEnd) {}
}
