package mekanism.common;

import mekanism.api.gas.GasStack;
import mekanism.common.item.ItemFreeRunners;
import mekanism.common.item.ItemGasMask;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemJetpack.JetpackMode;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.network.PacketStatusUpdate.StatusUpdateMessage;
import mekanism.common.util.MekanismUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.relauncher.Side;

public class CommonPlayerTickHandler
{
	@SubscribeEvent
	public void onTick(PlayerTickEvent event)
	{
		if(event.phase == Phase.END && event.side == Side.SERVER)
		{
			tickEnd(event.player);
		}
	}

	public void tickEnd(EntityPlayer player)
	{
		if(player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof ItemPortableTeleporter)
		{
			ItemPortableTeleporter item = (ItemPortableTeleporter)player.getCurrentEquippedItem().getItem();
			ItemStack itemstack = player.getCurrentEquippedItem();

			Teleporter.Code teleCode = new Teleporter.Code(item.getDigit(itemstack, 0), item.getDigit(itemstack, 1), item.getDigit(itemstack, 2), item.getDigit(itemstack, 3));

			if(Mekanism.teleporters.containsKey(teleCode))
			{
				if(Mekanism.teleporters.get(teleCode).size() > 0 && Mekanism.teleporters.get(teleCode).size() <= 2)
				{
					int energyNeeded = item.calculateEnergyCost(player, MekanismUtils.getClosestCoords(teleCode, player));

					if(item.getEnergy(itemstack) < energyNeeded)
					{
						if(item.getStatus(itemstack) != 2)
						{
							item.setStatus(itemstack, 2);
							Mekanism.packetHandler.sendTo(new StatusUpdateMessage(2), (EntityPlayerMP)player);
						}
					}
					else {
						if(item.getStatus(itemstack) != 1)
						{
							item.setStatus(itemstack, 1);
							Mekanism.packetHandler.sendTo(new StatusUpdateMessage(1), (EntityPlayerMP)player);
						}
					}
				}
				else if(Mekanism.teleporters.get(teleCode).size() > 2)
				{
					if(item.getStatus(itemstack) != 3)
					{
						item.setStatus(itemstack, 3);
						Mekanism.packetHandler.sendTo(new StatusUpdateMessage(3), (EntityPlayerMP)player);
					}
				}
				else {
					if(item.getStatus(itemstack) != 4)
					{
						item.setStatus(itemstack, 4);
						Mekanism.packetHandler.sendTo(new StatusUpdateMessage(4), (EntityPlayerMP)player);
					}
				}
			}
			else {
				if(item.getStatus(itemstack) != 4)
				{
					item.setStatus(itemstack, 4);
					Mekanism.packetHandler.sendTo(new StatusUpdateMessage(4), (EntityPlayerMP)player);
				}
			}
		}

		if(player.getEquipmentInSlot(1) != null && player.getEquipmentInSlot(1).getItem() instanceof ItemFreeRunners)
		{
			player.stepHeight = 1.002F;
		}
		else {
			if(player.stepHeight == 1.002F)
			{
				player.stepHeight = 0.5F;
			}
		}

		if(isJetpackOn(player))
		{
			ItemJetpack jetpack = (ItemJetpack)player.getEquipmentInSlot(3).getItem();

			if(jetpack.getMode(player.getEquipmentInSlot(3)) == JetpackMode.NORMAL)
			{
				player.motionY = Math.min(player.motionY + 0.15D, 0.5D);
			}
			else if(jetpack.getMode(player.getEquipmentInSlot(3)) == JetpackMode.HOVER)
			{
				if((!Mekanism.keyMap.has(player, KeySync.ASCEND) && !Mekanism.keyMap.has(player, KeySync.DESCEND)) || (Mekanism.keyMap.has(player, KeySync.ASCEND) && Mekanism.keyMap.has(player, KeySync.DESCEND)))
				{
					if(player.motionY > 0)
					{
						player.motionY = Math.max(player.motionY - 0.15D, 0);
					}
					else if(player.motionY < 0)
					{
						player.motionY = Math.min(player.motionY + 0.15D, 0);
					}
				}
				else {
					if(Mekanism.keyMap.has(player, KeySync.ASCEND))
					{
						player.motionY = Math.min(player.motionY + 0.15D, 0.2D);
					}
					else if(Mekanism.keyMap.has(player, KeySync.DESCEND))
					{
						player.motionY = Math.max(player.motionY - 0.15D, -0.2D);
					}
				}
			}

			player.fallDistance = 0.0F;

			if(player instanceof EntityPlayerMP)
			{
				MekanismUtils.setPrivateValue(((EntityPlayerMP)player).playerNetServerHandler, 0, NetHandlerPlayServer.class, ObfuscatedNames.NetHandlerPlayServer_floatingTickCount);
			}

			jetpack.useGas(player.getEquipmentInSlot(3));
		}

		if(isGasMaskOn(player))
		{
			ItemScubaTank tank = (ItemScubaTank)player.getEquipmentInSlot(3).getItem();

			final int max = 300;
			
			tank.useGas(player.getEquipmentInSlot(3));
			GasStack received = tank.removeGas(player.getEquipmentInSlot(3), max-player.getAir());
			
			if(received != null)
			{
				player.setAir(player.getAir()+received.amount);
				
				if(player.getAir() == max)
				{
					player.clearActivePotions();
				}
			}
		}
	}

	public boolean isJetpackOn(EntityPlayer player)
	{
		ItemStack stack = player.inventory.armorInventory[2];

		if(stack != null && !player.capabilities.isCreativeMode)
		{
			if(stack.getItem() instanceof ItemJetpack)
			{
				ItemJetpack jetpack = (ItemJetpack)stack.getItem();

				if(jetpack.getGas(stack) != null)
				{
					if((Mekanism.keyMap.has(player, KeySync.ASCEND) && jetpack.getMode(stack) == JetpackMode.NORMAL))
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
}
