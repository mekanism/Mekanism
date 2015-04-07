package mekanism.generators.common.tile.reactor;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.client;
import mekanism.api.Pos3D;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.client.SparkleAnimation.INodeChecker;
import mekanism.client.sound.ISoundSource;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IHasSound;
import mekanism.common.base.SoundWrapper;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.FusionReactor;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityReactorController extends TileEntityReactorBlock implements IActiveState, IHasSound, ISoundSource
{
	public static final int MAX_WATER = 100 * FluidContainerRegistry.BUCKET_VOLUME;

	public static final int MAX_FUEL = 1 * FluidContainerRegistry.BUCKET_VOLUME;

	public FluidTank waterTank = new FluidTank(MAX_WATER);
	public FluidTank steamTank = new FluidTank(MAX_WATER*1000);

	public GasTank deuteriumTank = new GasTank(MAX_FUEL);
	public GasTank tritiumTank = new GasTank(MAX_FUEL);

	public GasTank fuelTank = new GasTank(MAX_FUEL);

	public AxisAlignedBB box;
	
	public ResourceLocation soundURL = new ResourceLocation("mekanism", "tile.machine.fusionreactor");
	
	@SideOnly(Side.CLIENT)
	public SoundWrapper sound;

	public double clientTemp = 0;
	public boolean clientBurning = false;

	public TileEntityReactorController()
	{
		super("ReactorController", 1000000000);
		inventory = new ItemStack[1];
	}

	@Override
	public boolean isFrame()
	{
		return false;
	}

	public void radiateNeutrons(int neutrons) {} //future impl

	public void formMultiblock()
	{
		if(getReactor() == null)
		{
			setReactor(new FusionReactor(this));
		}
		
		getReactor().formMultiblock();
	}

	public double getPlasmaTemp()
	{
		if(getReactor() == null || !getReactor().isFormed())
		{
			return 0;
		}
		
		return getReactor().getPlasmaTemp();
	}

	public double getCaseTemp()
	{
		if(getReactor() == null || !getReactor().isFormed())
		{
			return 0;
		}
		
		return getReactor().getCaseTemp();
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(worldObj.isRemote)
		{
			updateSound();
		}

		if(isFormed())
		{
			getReactor().simulate();
			
			if(!worldObj.isRemote && (getReactor().isBurning() != clientBurning || Math.abs(getReactor().getPlasmaTemp() - clientTemp) > 1000000))
			{
				Mekanism.packetHandler.sendToAllAround(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), Coord4D.get(this).getTargetPoint(50D));
				clientBurning = getReactor().isBurning();
				clientTemp = getReactor().getPlasmaTemp();
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public void updateSound()
	{
		if(shouldPlaySound() && getSound().canRestart() && client.enableMachineSounds)
		{
			getSound().reset();
			getSound().play();
		}
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		
		formMultiblock();
	}
	
	@Override
	public void onAdded()
	{
		super.onAdded();
		
		formMultiblock();
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);

		tag.setBoolean("formed", isFormed());

		if(isFormed())
		{
			tag.setDouble("plasmaTemp", getReactor().getPlasmaTemp());
			tag.setDouble("caseTemp", getReactor().getCaseTemp());
			tag.setInteger("injectionRate", getReactor().getInjectionRate());
			tag.setBoolean("burning", getReactor().isBurning());
		}
		else {
			tag.setDouble("plasmaTemp", 0);
			tag.setDouble("caseTemp", 0);
			tag.setInteger("injectionRate", 0);
			tag.setBoolean("burning", false);
		}

		tag.setTag("fuelTank", fuelTank.write(new NBTTagCompound()));
		tag.setTag("deuteriumTank", deuteriumTank.write(new NBTTagCompound()));
		tag.setTag("tritiumTank", tritiumTank.write(new NBTTagCompound()));
		tag.setTag("waterTank", waterTank.writeToNBT(new NBTTagCompound()));
		tag.setTag("steamTank", steamTank.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);

		boolean formed = tag.getBoolean("formed");

		if(formed)
		{
			setReactor(new FusionReactor(this));
			getReactor().setPlasmaTemp(tag.getDouble("plasmaTemp"));
			getReactor().setCaseTemp(tag.getDouble("caseTemp"));
			getReactor().setInjectionRate(tag.getInteger("injectionRate"));
			getReactor().setBurning(tag.getBoolean("burning"));
			getReactor().updateTemperatures();
		}

		fuelTank.read(tag.getCompoundTag("fuelTank"));
		deuteriumTank.read(tag.getCompoundTag("deuteriumTank"));
		tritiumTank.read(tag.getCompoundTag("tritiumTank"));
		waterTank.readFromNBT(tag.getCompoundTag("waterTank"));
		steamTank.readFromNBT(tag.getCompoundTag("steamTank"));
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(getReactor() != null && getReactor().isFormed());
		
		if(getReactor() != null)
		{
			data.add(getReactor().getPlasmaTemp());
			data.add(getReactor().getCaseTemp());
			data.add(getReactor().getInjectionRate());
			data.add(getReactor().isBurning());
			data.add(fuelTank.getStored());
			data.add(deuteriumTank.getStored());
			data.add(tritiumTank.getStored());
			data.add(waterTank.getFluidAmount());
			data.add(steamTank.getFluidAmount());
		}

		return data;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(!worldObj.isRemote)
		{
			int type = dataStream.readInt();

			switch(type)
			{
				case 0:
					if(getReactor() != null) getReactor().setInjectionRate(dataStream.readInt());
					break;
			}

			return;
		}

		super.handlePacketData(dataStream);

		boolean formed = dataStream.readBoolean();
		
		if(formed)
		{
			if(getReactor() == null || !((FusionReactor)getReactor()).formed)
			{
				Mekanism.proxy.doGenericSparkle(this, new INodeChecker() {
					@Override
					public boolean isNode(TileEntity tile)
					{
						return tile instanceof TileEntityReactorBlock;
					}
				});
			}
			
			if(getReactor() == null)
			{
				setReactor(new FusionReactor(this));
				MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
			}
			
			((FusionReactor)getReactor()).formed = true;
			getReactor().setPlasmaTemp(dataStream.readDouble());
			getReactor().setCaseTemp(dataStream.readDouble());
			getReactor().setInjectionRate(dataStream.readInt());
			getReactor().setBurning(dataStream.readBoolean());
			fuelTank.setGas(new GasStack(GasRegistry.getGas("fusionFuelDT"), dataStream.readInt()));
			deuteriumTank.setGas(new GasStack(GasRegistry.getGas("deuterium"), dataStream.readInt()));
			tritiumTank.setGas(new GasStack(GasRegistry.getGas("tritium"), dataStream.readInt()));
			waterTank.setFluid(new FluidStack(FluidRegistry.getFluid("water"), dataStream.readInt()));
			steamTank.setFluid(new FluidStack(FluidRegistry.getFluid("steam"), dataStream.readInt()));
		}
		else if(getReactor() != null)
		{
			setReactor(null);
			MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
		}
	}

	public boolean isFormed()
	{
		return getReactor() != null && getReactor().isFormed();
	}

	public boolean isBurning()
	{
		return getActive() && getReactor().isBurning();
	}

	@Override
	public boolean getActive()
	{
		return isFormed();
	}

	@Override
	public void setActive(boolean active)
	{
		if(active == (getReactor() == null))
		{
			setReactor(active ? new FusionReactor(this) : null);
		}
	}

	@Override
	public boolean renderUpdate()
	{
		return true;
	}

	@Override
	public boolean lightUpdate()
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(box == null)
		{
			box = AxisAlignedBB.getBoundingBox(xCoord-1, yCoord-3, zCoord-1, xCoord+2, yCoord, zCoord+2);
		}
		
		return box;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public SoundWrapper getSound()
	{
		return sound;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldPlaySound()
	{
		return isBurning() && !isInvalid();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ResourceLocation getSoundLocation()
	{
		return soundURL;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getVolume()
	{
		return 2F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getPitch()
	{
		return 1F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Pos3D getSoundPosition()
	{
		return new Pos3D(xCoord+0.5, yCoord+0.5, zCoord+0.5);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldRepeat()
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRepeatDelay()
	{
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AttenuationType getAttenuation()
	{
		return AttenuationType.LINEAR;
	}

	@Override
	public void validate()
	{
		super.validate();

		if(worldObj.isRemote)
		{
			initSounds();
		}
	}

	@SideOnly(Side.CLIENT)
	public void initSounds()
	{
		sound = new SoundWrapper(this, this);
	}
}
