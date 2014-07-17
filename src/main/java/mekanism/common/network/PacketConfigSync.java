package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.common.EnergyDisplay.EnergyType;
import mekanism.common.IModule;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketConfigSync.ConfigSyncMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketConfigSync implements IMessageHandler<ConfigSyncMessage, IMessage>
{
	@Override
	public IMessage onMessage(ConfigSyncMessage message, MessageContext context) 
	{
		return null;
	}
	
	public static class ConfigSyncMessage implements IMessage
	{
		public ConfigSyncMessage() {}
		
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeBoolean(Mekanism.updateNotifications);
			dataStream.writeBoolean(Mekanism.controlCircuitOreDict);
			dataStream.writeBoolean(Mekanism.logPackets);
			dataStream.writeBoolean(Mekanism.dynamicTankEasterEgg);
			dataStream.writeBoolean(Mekanism.voiceServerEnabled);
			dataStream.writeBoolean(Mekanism.cardboardSpawners);
			dataStream.writeBoolean(Mekanism.creativeOverrideElectricChest);
			dataStream.writeInt(Mekanism.obsidianTNTDelay);
			dataStream.writeInt(Mekanism.obsidianTNTBlastRadius);
			dataStream.writeInt(Mekanism.UPDATE_DELAY);
			dataStream.writeDouble(Mekanism.FROM_IC2);
			dataStream.writeDouble(Mekanism.TO_IC2);
			dataStream.writeDouble(Mekanism.FROM_BC);
			dataStream.writeDouble(Mekanism.TO_BC);
			dataStream.writeDouble(Mekanism.FROM_H2);
			dataStream.writeDouble(Mekanism.ENERGY_PER_REDSTONE);
			dataStream.writeInt(Mekanism.VOICE_PORT);
			dataStream.writeInt(Mekanism.maxUpgradeMultiplier);
			dataStream.writeInt(Mekanism.activeType.ordinal());
	
			dataStream.writeDouble(Mekanism.enrichmentChamberUsage);
			dataStream.writeDouble(Mekanism.osmiumCompressorUsage);
			dataStream.writeDouble(Mekanism.combinerUsage);
			dataStream.writeDouble(Mekanism.crusherUsage);
			dataStream.writeDouble(Mekanism.factoryUsage);
			dataStream.writeDouble(Mekanism.metallurgicInfuserUsage);
			dataStream.writeDouble(Mekanism.purificationChamberUsage);
			dataStream.writeDouble(Mekanism.energizedSmelterUsage);
			dataStream.writeDouble(Mekanism.digitalMinerUsage);
			dataStream.writeDouble(Mekanism.electricPumpUsage);
			dataStream.writeDouble(Mekanism.rotaryCondensentratorUsage);
			dataStream.writeDouble(Mekanism.oxidationChamberUsage);
			dataStream.writeDouble(Mekanism.chemicalInfuserUsage);
			dataStream.writeDouble(Mekanism.chemicalInjectionChamberUsage);
			dataStream.writeDouble(Mekanism.precisionSawmillUsage);
			dataStream.writeDouble(Mekanism.chemicalDissolutionChamberUsage);
			dataStream.writeDouble(Mekanism.chemicalWasherUsage);
			dataStream.writeDouble(Mekanism.chemicalCrystallizerUsage);
			dataStream.writeDouble(Mekanism.seismicVibratorUsage);
			dataStream.writeDouble(Mekanism.fluidicPlenisherUsage);
	
			try {
				for(IModule module : Mekanism.modulesLoaded)
				{
					module.writeConfig(dataStream);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			Mekanism.updateNotifications = dataStream.readBoolean();
			Mekanism.controlCircuitOreDict = dataStream.readBoolean();
			Mekanism.logPackets = dataStream.readBoolean();
			Mekanism.dynamicTankEasterEgg = dataStream.readBoolean();
			Mekanism.voiceServerEnabled = dataStream.readBoolean();
			Mekanism.cardboardSpawners = dataStream.readBoolean();
			Mekanism.creativeOverrideElectricChest = dataStream.readBoolean();
			Mekanism.obsidianTNTDelay = dataStream.readInt();
			Mekanism.obsidianTNTBlastRadius = dataStream.readInt();
			Mekanism.UPDATE_DELAY = dataStream.readInt();
			Mekanism.FROM_IC2 = dataStream.readDouble();
			Mekanism.TO_IC2 = dataStream.readDouble();
			Mekanism.FROM_BC = dataStream.readDouble();
			Mekanism.TO_BC = dataStream.readDouble();
			Mekanism.FROM_H2 = dataStream.readDouble();
			Mekanism.ENERGY_PER_REDSTONE = dataStream.readDouble();
			Mekanism.VOICE_PORT = dataStream.readInt();
			Mekanism.maxUpgradeMultiplier = dataStream.readInt();
			Mekanism.activeType = EnergyType.values()[dataStream.readInt()];
	
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
			Mekanism.electricPumpUsage = dataStream.readDouble();
			Mekanism.rotaryCondensentratorUsage = dataStream.readDouble();
			Mekanism.oxidationChamberUsage = dataStream.readDouble();
			Mekanism.chemicalInfuserUsage = dataStream.readDouble();
			Mekanism.chemicalInjectionChamberUsage = dataStream.readDouble();
			Mekanism.precisionSawmillUsage = dataStream.readDouble();
			Mekanism.chemicalDissolutionChamberUsage = dataStream.readDouble();
			Mekanism.chemicalWasherUsage = dataStream.readDouble();
			Mekanism.chemicalCrystallizerUsage = dataStream.readDouble();
			Mekanism.seismicVibratorUsage = dataStream.readDouble();
			Mekanism.fluidicPlenisherUsage = dataStream.readDouble();
	
			try {
				for(IModule module : Mekanism.modulesLoaded)
				{
					module.readConfig(dataStream);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
	
			Mekanism.proxy.onConfigSync();
		}
	}
}
