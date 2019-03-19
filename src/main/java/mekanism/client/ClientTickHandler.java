package mekanism.client;

import static mekanism.client.sound.SoundHandler.Channel.FLAMETHROWER;
import static mekanism.client.sound.SoundHandler.Channel.GASMASK;
import static mekanism.client.sound.SoundHandler.Channel.JETPACK;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import mekanism.api.IClientTicker;
import mekanism.api.gas.GasStack;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.sound.SoundHandler;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.KeySync;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.Frequency;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.item.ItemFreeRunners;
import mekanism.common.item.ItemGasMask;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemJetpack.JetpackMode;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.network.PacketFlamethrowerData;
import mekanism.common.network.PacketFlamethrowerData.FlamethrowerDataMessage;
import mekanism.common.network.PacketFreeRunnerData;
import mekanism.common.network.PacketItemStack.ItemStackMessage;
import mekanism.common.network.PacketJetpackData.JetpackDataMessage;
import mekanism.common.network.PacketJetpackData.JetpackPacket;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterMessage;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterPacketType;
import mekanism.common.network.PacketScubaTankData.ScubaTankDataMessage;
import mekanism.common.network.PacketScubaTankData.ScubaTankPacket;
import mekanism.common.util.ListUtils;
import mekanism.common.util.MekanismUtils;
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

	public static final UUID UUID_AIDAN = UUID.fromString("38da5620-548e-4662-bbe1-4543ea5c3c38");

	private Map<UUID, CapeBufferDownload> donateDownload = new HashMap<>();
	private Object2BooleanMap<UUID> donateChecks = new Object2BooleanOpenHashMap<>();
	private CapeBufferDownload aidanDownload = null;

	public static Set<IClientTicker> tickingSet = new HashSet<>();
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

		if(!hasNotified && mc.world != null && Mekanism.latestVersionNumber != null && Mekanism.recentNews != null)
		{
			MekanismUtils.checkForUpdates(mc.player);
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

		if(mc.world != null)
		{
			shouldReset = true;
		}
		else if(shouldReset)
		{
			MekanismClient.reset();
			shouldReset = false;
		}

		if(mc.world != null && mc.player != null && !Mekanism.proxy.isPaused())
		{
			if((!initHoliday || MekanismClient.ticksPassed % 1200 == 0) && mc.player != null)
			{
				HolidayManager.check();
				initHoliday = true;
			}

			for(EntityPlayer entityPlayer : mc.world.playerEntities)
			{
				if(entityPlayer instanceof AbstractClientPlayer)
				{
					AbstractClientPlayer player = (AbstractClientPlayer)entityPlayer;

					if(player.getUniqueID().equals(UUID_AIDAN))
					{
						if(aidanDownload == null)
						{
							aidanDownload = new CapeBufferDownload(player.getName(), AIDAN_CAPE);

							aidanDownload.start();
						}
						else {
							if(!aidanDownload.downloaded)
							{
								continue;
							}
							
							setCape(player, aidanDownload.getResourceLocation());
						}
					}
					else if(isDonator(player))
					{
						CapeBufferDownload download = donateDownload.get(player.getUniqueID());

						if(download == null)
						{
							download = new CapeBufferDownload(player.getName(), DONATE_CAPE);
							donateDownload.put(player.getUniqueID(), download);

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

			if(Mekanism.freeRunnerOn.contains(mc.player.getUniqueID()) != isFreeRunnerOn(mc.player))
			{
				if(isFreeRunnerOn(mc.player) && mc.currentScreen == null)
				{
					Mekanism.freeRunnerOn.add(mc.player.getUniqueID());
				}
				else
				{
					Mekanism.freeRunnerOn.remove(mc.player.getUniqueID());
				}

				Mekanism.packetHandler.sendToServer(new PacketFreeRunnerData.FreeRunnerDataMessage(PacketFreeRunnerData.FreeRunnerPacket.UPDATE, mc.player.getUniqueID(), isFreeRunnerOn(mc.player)));
			}

			ItemStack bootStack = mc.player.getItemStackFromSlot(EntityEquipmentSlot.FEET);

			if(!bootStack.isEmpty() && bootStack.getItem() instanceof ItemFreeRunners && isFreeRunnerOn(mc.player))
			{
				mc.player.stepHeight = 1.002F;
			}
			else {
				if(mc.player.stepHeight == 1.002F)
				{
					mc.player.stepHeight = 0.6F;
				}
			}
			
			if(Mekanism.flamethrowerActive.contains(mc.player.getUniqueID()) != isFlamethrowerOn(mc.player))
			{
				if(isFlamethrowerOn(mc.player))
				{
					Mekanism.flamethrowerActive.add(mc.player.getUniqueID());
				}
				else {
					Mekanism.flamethrowerActive.remove(mc.player.getUniqueID());
				}
				
				Mekanism.packetHandler.sendToServer(new FlamethrowerDataMessage(PacketFlamethrowerData.FlamethrowerPacket.UPDATE, null, mc.player.getUniqueID(), isFlamethrowerOn(mc.player)));
			}

			if(Mekanism.jetpackOn.contains(mc.player.getUniqueID()) != isJetpackOn(mc.player))
			{
				if(isJetpackOn(mc.player))
				{
					Mekanism.jetpackOn.add(mc.player.getUniqueID());
				}
				else {
					Mekanism.jetpackOn.remove(mc.player.getUniqueID());
				}

				Mekanism.packetHandler.sendToServer(new JetpackDataMessage(JetpackPacket.UPDATE, mc.player.getUniqueID(), isJetpackOn(mc.player)));
			}

			if(Mekanism.gasmaskOn.contains(mc.player.getUniqueID()) != isGasMaskOn(mc.player))
			{
				if(isGasMaskOn(mc.player) && mc.currentScreen == null)
				{
					Mekanism.gasmaskOn.add(mc.player.getUniqueID());
				}
				else {
					Mekanism.gasmaskOn.remove(mc.player.getUniqueID());
				}

				Mekanism.packetHandler.sendToServer(new ScubaTankDataMessage(ScubaTankPacket.UPDATE, mc.player.getUniqueID(), isGasMaskOn(mc.player)));
			}

			if(MekanismConfig.current().client.enablePlayerSounds.val())
			{
				for(UUID playerId : Mekanism.jetpackOn)
				{
					playPlayerSound(playerId, JETPACK);
				}

				for(UUID playerId : Mekanism.gasmaskOn)
				{
					playPlayerSound(playerId, GASMASK);
				}

				for(EntityPlayer player : mc.world.playerEntities)
				{
					if(hasFlamethrower(player))
					{
						playPlayerSound(player.getUniqueID(),  FLAMETHROWER);
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
					
					mc.world.spawnParticle(EnumParticleTypes.PORTAL, x, y, z, 0, 1, 0);
				}
				
				if(mc.world.getWorldTime() == entry.getValue().teleportTime)
				{
					Mekanism.packetHandler.sendToServer(new PortableTeleporterMessage(PortableTeleporterPacketType.TELEPORT, entry.getValue().hand, entry.getValue().freq));
					iter.remove();
				}
			}

			ItemStack chestStack = mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
			
			if(!chestStack.isEmpty() && chestStack.getItem() instanceof ItemJetpack)
			{
				MekanismClient.updateKey(mc.gameSettings.keyBindJump, KeySync.ASCEND);
				MekanismClient.updateKey(mc.gameSettings.keyBindSneak, KeySync.DESCEND);
			}
			
			if(isFlamethrowerOn(mc.player))
			{
				ItemFlamethrower flamethrower = (ItemFlamethrower)mc.player.inventory.getCurrentItem().getItem();
				
				if(!(mc.player.isCreative() || mc.player.isSpectator()))
				{
					flamethrower.useGas(mc.player.inventory.getCurrentItem());
				}
			}
			
			if(isJetpackOn(mc.player))
			{
				ItemJetpack jetpack = (ItemJetpack)chestStack.getItem();

				if(jetpack.getMode(chestStack) == JetpackMode.NORMAL)
				{
					mc.player.motionY = Math.min(mc.player.motionY + 0.15D, 0.5D);
					mc.player.fallDistance = 0.0F;
				}
				else if(jetpack.getMode(chestStack) == JetpackMode.HOVER)
				{
					if((!mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) || (mc.gameSettings.keyBindJump.isKeyDown() && mc.gameSettings.keyBindSneak.isKeyDown()) || mc.currentScreen != null)
					{
						if(mc.player.motionY > 0)
						{
							mc.player.motionY = Math.max(mc.player.motionY - 0.15D, 0);
						}
						else if(mc.player.motionY < 0)
						{
							if(!CommonPlayerTickHandler.isOnGround(mc.player))
							{
								mc.player.motionY = Math.min(mc.player.motionY + 0.15D, 0);
							}
						}
					}
					else {
						if(mc.gameSettings.keyBindJump.isKeyDown() && mc.currentScreen == null)
						{
							mc.player.motionY = Math.min(mc.player.motionY + 0.15D, 0.2D);
						}
						else if(mc.gameSettings.keyBindSneak.isKeyDown() && mc.currentScreen == null)
						{
							if(!CommonPlayerTickHandler.isOnGround(mc.player))
							{
								mc.player.motionY = Math.max(mc.player.motionY - 0.15D, -0.2D);
							}
						}
					}

					mc.player.fallDistance = 0.0F;
				}

				jetpack.useGas(chestStack);
			}

			if(isGasMaskOn(mc.player))
			{
				ItemScubaTank tank = (ItemScubaTank)chestStack.getItem();

				final int max = 300;
				
				tank.useGas(chestStack);
				GasStack received = tank.useGas(chestStack, max-mc.player.getAir());

				if(received != null)
				{
					mc.player.setAir(mc.player.getAir()+received.amount);
				}
				
				if(mc.player.getAir() == max)
				{
					for(PotionEffect obj : mc.player.getActivePotionEffects())
					{
						if(obj != null)
						{
							for(int i = 0; i < 9; i++)
							{
								obj.onUpdate(mc.player);
							}
						}
					}
				}
			}
		}
	}

	private boolean isDonator(AbstractClientPlayer player)
	{
		UUID uniqueID = player.getUniqueID();
		if (donateChecks.containsKey(uniqueID))
		{
			return donateChecks.getBoolean(uniqueID);
		}
		boolean isDonator = Mekanism.donators.contains(StringUtils.stripControlCodes(player.getName()));
		donateChecks.put(uniqueID, isDonator);
		return isDonator;
	}

	private void playPlayerSound(UUID playerId, SoundHandler.Channel soundChannel)
	{
		EntityPlayer player = mc.world.getPlayerEntityByUUID(playerId);

		if(player != null)
		{
			if(!SoundHandler.soundPlaying(player, soundChannel))
			{
				SoundHandler.addSound(player, soundChannel, MekanismConfig.current().client.replaceSoundsWhenResuming.val());
			}

			SoundHandler.playSound(player, soundChannel);
		}
	}

	@SubscribeEvent
	public void onMouseEvent(MouseEvent event)
	{
		if(MekanismConfig.current().client.allowConfiguratorModeScroll.val() && mc.player != null && mc.player.isSneaking())
		{
			ItemStack stack = mc.player.getHeldItemMainhand();
			int delta = event.getDwheel();
			
			if(stack.getItem() instanceof ItemConfigurator && delta != 0)
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
				Mekanism.packetHandler.sendToServer(new ItemStackMessage(EnumHand.MAIN_HAND, ListUtils.asArrayList(newVal)));
				event.setCanceled(true);
			}
		}
	}
	
	public static void setCape(AbstractClientPlayer player, ResourceLocation cape)
	{
		NetworkPlayerInfo info = player.getPlayerInfo();

		if (info != null) {
			info.playerTextures.put(MinecraftProfileTexture.Type.CAPE, cape);
		}
	}

	public static void killDeadNetworks()
	{
		tickingSet.removeIf(iClientTicker -> !iClientTicker.needsTicks());
	}

	public static boolean isJetpackOn(EntityPlayer player)
	{
		if(player != mc.player)
		{
			return Mekanism.jetpackOn.contains(player.getUniqueID());
		}

		ItemStack stack = player.inventory.armorInventory.get(2);

		if(!stack.isEmpty() && ! (player.isCreative() || player.isSpectator()))
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
		if(player != mc.player)
		{
			return Mekanism.gasmaskOn.contains(player.getUniqueID());
		}

		return CommonPlayerTickHandler.isGasMaskOn(player);
	}

	public static boolean isFreeRunnerOn(EntityPlayer player)
	{
		if(player != mc.player)
		{
			return Mekanism.freeRunnerOn.contains(player.getUniqueID());
		}

		ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);

		if(!stack.isEmpty() && stack.getItem() instanceof ItemFreeRunners)
		{
			ItemFreeRunners freeRunners = (ItemFreeRunners) stack.getItem();

			if(/*freeRunners.getEnergy(stack) > 0 && */freeRunners.getMode(stack) == ItemFreeRunners.FreeRunnerMode.NORMAL)
			{
				return true;
			}
		}

		return false;
	}
	
	public static boolean isFlamethrowerOn(EntityPlayer player)
	{
		if(player != mc.player)
		{
			return Mekanism.flamethrowerActive.contains(player.getUniqueID());
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
		if(!player.inventory.getCurrentItem().isEmpty() && player.inventory.getCurrentItem().getItem() instanceof ItemFlamethrower)
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
		if(MekanismConfig.current().general.portableTeleporterDelay.val() == 0)
		{
			Mekanism.packetHandler.sendToServer(new PortableTeleporterMessage(PortableTeleporterPacketType.TELEPORT, hand, freq));
		}
		else {
			portableTeleports.put(player, new TeleportData(hand, freq, mc.world.getWorldTime()+ MekanismConfig.current().general.portableTeleporterDelay.val()));
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
