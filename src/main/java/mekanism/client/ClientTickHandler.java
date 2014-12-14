package mekanism.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mekanism.api.IClientTicker;
import mekanism.api.MekanismConfig.client;
import mekanism.api.gas.GasStack;
import mekanism.client.sound.SoundHandler;
import mekanism.common.KeySync;
import mekanism.common.Mekanism;
import mekanism.common.ObfuscatedNames;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.item.ItemFreeRunners;
import mekanism.common.item.ItemGasMask;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemJetpack.JetpackMode;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.network.PacketFlamethrowerActive.FlamethrowerActiveMessage;
import mekanism.common.network.PacketJetpackData.JetpackDataMessage;
import mekanism.common.network.PacketJetpackData.JetpackPacket;
import mekanism.common.network.PacketScubaTankData.ScubaTankDataMessage;
import mekanism.common.network.PacketScubaTankData.ScubaTankPacket;
import mekanism.common.util.MekanismUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static mekanism.client.sound.SoundHandler.Channel.FLAMETHROWER;
import static mekanism.client.sound.SoundHandler.Channel.GASMASK;
import static mekanism.client.sound.SoundHandler.Channel.JETPACK;

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
	}

	public void tickStart()
	{
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
						if(StringUtils.stripControlCodes(player.getName()).equals("mikeacttck"))
						{
							CapeBufferDownload download = mikeDownload.get(player.getName());

							if(download == null)
							{
								download = new CapeBufferDownload(player.getName(), MIKE_CAPE);
								mikeDownload.put(player.getName(), download);

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
						else if(StringUtils.stripControlCodes(player.getName()).equals("aidancbrady"))
						{
							CapeBufferDownload download = aidanDownload.get(player.getName());

							if(download == null)
							{
								download = new CapeBufferDownload(player.getName(), AIDAN_CAPE);
								aidanDownload.put(player.getName(), download);

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
						else if(Mekanism.donators.contains(StringUtils.stripControlCodes(player.getName())))
						{
							CapeBufferDownload download = donateDownload.get(player.getName());

							if(download == null)
							{
								download = new CapeBufferDownload(player.getName(), DONATE_CAPE);
								donateDownload.put(player.getName(), download);

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
			
			if(isFlamethrowerOn(mc.thePlayer) != Mekanism.flamethrowerActive.contains(mc.thePlayer.getName()))
			{
				if(isFlamethrowerOn(mc.thePlayer))
				{
					Mekanism.flamethrowerActive.add(mc.thePlayer.getName());
				}
				else {
					Mekanism.flamethrowerActive.remove(mc.thePlayer);
				}
				
				Mekanism.packetHandler.sendToServer(new FlamethrowerActiveMessage(isFlamethrowerOn(mc.thePlayer)));
			}

			if(Mekanism.jetpackOn.contains(mc.thePlayer.getName()) != isJetpackOn(mc.thePlayer))
			{
				if(isJetpackOn(mc.thePlayer))
				{
					Mekanism.jetpackOn.add(mc.thePlayer.getName());
				}
				else {
					Mekanism.jetpackOn.remove(mc.thePlayer.getName());
				}

				Mekanism.packetHandler.sendToServer(new JetpackDataMessage(JetpackPacket.UPDATE, mc.thePlayer.getName(), isJetpackOn(mc.thePlayer)));
			}

			if(Mekanism.gasmaskOn.contains(mc.thePlayer.getName()) != isGasMaskOn(mc.thePlayer))
			{
				if(isGasMaskOn(mc.thePlayer) && mc.currentScreen == null)
				{
					Mekanism.gasmaskOn.add(mc.thePlayer.getName());
				}
				else {
					Mekanism.gasmaskOn.remove(mc.thePlayer.getName());
				}

				Mekanism.packetHandler.sendToServer(new ScubaTankDataMessage(ScubaTankPacket.UPDATE, mc.thePlayer.getName(), isGasMaskOn(mc.thePlayer)));
			}

			if(client.enablePlayerSounds)
			{
				for(String username : Mekanism.jetpackOn)
				{
					EntityPlayer player = mc.theWorld.getPlayerEntityByName(username);

					if(player != null)
					{
						if(!SoundHandler.soundPlaying(player, JETPACK))
						{
							SoundHandler.addSound(player, JETPACK, client.replaceSoundsWhenResuming);
						}
						SoundHandler.playSound(player, JETPACK);
					}
				}

				for(String username : Mekanism.gasmaskOn)
				{
					EntityPlayer player = mc.theWorld.getPlayerEntityByName(username);

					if(player != null)
					{
						if(!SoundHandler.soundPlaying(player, GASMASK))
						{
							SoundHandler.addSound(player, GASMASK, client.replaceSoundsWhenResuming);
						}
						SoundHandler.playSound(player, GASMASK);
					}
				}

				for(EntityPlayer player : (List<EntityPlayer>) mc.theWorld.playerEntities)
				{
					if(hasFlamethrower(player))
					{
						if(!SoundHandler.soundPlaying(player, FLAMETHROWER))
						{
							SoundHandler.addSound(player, FLAMETHROWER, client.replaceSoundsWhenResuming);
						}
						SoundHandler.playSound(player, FLAMETHROWER);
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
					if((!mc.gameSettings.keyBindJump.isPressed() && !mc.gameSettings.keyBindSneak.isPressed()) || (mc.gameSettings.keyBindJump.isPressed() && mc.gameSettings.keyBindSneak.isPressed()) || mc.currentScreen != null)
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
						if(mc.gameSettings.keyBindJump.isPressed() && mc.currentScreen == null)
						{
							mc.thePlayer.motionY = Math.min(mc.thePlayer.motionY + 0.15D, 0.2D);
						}
						else if(mc.gameSettings.keyBindSneak.isPressed() && mc.currentScreen == null)
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
			return Mekanism.jetpackOn.contains(player.getName());
		}

		ItemStack stack = player.inventory.armorInventory[2];

		if(stack != null)
		{
			if(stack.getItem() instanceof ItemJetpack)
			{
				ItemJetpack jetpack = (ItemJetpack)stack.getItem();

				if(jetpack.getGas(stack) != null)
				{
					if((mc.gameSettings.keyBindJump.isPressed() && jetpack.getMode(stack) == JetpackMode.NORMAL) && mc.currentScreen == null)
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
			return Mekanism.gasmaskOn.contains(player.getName());
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
			if(mc.gameSettings.keyBindUseItem.isPressed())
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
}
