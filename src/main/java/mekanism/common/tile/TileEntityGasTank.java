package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.ITubeConnection;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.SideData;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.GasTankTier;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.base.TileNetworkList;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.GasUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityGasTank extends TileEntityContainerBlock implements IGasHandler, ITubeConnection, IRedstoneControl, ISideConfiguration, ISecurityTile, ITierUpgradeable, IComputerIntegration
{
	public enum GasMode
	{
		IDLE,
		DUMPING_EXCESS,
		DUMPING
	}

	/** The type of gas stored in this tank. */
	public GasTank gasTank;
	
	public GasTankTier tier = GasTankTier.BASIC;

	public GasMode dumping;

	public int currentGasAmount;
	
	public int currentRedstoneLevel;

	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType;
	
	public TileComponentEjector ejectorComponent;
	public TileComponentConfig configComponent;
	public TileComponentSecurity securityComponent;
	private static final String[] methods = new String[] {"getMaxGas", "getStoredGas", "getGas"};

	public TileEntityGasTank()
	{
		super("GasTank");
		
		configComponent = new TileComponentConfig(this, TransmissionType.GAS, TransmissionType.ITEM);
		
		configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Fill", EnumColor.DARK_BLUE, new int[] {0}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Empty", EnumColor.DARK_RED, new int[] {1}));
		
		configComponent.setConfig(TransmissionType.ITEM, new byte[] {2, 1, 0, 0, 0, 0});
		configComponent.setCanEject(TransmissionType.ITEM, false);
		configComponent.setIOConfig(TransmissionType.GAS);
		configComponent.setEjecting(TransmissionType.GAS, true);
		
		gasTank = new GasTank(tier.storage);
		inventory = NonNullList.withSize(2, ItemStack.EMPTY);
		dumping = GasMode.IDLE;
		controlType = RedstoneControl.DISABLED;
		
		ejectorComponent = new TileComponentEjector(this);
		
		securityComponent = new TileComponentSecurity(this);
	}

	@Override
	public void onUpdate()
	{
		if(!world.isRemote)
		{
			if(!inventory.get(0).isEmpty() && gasTank.getGas() != null)
			{
				gasTank.draw(GasUtils.addGas(inventory.get(0), gasTank.getGas()), tier != GasTankTier.CREATIVE);
			}

			if(!inventory.get(1).isEmpty() && (gasTank.getGas() == null || gasTank.getGas().amount < gasTank.getMaxGas()))
			{
				gasTank.receive(GasUtils.removeGas(inventory.get(1), gasTank.getGasType(), gasTank.getNeeded()), true);
				
				if(tier == GasTankTier.CREATIVE && gasTank.getGas() != null)
				{
					gasTank.getGas().amount = Integer.MAX_VALUE;
				}
			}

			if(gasTank.getGas() != null && MekanismUtils.canFunction(this) && (tier == GasTankTier.CREATIVE || dumping != GasMode.DUMPING))
			{
				if(configComponent.isEjecting(TransmissionType.GAS))
				{
					GasStack toSend = new GasStack(gasTank.getGas().getGas(), Math.min(gasTank.getStored(), tier.output));
					gasTank.draw(GasUtils.emit(toSend, this, configComponent.getSidesForData(TransmissionType.GAS, facing, 2)), tier != GasTankTier.CREATIVE);
				}
			}

			if(tier != GasTankTier.CREATIVE)
			{
				if(dumping == GasMode.DUMPING)
				{
					gasTank.draw(tier.storage/400, true);
				}
		
				if(dumping == GasMode.DUMPING_EXCESS && gasTank.getNeeded() < tier.output)
				{
					gasTank.draw(tier.output-gasTank.getNeeded(), true);
				}
			}
			
			int newGasAmount = gasTank.getStored();
			
			if(newGasAmount != currentGasAmount)
			{
				MekanismUtils.saveChunk(this);
			}
			
			currentGasAmount = newGasAmount;
			
			int newRedstoneLevel = getRedstoneLevel();

			if(newRedstoneLevel != currentRedstoneLevel)
			{
				markDirty();
				currentRedstoneLevel = newRedstoneLevel;
			}
		}
	}
	
	@Override
	public boolean upgrade(BaseTier upgradeTier)
	{
		if(upgradeTier.ordinal() != tier.ordinal()+1)
		{
			return false;
		}
		
		tier = GasTankTier.values()[upgradeTier.ordinal()];
		gasTank.setMaxGas(tier.storage);
		
		Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())), new Range4D(Coord4D.get(this)));
		markDirty();
		
		return true;
	}
	
	@Override
	public String getName()
	{
		return LangUtils.localize("tile.GasTank" + tier.getBaseTier().getSimpleName() + ".name");
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, EnumFacing side)
	{
		if(slotID == 1)
		{
			return (itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).getGas(itemstack) == null);
		}
		else if(slotID == 0)
		{
			return (itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).getGas(itemstack) != null &&
			    ((IGasItem)itemstack.getItem()).getGas(itemstack).amount == ((IGasItem)itemstack.getItem()).getMaxGas(itemstack));
		}

		return false;
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return itemstack.getItem() instanceof IGasItem && (gasTank.getGas() == null || ((IGasItem)itemstack.getItem()).canReceiveGas(itemstack, gasTank.getGas().getGas()));
		}
		else if(slotID == 1)
		{
			return itemstack.getItem() instanceof IGasItem && (gasTank.getGas() == null || ((IGasItem)itemstack.getItem()).canProvideGas(itemstack, gasTank.getGas().getGas()));
		}

		return false;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side)
	{
		return configComponent.getOutput(TransmissionType.ITEM, side, facing).availableSlots;
	}

	@Override
	public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer)
	{
		if(tier == GasTankTier.CREATIVE)
		{
			return stack != null ? stack.amount : 0;
		}
		
		return gasTank.receive(stack, doTransfer);
	}

	@Override
	public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer)
	{
		return null;
	}

	@Override
	public boolean canDrawGas(EnumFacing side, Gas type)
	{
		return gasTank.canDraw(type);
	}

	@Override
	public boolean canReceiveGas(EnumFacing side, Gas type)
	{
		if(configComponent.hasSideForData(TransmissionType.GAS, facing, 1, side))
		{
			return gasTank.canReceive(type);
		}

		return false;
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing side)
	{
		return capability == Capabilities.GAS_HANDLER_CAPABILITY || capability == Capabilities.TUBE_CONNECTION_CAPABILITY 
				|| super.hasCapability(capability, side);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing side)
	{
		if(capability == Capabilities.GAS_HANDLER_CAPABILITY || capability == Capabilities.TUBE_CONNECTION_CAPABILITY)
		{
			return (T)this;
		}
		
		return super.getCapability(capability, side);
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			int type = dataStream.readInt();

			if(type == 0)
			{
				int index = (dumping.ordinal() + 1)%GasMode.values().length;
				dumping = GasMode.values()[index];
			}

			for(EntityPlayer player : playersUsing)
			{
				Mekanism.packetHandler.sendTo(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())), (EntityPlayerMP)player);
			}

			return;
		}

		super.handlePacketData(dataStream);
		
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			GasTankTier prevTier = tier;
			
			tier = GasTankTier.values()[dataStream.readInt()];
			gasTank.setMaxGas(tier.storage);
	
			if(dataStream.readBoolean())
			{
				gasTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
			}
			else {
				gasTank.setGas(null);
			}
	
			dumping = GasMode.values()[dataStream.readInt()];
			controlType = RedstoneControl.values()[dataStream.readInt()];
	
			if(prevTier != tier)
			{
				MekanismUtils.updateBlock(world, getPos());
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		tier = GasTankTier.values()[nbtTags.getInteger("tier")];
		gasTank.read(nbtTags.getCompoundTag("gasTank"));
		dumping = GasMode.values()[nbtTags.getInteger("dumping")];
		
		controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setInteger("tier", tier.ordinal());
		nbtTags.setTag("gasTank", gasTank.write(new NBTTagCompound()));
		nbtTags.setInteger("dumping", dumping.ordinal());
		nbtTags.setInteger("controlType", controlType.ordinal());
		
		return nbtTags;
	}

	@Override
	public TileNetworkList getNetworkedData(TileNetworkList data)
	{
		super.getNetworkedData(data);
		
		data.add(tier.ordinal());

		if(gasTank.getGas() != null)
		{
			data.add(true);
			data.add(gasTank.getGas().getGas().getID());
			data.add(gasTank.getStored());
		}
		else {
			data.add(false);
		}

		data.add(dumping.ordinal());
		data.add(controlType.ordinal());

		return data;
	}

	@Override
	public boolean canSetFacing(int side)
	{
		return side != 0 && side != 1;
	}

	@Override
	public boolean canTubeConnect(EnumFacing side)
	{
		return true;
	}

	public int getRedstoneLevel()
	{
        double fractionFull = (float)gasTank.getStored()/(float)gasTank.getMaxGas();
        return MathHelper.floor((float)(fractionFull * 14.0F)) + (fractionFull > 0 ? 1 : 0);
	}
	
	@Override
	public RedstoneControl getControlType()
	{
		return controlType;
	}

	@Override
	public void setControlType(RedstoneControl type)
	{
		controlType = type;
	}

	@Override
	public boolean canPulse()
	{
		return false;
	}

	@Override
	public TileComponentEjector getEjector()
	{
		return ejectorComponent;
	}
	
	@Override
	public TileComponentConfig getConfig()
	{
		return configComponent;
	}
	
	@Override
	public EnumFacing getOrientation()
	{
		return facing;
	}
	
	@Override
	public TileComponentSecurity getSecurity()
	{
		return securityComponent;
	}

	@Override
	public String[] getMethods()
	{
		return methods;
	}

	@Override
	public Object[] invoke(int method, Object[] arguments) throws Exception
	{
		switch(method)
		{
			case 0:
				return new Object[] {gasTank.getMaxGas()};
			case 1:
				return new Object[] {gasTank.getStored()};
			case 2:
				return new Object[] {gasTank.getGas()};
			default:
				throw new NoSuchMethodException();
		}
	}
}
