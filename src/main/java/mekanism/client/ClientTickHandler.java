package mekanism.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mekanism.api.EnumColor;
import mekanism.api.IClientTicker;
import mekanism.api.gas.GasStack;
import mekanism.client.sound.FlamethrowerSound;
import mekanism.client.sound.GasMaskSound;
import mekanism.client.sound.JetpackSound;
import mekanism.client.sound.SoundHandler;
import mekanism.common.KeySync;
import mekanism.common.Mekanism;
import mekanism.common.ObfuscatedNames;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemElectricBow;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.item.ItemFreeRunners;
import mekanism.common.item.ItemGasMask;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemJetpack.JetpackMode;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.network.PacketConfiguratorState.ConfiguratorStateMessage;
import mekanism.common.network.PacketElectricBowState.ElectricBowStateMessage;
import mekanism.common.network.PacketFlamethrowerActive.FlamethrowerActiveMessage;
import mekanism.common.network.PacketJetpackData.JetpackDataMessage;
import mekanism.common.network.PacketJetpackData.JetpackPacket;
import mekanism.common.network.PacketPortableTankState.PortableTankStateMessage;
import mekanism.common.network.PacketScubaTankData.ScubaTankDataMessage;
import mekanism.common.network.PacketScubaTankData.ScubaTankPacket;
import mekanism.common.network.PacketWalkieTalkieState.WalkieTalkieStateMessage;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StringUtils;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Client-side tick handler for Mekanism. Used mainly for the update check upon startup.
 * @author AidanBrady
 *
 */
@SideOnly(Side.CLIENT)
public class ClientTickHandler
{
	public boolean hasNotified = false;
	public boolean initHoliday = false;

	public boolean preloadedSounds = false;

	public boolean lastTickUpdate;

	public boolean shouldReset = false;

	public static Minecraft mc = FMLClientHandler.instance().getClient();

	public static final String MIKE_CAPE = "https://dl.dropboxusercontent.com/s/ji06yflixnszcby/cape.png";
	public static final String DONATE_CAPE = "https://dl.dropboxusercontent.com/u/90411166/donate.png";
	public static final String AIDAN_CAPE = "https://dl.dropboxusercontent.com/u/90411166/aidan.png";

	private Map<String, CapeBufferDownload> mikeDownload = new HashMap<String, CapeBufferDownload>();
	private Map<String, CapeBufferDownload> donateDownload = new HashMap<String, CapeBufferDownload>();
	private Map<String, CapeBufferDownload> aidanDownload = new HashMap<String, CapeBufferDownload>();

	public static Set<IClientTicker> tickingSet = new HashSet<IClientTicker>();

	@SubscribeEvent
	public void onTick(ClientTickEvent event)
	{
		if(event.phase == Phase.START)
		{
			tickStart();
		}
		else if(event.phase == Phase.END)
		{
			tickEnd();
		}
	}

	public void tickStart()
	{
		if(!preloadedSounds && MekanismClient.enableSounds && MekanismClient.audioHandler != null && MekanismClient.audioHandler.isSystemLoaded())
		{
			preloadedSounds = true;
			
			new Thread(new Runnable() {
				@Override
				public void run()
				{
					MekanismClient.audioHandler.preloadSounds();
				}
			}).start();
		}

		MekanismClient.ticksPassed++;

		if(!hasNotified && mc.theWorld != null && Mekanism.latestVersionNumber != null && Mekanism.recentNews != null)
		{
			MekanismUtils.checkForUpdates(mc.thePlayer);
			hasNotified = true;
		}

		if(!Mekanism.proxy.isPaused())
		{
			for(Iterator<IClientTicker> iter = tickingSet.iterator(); iter.hasNext();)
			{
				IClientTicker ticker = iter.next();

				if(ticker.needsTicks())
				{
					ticker.clientTick();
				}
				else {
					iter.remove();
				}
			}
		}

		if(mc.theWorld != null)
		{
			shouldReset = true;
		}
		else if(shouldReset)
		{
			MekanismClient.reset();
			shouldReset = false;
		}

		if(mc.theWorld != null && !Mekanism.proxy.isPaused())
		{
			if((!initHoliday || MekanismClient.ticksPassed % 1200 == 0) && mc.thePlayer != null)
			{
				HolidayManager.check();
				initHoliday = true;
			}

			for(EntityPlayer entityPlayer : (List<EntityPlayer>)mc.theWorld.playerEntities)
			{
				if(entityPlayer instanceof AbstractClientPlayer)
				{
					AbstractClientPlayer player = (AbstractClientPlayer)entityPlayer;

					if(player != null)
					{
						if(StringUtils.stripControlCodes(player.getCommandSenderName()).equals("mikeacttck"))
						{
							CapeBufferDownload download = mikeDownload.get(player.getCommandSenderName());

							if(download == null)
							{
								download = new CapeBufferDownload(player.getCommandSenderName(), MIKE_CAPE);
								mikeDownload.put(player.getCommandSenderName(), download);

								download.start();
							}
							else {
								if(!download.downloaded)
								{
									continue;
								}

								MekanismUtils.setPrivateValue(player, download.getImage(), AbstractClientPlayer.class, ObfuscatedNames.AbstractClientPlayer_downloadImageCape);
								MekanismUtils.setPrivateValue(player, download.getResourceLocation(), AbstractClientPlayer.class, ObfuscatedNames.AbstractClientPlayer_locationCape);
							}
						}
						else if(StringUtils.stripControlCodes(player.getCommandSenderName()).equals("aidancbrady"))
						{
							CapeBufferDownload download = aidanDownload.get(player.getCommandSenderName());

							if(download == null)
							{
								download = new CapeBufferDownload(player.getCommandSenderName(), AIDAN_CAPE);
								aidanDownload.put(player.getCommandSenderName(), download);

								download.start();
							}
							else {
								if(!download.downloaded)
								{
									continue;
								}

								MekanismUtils.setPrivateValue(player, download.getImage(), AbstractClientPlayer.class, ObfuscatedNames.AbstractClientPlayer_downloadImageCape);
								MekanismUtils.setPrivateValue(player, download.getResourceLocation(), AbstractClientPlayer.class, ObfuscatedNames.AbstractClientPlayer_locationCape);
							}
						}
						else if(Mekanism.donators.contains(StringUtils.stripControlCodes(player.getCommandSenderName())))
						{
							CapeBufferDownload download = donateDownload.get(player.getCommandSenderName());

							if(download == null)
							{
								download = new CapeBufferDownload(player.getCommandSenderName(), DONATE_CAPE);
								donateDownload.put(player.getCommandSenderName(), download);

								download.start();
							}
							else {
								if(!download.downloaded)
								{
									continue;
								}

								MekanismUtils.setPrivateValue(player, download.getImage(), AbstractClientPlayer.class, ObfuscatedNames.AbstractClientPlayer_downloadImageCape);
								MekanismUtils.setPrivateValue(player, download.getResourceLocation(), AbstractClientPlayer.class, ObfuscatedNames.AbstractClientPlayer_locationCape);
							}
						}
					}
				}
			}

			ItemStack stack = mc.thePlayer.getCurrentEquippedItem();

			if(mc.currentScreen == null)
			{
				if(mc.thePlayer.isSneaking() && StackUtils.getItem(mc.thePlayer.getCurrentEquippedItem()) instanceof ItemConfigurator)
				{
					ItemConfigurator item = (ItemConfigurator)mc.thePlayer.getCurrentEquippedItem().getItem();

					if(MekanismKeyHandler.modeSwitchKey.getIsKeyPressed())
					{
						if(!lastTickUpdate)
						{
							item.setState(stack, (byte)(item.getState(stack) < 3 ? item.getState(stack)+1 : 0));
							Mekanism.packetHandler.sendToServer(new ConfiguratorStateMessage(item.getState(stack)));
							mc.thePlayer.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + MekanismUtils.localize("tooltip.configureState") + ": " + item.getColor(item.getState(stack)) + item.getStateDisplay(item.getState(stack))));
							lastTickUpdate = true;
						}
					}
					else {
						lastTickUpdate = false;
					}
				}
				else if(mc.thePlayer.isSneaking() && StackUtils.getItem(mc.thePlayer.getCurrentEquippedItem()) instanceof ItemElectricBow)
				{
					ItemElectricBow item = (ItemElectricBow)mc.thePlayer.getCurrentEquippedItem().getItem();

					if(MekanismKeyHandler.modeSwitchKey.getIsKeyPressed())
					{
						if(!lastTickUpdate)
						{
							item.setFireState(stack, !item.getFireState(stack));
							Mekanism.packetHandler.sendToServer(new ElectricBowStateMessage(item.getFireState(stack)));
							mc.thePlayer.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + MekanismUtils.localize("tooltip.fireMode") + ": " + (item.getFireState(stack) ? EnumColor.DARK_GREEN : EnumColor.DARK_RED) + LangUtils.transYesNo(item.getFireState(stack))));
							lastTickUpdate = true;
						}
					}
					else {
						lastTickUpdate = false;
					}
				}
				else if(mc.thePlayer.isSneaking() && StackUtils.getItem(mc.thePlayer.getCurrentEquippedItem()) instanceof ItemBlockMachine)
				{
					ItemBlockMachine item = (ItemBlockMachine)mc.thePlayer.getCurrentEquippedItem().getItem();

					if(MachineType.get(mc.thePlayer.getCurrentEquippedItem()) == MachineType.PORTABLE_TANK)
					{
						if(MekanismKeyHandler.modeSwitchKey.getIsKeyPressed())
						{
							if(!lastTickUpdate)
							{
								item.setBucketMode(stack, !item.getBucketMode(stack));
								Mekanism.packetHandler.sendToServer(new PortableTankStateMessage(item.getBucketMode(stack)));
								mc.thePlayer.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + MekanismUtils.localize("tooltip.portableTank.bucketMode") + ": " + (item.getBucketMode(stack) ? (EnumColor.DARK_GREEN + "ON") : (EnumColor.DARK_RED + "OFF"))));
								lastTickUpdate = true;
							}
						}
						else {
							lastTickUpdate = false;
						}
					}
				}
				else if(mc.thePlayer.isSneaking() && StackUtils.getItem(mc.thePlayer.getCurrentEquippedItem()) instanceof ItemWalkieTalkie)
				{
					ItemWalkieTalkie item = (ItemWalkieTalkie)mc.thePlayer.getCurrentEquippedItem().getItem();

					if(MekanismKeyHandler.modeSwitchKey.getIsKeyPressed() && item.getOn(stack))
					{
						if(!lastTickUpdate)
						{
							int newChan = item.getChannel(stack) < 9 ? item.getChannel(stack)+1 : 1;
							item.setChannel(stack, newChan);
							Mekanism.packetHandler.sendToServer(new WalkieTalkieStateMessage(newChan));
							SoundHandler.playSound("mekanism:etc.Ding");
							lastTickUpdate = true;
						}
					}
					else {
						lastTickUpdate = false;
					}
				}
				else if(mc.thePlayer.getEquipmentInSlot(3) != null && mc.thePlayer.getEquipmentInSlot(3).getItem() instanceof ItemJetpack)
				{
					ItemStack jetpack = mc.thePlayer.getEquipmentInSlot(3);

					if(MekanismKeyHandler.modeSwitchKey.getIsKeyPressed())
					{
						if(!lastTickUpdate)
						{
							((ItemJetpack)jetpack.getItem()).incrementMode(jetpack);
							Mekanism.packetHandler.sendToServer(new JetpackDataMessage(JetpackPacket.MODE, null, false));
							SoundHandler.playSound("mekanism:etc.Hydraulic");
							lastTickUpdate = true;
						}
					}
					else {
						lastTickUpdate = false;
					}
				}
				else if(mc.thePlayer.getEquipmentInSlot(3) != null && mc.thePlayer.getEquipmentInSlot(3).getItem() instanceof ItemScubaTank)
				{
					ItemStack scubaTank = mc.thePlayer.getEquipmentInSlot(3);

					if(MekanismKeyHandler.modeSwitchKey.getIsKeyPressed())
					{
						if(!lastTickUpdate)
						{
							((ItemScubaTank)scubaTank.getItem()).toggleFlowing(scubaTank);
							Mekanism.packetHandler.sendToServer(new ScubaTankDataMessage(ScubaTankPacket.MODE, null, false));
							SoundHandler.playSound("mekanism:etc.Hydraulic");
							lastTickUpdate = true;
						}
					}
					else {
						lastTickUpdate = false;
					}
				}
				else {
					lastTickUpdate = false;
				}
			}

			if(mc.thePlayer.getEquipmentInSlot(1) != null && mc.thePlayer.getEquipmentInSlot(1).getItem() instanceof ItemFreeRunners)
			{
				mc.thePlayer.stepHeight = 1.002F;
			}
			else {
				if(mc.thePlayer.stepHeight == 1.002F)
				{
					mc.thePlayer.stepHeight = 0.5F;
				}
			}
			
			if(isFlamethrowerOn(mc.thePlayer) != Mekanism.flamethrowerActive.contains(mc.thePlayer.getCommandSenderName()))
			{
				if(isFlamethrowerOn(mc.thePlayer))
				{
					Mekanism.flamethrowerActive.add(mc.thePlayer.getCommandSenderName());
				}
				else {
					Mekanism.flamethrowerActive.remove(mc.thePlayer);
				}
				
				Mekanism.packetHandler.sendToServer(new FlamethrowerActiveMessage(isFlamethrowerOn(mc.thePlayer)));
			}

			if(Mekanism.jetpackOn.contains(mc.thePlayer.getCommandSenderName()) != isJetpackOn(mc.thePlayer))
			{
				if(isJetpackOn(mc.thePlayer))
				{
					Mekanism.jetpackOn.add(mc.thePlayer.getCommandSenderName());
				}
				else {
					Mekanism.jetpackOn.remove(mc.thePlayer.getCommandSenderName());
				}

				Mekanism.packetHandler.sendToServer(new JetpackDataMessage(JetpackPacket.UPDATE, mc.thePlayer.getCommandSenderName(), isJetpackOn(mc.thePlayer)));
			}

			if(Mekanism.gasmaskOn.contains(mc.thePlayer.getCommandSenderName()) != isGasMaskOn(mc.thePlayer))
			{
				if(isGasMaskOn(mc.thePlayer) && mc.currentScreen == null)
				{
					Mekanism.gasmaskOn.add(mc.thePlayer.getCommandSenderName());
				}
				else {
					Mekanism.gasmaskOn.remove(mc.thePlayer.getCommandSenderName());
				}

				Mekanism.packetHandler.sendToServer(new ScubaTankDataMessage(ScubaTankPacket.UPDATE, mc.thePlayer.getCommandSenderName(), isGasMaskOn(mc.thePlayer)));
			}

			if(MekanismClient.audioHandler != null)
			{
				for(String username : Mekanism.jetpackOn)
				{
					EntityPlayer player = mc.theWorld.getPlayerEntityByName(username);
					
					if(player != null)
					{
						if(MekanismClient.audioHandler.getSound(player, SoundHandler.CHANNEL_JETPACK) == null)
						{
							new JetpackSound(MekanismClient.audioHandler.getIdentifier(player), player);
						}
					}
				}

				for(String username : Mekanism.gasmaskOn)
				{
					EntityPlayer player = mc.theWorld.getPlayerEntityByName(username);
					
					if(player != null)
					{
						if(MekanismClient.audioHandler.getSound(player, SoundHandler.CHANNEL_GASMASK) == null)
						{
							new GasMaskSound(MekanismClient.audioHandler.getIdentifier(player), player);
						}
					}
				}
				
				for(EntityPlayer player : (List<EntityPlayer>)mc.theWorld.playerEntities)
				{
					if(hasFlamethrower(player))
					{
						if(MekanismClient.audioHandler.getSound(player, SoundHandler.CHANNEL_FLAMETHROWER) == null)
						{
							new FlamethrowerSound(MekanismClient.audioHandler.getIdentifier(player), player);
						}
					}
				}
			}

			if(mc.thePlayer.getEquipmentInSlot(3) != null && mc.thePlayer.getEquipmentInSlot(3).getItem() instanceof ItemJetpack)
			{
				MekanismClient.updateKey(mc.gameSettings.keyBindJump, KeySync.ASCEND);
				MekanismClient.updateKey(mc.gameSettings.keyBindSneak, KeySync.DESCEND);
			}

			if(isJetpackOn(mc.thePlayer))
			{
				ItemJetpack jetpack = (ItemJetpack)mc.thePlayer.getEquipmentInSlot(3).getItem();

				if(jetpack.getMode(mc.thePlayer.getEquipmentInSlot(3)) == JetpackMode.NORMAL)
				{
					mc.thePlayer.motionY = Math.min(mc.thePlayer.motionY + 0.15D, 0.5D);
					mc.thePlayer.fallDistance = 0.0F;
				}
				else if(jetpack.getMode(mc.thePlayer.getEquipmentInSlot(3)) == JetpackMode.HOVER)
				{
					if((!mc.gameSettings.keyBindJump.getIsKeyPressed() && !mc.gameSettings.keyBindSneak.getIsKeyPressed()) || (mc.gameSettings.keyBindJump.getIsKeyPressed() && mc.gameSettings.keyBindSneak.getIsKeyPressed()) || mc.currentScreen != null)
					{
						if(mc.thePlayer.motionY > 0)
						{
							mc.thePlayer.motionY = Math.max(mc.thePlayer.motionY - 0.15D, 0);
						}
						else if(mc.thePlayer.motionY < 0)
						{
							mc.thePlayer.motionY = Math.min(mc.thePlayer.motionY + 0.15D, 0);
						}
					}
					else {
						if(mc.gameSettings.keyBindJump.getIsKeyPressed() && mc.currentScreen == null)
						{
							mc.thePlayer.motionY = Math.min(mc.thePlayer.motionY + 0.15D, 0.2D);
						}
						else if(mc.gameSettings.keyBindSneak.getIsKeyPressed() && mc.currentScreen == null)
						{
							mc.thePlayer.motionY = Math.max(mc.thePlayer.motionY - 0.15D, -0.2D);
						}
					}

					mc.thePlayer.fallDistance = 0.0F;
				}

				jetpack.useGas(mc.thePlayer.getEquipmentInSlot(3));
			}

			if(isGasMaskOn(mc.thePlayer))
			{
				ItemScubaTank tank = (ItemScubaTank)mc.thePlayer.getEquipmentInSlot(3).getItem();

				final int max = 300;

				tank.useGas(mc.thePlayer.getEquipmentInSlot(3));
				GasStack received = tank.removeGas(mc.thePlayer.getEquipmentInSlot(3), max-mc.thePlayer.getAir());

				if(received != null)
				{
					mc.thePlayer.setAir(mc.thePlayer.getAir()+received.amount);

					if(mc.thePlayer.getAir() == max)
					{
						mc.thePlayer.clearActivePotions();
					}
				}
			}
		}
	}

	public static void killDeadNetworks()
	{
		for(Iterator<IClientTicker> iter = tickingSet.iterator(); iter.hasNext();)
		{
			if(!iter.next().needsTicks())
			{
				iter.remove();
			}
		}
	}

	public static boolean isJetpackOn(EntityPlayer player)
	{
		if(player != mc.thePlayer)
		{
			return Mekanism.jetpackOn.contains(player.getCommandSenderName());
		}

		ItemStack stack = player.inventory.armorInventory[2];

		if(stack != null)
		{
			if(stack.getItem() instanceof ItemJetpack)
			{
				ItemJetpack jetpack = (ItemJetpack)stack.getItem();

				if(jetpack.getGas(stack) != null)
				{
					if((mc.gameSettings.keyBindJump.getIsKeyPressed() && jetpack.getMode(stack) == JetpackMode.NORMAL) && mc.currentScreen == null)
					{
						return true;
					}
					else if(jetpack.getMode(stack) == JetpackMode.HOVER)
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	public static boolean isGasMaskOn(EntityPlayer player)
	{
		if(player != mc.thePlayer)
		{
			return Mekanism.gasmaskOn.contains(player.getCommandSenderName());
		}

		ItemStack tank = player.inventory.armorInventory[2];
		ItemStack mask = player.inventory.armorInventory[3];

		if(tank != null && mask != null)
		{
			if(tank.getItem() instanceof ItemScubaTank && mask.getItem() instanceof ItemGasMask)
			{
				ItemScubaTank scubaTank = (ItemScubaTank)tank.getItem();

				if(scubaTank.getGas(tank) != null)
				{
					if(scubaTank.getFlowing(tank))
					{
						return true;
					}
				}
			}
		}

		return false;
	}
	
	public static boolean isFlamethrowerOn(EntityPlayer player)
	{
		if(hasFlamethrower(player))
		{
			if(mc.gameSettings.keyBindUseItem.getIsKeyPressed())
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean hasFlamethrower(EntityPlayer player)
	{
		if(player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof ItemFlamethrower)
		{
			ItemFlamethrower flamethrower = (ItemFlamethrower)player.getCurrentEquippedItem().getItem();
			
			if(flamethrower.getGas(player.getCurrentEquippedItem()) != null)
			{
				return true;
			}
		}
		
		return false;
	}

	public void tickEnd()
	{
		if(MekanismClient.audioHandler != null)
		{
			synchronized(MekanismClient.audioHandler.soundMaps)
			{
				MekanismClient.audioHandler.onTick();
			}
		}
	}
}
