package mekanism.common.item;

import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;

import java.util.List;

import cofh.api.energy.IEnergyContainerItem;
import mekanism.api.EnumColor;
import mekanism.api.energy.IEnergizedItem;
import mekanism.client.render.ModelCustomArmor;
import mekanism.client.render.ModelCustomArmor.ArmorModel;
import mekanism.common.Mekanism;
import mekanism.common.integration.IC2ItemManager;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumHelper;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemFreeRunners extends ItemArmor implements IEnergizedItem, ISpecialElectricItem, IEnergyContainerItem
{
	/** The maximum amount of energy this item can hold. */
	public double MAX_ELECTRICITY = 64000;

	public ItemFreeRunners(int id)
	{
		super(id, EnumHelper.addArmorMaterial("FRICTIONBOOTS", 0, new int[] {0, 0, 0, 0}, 0), 0, 3);
		setMaxStackSize(1);
		setMaxDamage(100);
		setNoRepair();
		setCreativeTab(Mekanism.tabMekanism);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {}

	@Override
	public boolean isValidArmor(ItemStack stack, int armorType, Entity entity)
	{
		return armorType == 3;
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type)
	{
		return "mekanism:render/NullArmor.png";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, int armorSlot)
	{
		ModelCustomArmor model = ModelCustomArmor.INSTANCE;
		model.modelType = ArmorModel.FREERUNNERS;
		return model;
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		list.add(EnumColor.AQUA + "Stored Energy: " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(getEnergy(itemstack)));
	}

	public ItemStack getUnchargedItem()
	{
		ItemStack charged = new ItemStack(this);
		charged.setItemDamage(100);
		return charged;
	}

	@Override
	public void getSubItems(int i, CreativeTabs tabs, List list)
	{
		ItemStack discharged = new ItemStack(this);
		discharged.setItemDamage(100);
		list.add(discharged);
		ItemStack charged = new ItemStack(this);
		setEnergy(charged, ((IEnergizedItem)charged.getItem()).getMaxEnergy(charged));
		list.add(charged);
	}

	@Override
	public boolean canProvideEnergy(ItemStack itemStack)
	{
		return canSend(itemStack);
	}

	@Override
	public int getChargedItemId(ItemStack itemStack)
	{
		return itemID;
	}

	@Override
	public int getEmptyItemId(ItemStack itemStack)
	{
		return itemID;
	}

	@Override
	public int getMaxCharge(ItemStack itemStack)
	{
		return 0;
	}

	@Override
	public int getTier(ItemStack itemStack)
	{
		return 4;
	}

	@Override
	public int getTransferLimit(ItemStack itemStack)
	{
		return 0;
	}

	@Override
	public double getEnergy(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			return 0;
		}

		double electricityStored = itemStack.stackTagCompound.getDouble("electricity");
		itemStack.setItemDamage((int)Math.max(1, (Math.abs(((electricityStored/getMaxEnergy(itemStack))*100)-100))));

		return electricityStored;
	}

	@Override
	public void setEnergy(ItemStack itemStack, double amount)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		double electricityStored = Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0);
		itemStack.stackTagCompound.setDouble("electricity", electricityStored);
		itemStack.setItemDamage((int)Math.max(1, (Math.abs(((electricityStored/getMaxEnergy(itemStack))*100)-100))));
	}

	@Override
	public double getMaxEnergy(ItemStack itemStack)
	{
		return MAX_ELECTRICITY;
	}

	@Override
	public double getMaxTransfer(ItemStack itemStack)
	{
		return getMaxEnergy(itemStack)*0.005;
	}

	@Override
	public boolean canReceive(ItemStack itemStack)
	{
		return getMaxEnergy(itemStack)-getEnergy(itemStack) > 0;
	}

	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return false;
	}

	@Override
	public int receiveEnergy(ItemStack theItem, int energy, boolean simulate)
	{
		if(canReceive(theItem))
		{
			double energyNeeded = getMaxEnergy(theItem)-getEnergy(theItem);
			double toReceive = Math.min(energy*Mekanism.FROM_TE, energyNeeded);

			if(!simulate)
			{
				setEnergy(theItem, getEnergy(theItem) + toReceive);
			}

			return (int)Math.round(toReceive*Mekanism.TO_TE);
		}

		return 0;
	}

	@Override
	public int extractEnergy(ItemStack theItem, int energy, boolean simulate)
	{
		if(canSend(theItem))
		{
			double energyRemaining = getEnergy(theItem);
			double toSend = Math.min((energy*Mekanism.FROM_TE), energyRemaining);

			if(!simulate)
			{
				setEnergy(theItem, getEnergy(theItem) - toSend);
			}

			return (int)Math.round(toSend*Mekanism.TO_TE);
		}

		return 0;
	}

	@Override
	public int getEnergyStored(ItemStack theItem)
	{
		return (int)Math.round(getEnergy(theItem)*Mekanism.TO_TE);
	}

	@Override
	public int getMaxEnergyStored(ItemStack theItem)
	{
		return (int)Math.round(getMaxEnergy(theItem)*Mekanism.TO_TE);
	}

	@Override
	public boolean isMetadataSpecific()
	{
		return false;
	}

	@Override
	public IElectricItemManager getManager(ItemStack itemStack)
	{
		return IC2ItemManager.getManager(this);
	}

	@SubscribeEvent
	public void onEntityAttacked(LivingAttackEvent event)
	{
		EntityLivingBase base = event.entityLiving;

		if(base.getCurrentItemOrArmor(1) != null && base.getCurrentItemOrArmor(1).getItem() instanceof ItemFreeRunners)
		{
			ItemStack stack = base.getCurrentItemOrArmor(1);
			ItemFreeRunners boots = (ItemFreeRunners)stack.getItem();

			if(boots.getEnergy(stack) > 0 && event.source == DamageSource.fall)
			{
				boots.setEnergy(stack, boots.getEnergy(stack)-event.ammount*50);
				event.setCanceled(true);
			}
		}
	}
}
