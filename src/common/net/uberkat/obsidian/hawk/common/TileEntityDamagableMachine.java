package net.uberkat.obsidian.hawk.common;

import hawk.api.ProcessingRecipes.EnumProcessing;

import java.util.Random;
import com.google.common.io.ByteArrayDataInput;
import universalelectricity.electricity.ElectricInfo;
import universalelectricity.implement.IRotatable;
import universalelectricity.prefab.TileEntityElectricityReceiver;
import universalelectricity.prefab.network.IPacketReceiver;
import universalelectricity.prefab.network.PacketManager;
import universalelectricity.core.Vector3;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import net.uberkat.obsidian.common.TileEntityBasicMachine;

/**
 * 
 * Extend this if you'd like to make a machine slightly faster.
 * 
 * @author Elusivehawk
 */
public abstract class TileEntityDamagableMachine extends TileEntityBasicMachine
{
	public EnumProcessing machineEnum;
	
	protected boolean isProcessor;
	
	public TileEntityDamagableMachine(String soundPath, String name, String path, int perTick, int ticksRequired, int maxEnergy)
	{
		super(soundPath, name, path, perTick, ticksRequired, maxEnergy);
	}
	
	protected void explodeMachine(float strength)
	{
		worldObj.createExplosion((Entity)null, xCoord, yCoord, zCoord, strength, true);
	}
	
	public void handlePacketData(INetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		
	}
}
