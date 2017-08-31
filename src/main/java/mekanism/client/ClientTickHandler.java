package mekanism.client;

import static mekanism.client.sound.SoundHandler.Channel.FLAMETHROWER;
import static mekanism.client.sound.SoundHandler.Channel.GASMASK;
import static mekanism.client.sound.SoundHandler.Channel.JETPACK;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import mekanism.api.IClientTicker;
import mekanism.api.gas.GasStack;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.sound.SoundHandler;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.KeySync;
import mekanism.common.Mekanism;
import mekanism.common.ObfuscatedNames;
import mekanism.common.config.MekanismConfig.client;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.frequency.Frequency;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.item.ItemFreeRunners;
import mekanism.common.item.ItemGasMask;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.ItemJetpack.JetpackMode;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.network.PacketFlamethrowerData;
import mekanism.common.network.PacketConfiguratorState.ConfiguratorStateMessage;
import mekanism.common.network.PacketFlamethrowerData.FlamethrowerDataMessage;
import mekanism.common.network.PacketJetpackData.JetpackDataMessage;
import mekanism.common.network.PacketJetpackData.JetpackPacket;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterMessage;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterPacketType;
import mekanism.common.network.PacketScubaTankData.ScubaTankDataMessage;
import mekanism.common.network.PacketScubaTankData.ScubaTankPacket;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.ReflectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;

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
	public static Random rand = new Random();

	public static final String DONATE_CAPE = "http://aidancbrady.com/data/capes/donate.png";
	public static final String AIDAN_CAPE = "http://aidancbrady.com/data/capes/aidan.png";

	private Map<String, CapeBufferDownload> donateDownload = new HashMap<String, CapeBufferDownload>();
	private Map<String, CapeBufferDownload> aidanDownload = new HashMap<String, CapeBufferDownload>();

	public static Set<IClientTicker> tickingSet = new HashSet<IClientTicker>();
	public static Map<EntityPlayer, TeleportData> portableTeleports = new HashMap<>();
	
	public static int wheelStatus = 0;

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

		if(mc.theWorld != null && mc.thePlayer != null && !Mekanism.proxy.isPaused())
		{
			if((!initHoliday || MekanismClient.ticksPassed % 1200 == 0) && mc.thePlayer != null)
			{
				HolidayManager.check();
				initHoliday = true;
			}

			for(EntityPlayer entityPlayer : mc.theWorld.playerEntities)
			{
				if(entityPlayer instanceof AbstractClientPlayer)
				{
					AbstractClientPlayer player = (AbstractClientPlayer)entityPlayer;

					if(StringUtils.stripControlCodes(player.getName()).equals("aidancbrady"))
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
							
							setCape(player, download.getResourceLocation());
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
							
							setCape(player, download.getResourceLocation());
						}
					}
				}
			}
			
			ItemStack bootStack = mc.thePlayer.getItemStackFromSlot(EntityEquipmentSlot.FEET);

			if(bootStack != null && bootStack.getItem() instanceof ItemFreeRunners)
			{
				mc.thePlayer.stepHeight = 1.002F;
			}
			else {
				if(mc.thePlayer.stepHeight == 1.002F)
				{
					mc.thePlayer.stepHeight = 0.5F;
				}
			}
			
			if(Mekanism.flamethrowerActive.contains(mc.thePlayer.getName()) != isFlamethrowerOn(mc.thePlayer))
			{
				if(isFlamethrowerOn(mc.thePlayer))
				{
					Mekanism.flamethrowerActive.add(mc.thePlayer.getName());
				}
				else {
					Mekanism.flamethrowerActive.remove(mc.thePlayer.getName());
				}
				
				Mekanism.packetHandler.sendToServer(new FlamethrowerDataMessage(PacketFlamethrowerData.FlamethrowerPacket.UPDATE, null, mc.thePlayer.getName(), isFlamethrowerOn(mc.thePlayer)));
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

				for(EntityPlayer player : (List<EntityPlayer>)mc.theWorld.playerEntities)
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
			
			for(Iterator<Entry<EntityPlayer, TeleportData>> iter = portableTeleports.entrySet().iterator(); iter.hasNext();)
			{
				Entry<EntityPlayer, TeleportData> entry = iter.next();
				
				for(int i = 0; i < 100; i++)
				{
					double x = entry.getKey().posX + rand.nextDouble()-0.5D;
					double y = entry.getKey().posY + rand.nextDouble()*2-2D;
					double z = entry.getKey().posZ + rand.nextDouble()-0.5D;
					
					mc.theWorld.spawnParticle(EnumParticleTypes.PORTAL, x, y, z, 0, 1, 0);
				}
				
				if(mc.theWorld.getWorldTime() == entry.getValue().teleportTime)
				{
					Mekanism.packetHandler.sendToServer(new PortableTeleporterMessage(PortableTeleporterPacketType.TELEPORT, entry.getValue().hand, entry.getValue().freq));
					iter.remove();
				}
			}

			ItemStack chestStack = mc.thePlayer.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
			
			if(chestStack != null && chestStack.getItem() instanceof ItemJetpack)
			{
				MekanismClient.updateKey(mc.gameSettings.keyBindJump, KeySync.ASCEND);
				MekanismClient.updateKey(mc.gameSettings.keyBindSneak, KeySync.DESCEND);
			}
			
			if(isFlamethrowerOn(mc.thePlayer))
			{
				ItemFlamethrower flamethrower = (ItemFlamethrower)mc.thePlayer.inventory.getCurrentItem().getItem();
				
				if(!mc.thePlayer.capabilities.isCreativeMode)
				{
					flamethrower.useGas(mc.thePlayer.inventory.getCurrentItem());
				}
			}
			
			if(isJetpackOn(mc.thePlayer))
			{
				ItemJetpack jetpack = (ItemJetpack)chestStack.getItem();

				if(jetpack.getMode(chestStack) == JetpackMode.NORMAL)
				{
					mc.thePlayer.motionY = Math.min(mc.thePlayer.motionY + 0.15D, 0.5D);
					mc.thePlayer.fallDistance = 0.0F;
				}
				else if(jetpack.getMode(chestStack) == JetpackMode.HOVER)
				{
					if((!mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) || (mc.gameSettings.keyBindJump.isKeyDown() && mc.gameSettings.keyBindSneak.isKeyDown()) || mc.currentScreen != null)
					{
						if(mc.thePlayer.motionY > 0)
						{
							mc.thePlayer.motionY = Math.max(mc.thePlayer.motionY - 0.15D, 0);
						}
						else if(mc.thePlayer.motionY < 0)
						{
							if(!CommonPlayerTickHandler.isOnGround(mc.thePlayer))
							{
								mc.thePlayer.motionY = Math.min(mc.thePlayer.motionY + 0.15D, 0);
							}
						}
					}
					else {
						if(mc.gameSettings.keyBindJump.isKeyDown() && mc.currentScreen == null)
						{
							mc.thePlayer.motionY = Math.min(mc.thePlayer.motionY + 0.15D, 0.2D);
						}
						else if(mc.gameSettings.keyBindSneak.isKeyDown() && mc.currentScreen == null)
						{
							if(!CommonPlayerTickHandler.isOnGround(mc.thePlayer))
							{
								mc.thePlayer.motionY = Math.max(mc.thePlayer.motionY - 0.15D, -0.2D);
							}
						}
					}

					mc.thePlayer.fallDistance = 0.0F;
				}

				jetpack.useGas(chestStack);
			}

			if(isGasMaskOn(mc.thePlayer))
			{
				ItemScubaTank tank = (ItemScubaTank)chestStack.getItem();

				final int max = 300;
				
				tank.useGas(chestStack);
				GasStack received = tank.useGas(chestStack, max-mc.thePlayer.getAir());

				if(received != null)
				{
					mc.thePlayer.setAir(mc.thePlayer.getAir()+received.amount);
				}
				
				if(mc.thePlayer.getAir() == max)
				{
					for(Object obj : mc.thePlayer.getActivePotionEffects())
					{
						if(obj instanceof PotionEffect)
						{
							for(int i = 0; i < 9; i++)
							{
								((PotionEffect)obj).onUpdate(mc.thePlayer);
							}
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onMouseEvent(MouseEvent event)
	{
		if(client.allowConfiguratorModeScroll && mc.thePlayer != null && mc.thePlayer.isSneaking())
		{
			ItemStack stack = mc.thePlayer.getHeldItemMainhand();
			int delta = event.getDwheel();
			
			if(stack != null && stack.getItem() instanceof ItemConfigurator && delta != 0)
			{
				ItemConfigurator configurator = (ItemConfigurator)stack.getItem();
				RenderTickHandler.modeSwitchTimer = 100;
				
				wheelStatus += event.getDwheel();
				int scaledDelta = wheelStatus/120;
				wheelStatus = wheelStatus % 120;
				int newVal = configurator.getState(stack).ordinal() + (scaledDelta % ConfiguratorMode.values().length);
				
				if(newVal > 0)
				{
					newVal = newVal % ConfiguratorMode.values().length;
				}
				else if(newVal < 0) 
				{
					newVal = ConfiguratorMode.values().length + newVal;
				}
				
				configurator.setState(stack, ConfiguratorMode.values()[newVal]);
				Mekanism.packetHandler.sendToServer(new ConfiguratorStateMessage(EnumHand.MAIN_HAND, configurator.getState(stack)));
				event.setCanceled(true);
			}
		}
	}
	
	public static void setCape(AbstractClientPlayer player, ResourceLocation cape)
	{
		try {
			Method m = ReflectionUtils.getPrivateMethod(AbstractClientPlayer.class, ObfuscatedNames.AbstractClientPlayer_getPlayerInfo);
			Object obj = m.invoke(player);
			
			if(obj instanceof NetworkPlayerInfo)
			{
				NetworkPlayerInfo info = (NetworkPlayerInfo)obj;
				Map<MinecraftProfileTexture.Type, ResourceLocation> map = (Map<MinecraftProfileTexture.Type, ResourceLocation>)ReflectionUtils.getPrivateValue(info, NetworkPlayerInfo.class, ObfuscatedNames.NetworkPlayerInfo_playerTextures);
				map.put(MinecraftProfileTexture.Type.CAPE, cape);
			}
		} catch(Exception e) {}
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

		if(stack != null && !player.capabilities.isCreativeMode)
		{
			if(stack.getItem() instanceof ItemJetpack)
			{
				ItemJetpack jetpack = (ItemJetpack)stack.getItem();

				if(jetpack.getGas(stack) != null)
				{
					if((mc.gameSettings.keyBindJump.isKeyDown() && jetpack.getMode(stack) == JetpackMode.NORMAL) && mc.currentScreen == null)
					{
						return true;
					}
					else if(jetpack.getMode(stack) == JetpackMode.HOVER)
					{
						if((!mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) || (mc.gameSettings.keyBindJump.isKeyDown() && mc.gameSettings.keyBindSneak.isKeyDown()) || mc.currentScreen != null)
						{
							return !CommonPlayerTickHandler.isOnGround(player);
						}
						else if(mc.gameSettings.keyBindSneak.isKeyDown() && mc.currentScreen == null)
						{
							return !CommonPlayerTickHandler.isOnGround(player);
						}
						
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
		if(player != mc.thePlayer)
		{
			return Mekanism.flamethrowerActive.contains(player.getName());
		}
		
		if(hasFlamethrower(player))
		{
			if(mc.gameSettings.keyBindUseItem.isKeyDown())
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean hasFlamethrower(EntityPlayer player)
	{
		if(player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() instanceof ItemFlamethrower)
		{
			ItemFlamethrower flamethrower = (ItemFlamethrower)player.inventory.getCurrentItem().getItem();
			
			if(flamethrower.getGas(player.inventory.getCurrentItem()) != null)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static void portableTeleport(EntityPlayer player, EnumHand hand, Frequency freq)
	{
		if(general.portableTeleporterDelay == 0)
		{
			Mekanism.packetHandler.sendToServer(new PortableTeleporterMessage(PortableTeleporterPacketType.TELEPORT, hand, freq));
		}
		else {
			portableTeleports.put(player, new TeleportData(hand, freq, mc.theWorld.getWorldTime()+general.portableTeleporterDelay));
		}
	}
	
	private static class TeleportData
	{
		private EnumHand hand;
		private Frequency freq;
		private long teleportTime;
		
		public TeleportData(EnumHand h, Frequency f, long t)
		{
			hand = h;
			freq = f;
			teleportTime = t;
		}
	}
}
