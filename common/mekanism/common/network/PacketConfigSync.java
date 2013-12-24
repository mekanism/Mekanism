package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.Mekanism;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;

import com.google.common.io.ByteArrayDataInput;

public class PacketConfigSync implements IMekanismPacket
{
	@Override
	public String getName() 
	{
		return "ConfigSync";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
	  	Mekanism.osmiumGenerationEnabled = dataStream.readBoolean();
	  	Mekanism.copperGenerationEnabled = dataStream.readBoolean();
	  	Mekanism.tinGenerationEnabled = dataStream.readBoolean();
	  	Mekanism.disableBCSteelCrafting = dataStream.readBoolean();
	  	Mekanism.disableBCBronzeCrafting = dataStream.readBoolean();
	  	Mekanism.updateNotifications = dataStream.readBoolean();
	  	Mekanism.controlCircuitOreDict = dataStream.readBoolean();
	  	Mekanism.logPackets = dataStream.readBoolean();
	  	Mekanism.dynamicTankEasterEgg = dataStream.readBoolean();
	  	Mekanism.voiceServerEnabled = dataStream.readBoolean();
	  	Mekanism.forceBuildcraft = dataStream.readBoolean();
	  	Mekanism.obsidianTNTDelay = dataStream.readInt();
	  	Mekanism.obsidianTNTBlastRadius = dataStream.readInt();
	  	Mekanism.UPDATE_DELAY = dataStream.readInt();
	  	Mekanism.osmiumGenerationAmount = dataStream.readInt();
	  	Mekanism.copperGenerationAmount = dataStream.readInt();
	  	Mekanism.tinGenerationAmount = dataStream.readInt();
	  	Mekanism.FROM_IC2 = dataStream.readDouble();
	  	Mekanism.TO_IC2 = dataStream.readDouble();
	  	Mekanism.FROM_BC = dataStream.readDouble();
	  	Mekanism.TO_BC = dataStream.readDouble();
	  	Mekanism.ENERGY_PER_REDSTONE = dataStream.readDouble();
	  	Mekanism.VOICE_PORT = dataStream.readInt();
	  	Mekanism.upgradeModifier = dataStream.readInt();
	  	
	  	Mekanism.TO_TE = Mekanism.TO_BC*10;
	  	Mekanism.FROM_TE = Mekanism.FROM_BC/10;
	  	
		Mekanism.enrichmentChamberUsage = dataStream.readDouble();
		Mekanism.osmiumCompressorUsage = dataStream.readDouble();
		Mekanism.combinerUsage = dataStream.readDouble();
		Mekanism.crusherUsage = dataStream.readDouble();
		Mekanism.factoryUsage = dataStream.readDouble();
		Mekanism.metallurgicInfuserUsage = dataStream.readDouble();
		Mekanism.purificationChamberUsage = dataStream.readDouble();
		Mekanism.energizedSmelterUsage = dataStream.readDouble();
		Mekanism.digitalMinerUsage = dataStream.readDouble();
		Mekanism.rotaryCondensentratorUsage = dataStream.readDouble();
		Mekanism.chemicalFormulatorUsage = dataStream.readDouble();
		Mekanism.chemicalInfuserUsage = dataStream.readDouble();
		Mekanism.chemicalInjectionChamberUsage = dataStream.readDouble();

		Mekanism.proxy.onConfigSync();
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{	  	
		dataStream.writeBoolean(Mekanism.osmiumGenerationEnabled);
	  	dataStream.writeBoolean(Mekanism.copperGenerationEnabled);
	  	dataStream.writeBoolean(Mekanism.tinGenerationEnabled);
	  	dataStream.writeBoolean(Mekanism.disableBCSteelCrafting);
	  	dataStream.writeBoolean(Mekanism.disableBCBronzeCrafting);
	  	dataStream.writeBoolean(Mekanism.updateNotifications);
	  	dataStream.writeBoolean(Mekanism.controlCircuitOreDict);
	  	dataStream.writeBoolean(Mekanism.logPackets);
	  	dataStream.writeBoolean(Mekanism.dynamicTankEasterEgg);
	  	dataStream.writeBoolean(Mekanism.voiceServerEnabled);
	  	dataStream.writeBoolean(Mekanism.forceBuildcraft);
	  	dataStream.writeInt(Mekanism.obsidianTNTDelay);
	  	dataStream.writeInt(Mekanism.obsidianTNTBlastRadius);
	  	dataStream.writeInt(Mekanism.UPDATE_DELAY);
	  	dataStream.writeInt(Mekanism.osmiumGenerationAmount);
	  	dataStream.writeInt(Mekanism.copperGenerationAmount);
	  	dataStream.writeInt(Mekanism.tinGenerationAmount);
	  	dataStream.writeDouble(Mekanism.FROM_IC2);
	  	dataStream.writeDouble(Mekanism.TO_IC2);
	  	dataStream.writeDouble(Mekanism.FROM_BC);
	  	dataStream.writeDouble(Mekanism.TO_BC);
	  	dataStream.writeDouble(Mekanism.ENERGY_PER_REDSTONE);
	  	dataStream.writeInt(Mekanism.VOICE_PORT);
	  	dataStream.writeInt(Mekanism.upgradeModifier);
	  	
	  	dataStream.writeDouble(Mekanism.enrichmentChamberUsage);
		dataStream.writeDouble(Mekanism.osmiumCompressorUsage);
		dataStream.writeDouble(Mekanism.combinerUsage);
		dataStream.writeDouble(Mekanism.crusherUsage);
		dataStream.writeDouble(Mekanism.factoryUsage);
		dataStream.writeDouble(Mekanism.metallurgicInfuserUsage);
		dataStream.writeDouble(Mekanism.purificationChamberUsage);
		dataStream.writeDouble(Mekanism.energizedSmelterUsage);
		dataStream.writeDouble(Mekanism.digitalMinerUsage);
		dataStream.writeDouble(Mekanism.rotaryCondensentratorUsage);
		dataStream.writeDouble(Mekanism.chemicalFormulatorUsage);
		dataStream.writeDouble(Mekanism.chemicalInfuserUsage);
		dataStream.writeDouble(Mekanism.chemicalInjectionChamberUsage);
	}
}
