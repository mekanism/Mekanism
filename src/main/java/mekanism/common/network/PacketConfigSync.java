package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.api.MekanismConfig.general;
import mekanism.api.MekanismConfig.usage;
import mekanism.api.util.EnergyUtils.EnergyType;
import mekanism.common.Mekanism;
import mekanism.common.base.IModule;
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
			dataStream.writeBoolean(general.updateNotifications);
			dataStream.writeBoolean(general.controlCircuitOreDict);
			dataStream.writeBoolean(general.logPackets);
			dataStream.writeBoolean(general.dynamicTankEasterEgg);
			dataStream.writeBoolean(general.voiceServerEnabled);
			dataStream.writeBoolean(general.cardboardSpawners);
			dataStream.writeBoolean(general.creativeOverrideElectricChest);
			dataStream.writeBoolean(general.spawnBabySkeletons);
			dataStream.writeInt(general.obsidianTNTDelay);
			dataStream.writeInt(general.obsidianTNTBlastRadius);
			dataStream.writeInt(general.UPDATE_DELAY);
			dataStream.writeDouble(general.FROM_IC2);
			dataStream.writeDouble(general.TO_IC2);
			dataStream.writeDouble(general.FROM_BC);
			dataStream.writeDouble(general.TO_BC);
			dataStream.writeDouble(general.FROM_H2);
			dataStream.writeDouble(general.ENERGY_PER_REDSTONE);
			dataStream.writeInt(general.VOICE_PORT);
			dataStream.writeInt(general.maxUpgradeMultiplier);
			dataStream.writeInt(general.activeType.ordinal());
	
			dataStream.writeDouble(usage.enrichmentChamberUsage);
			dataStream.writeDouble(usage.osmiumCompressorUsage);
			dataStream.writeDouble(usage.combinerUsage);
			dataStream.writeDouble(usage.crusherUsage);
			dataStream.writeDouble(usage.factoryUsage);
			dataStream.writeDouble(usage.metallurgicInfuserUsage);
			dataStream.writeDouble(usage.purificationChamberUsage);
			dataStream.writeDouble(usage.energizedSmelterUsage);
			dataStream.writeDouble(usage.digitalMinerUsage);
			dataStream.writeDouble(usage.electricPumpUsage);
			dataStream.writeDouble(usage.rotaryCondensentratorUsage);
			dataStream.writeDouble(usage.oxidationChamberUsage);
			dataStream.writeDouble(usage.chemicalInfuserUsage);
			dataStream.writeDouble(usage.chemicalInjectionChamberUsage);
			dataStream.writeDouble(usage.precisionSawmillUsage);
			dataStream.writeDouble(usage.chemicalDissolutionChamberUsage);
			dataStream.writeDouble(usage.chemicalWasherUsage);
			dataStream.writeDouble(usage.chemicalCrystallizerUsage);
			dataStream.writeDouble(usage.seismicVibratorUsage);
			dataStream.writeDouble(usage.fluidicPlenisherUsage);
	
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
			general.updateNotifications = dataStream.readBoolean();
			general.controlCircuitOreDict = dataStream.readBoolean();
			general.logPackets = dataStream.readBoolean();
			general.dynamicTankEasterEgg = dataStream.readBoolean();
			general.voiceServerEnabled = dataStream.readBoolean();
			general.cardboardSpawners = dataStream.readBoolean();
			general.creativeOverrideElectricChest = dataStream.readBoolean();
			general.spawnBabySkeletons = dataStream.readBoolean();
			general.obsidianTNTDelay = dataStream.readInt();
			general.obsidianTNTBlastRadius = dataStream.readInt();
			general.UPDATE_DELAY = dataStream.readInt();
			general.FROM_IC2 = dataStream.readDouble();
			general.TO_IC2 = dataStream.readDouble();
			general.FROM_BC = dataStream.readDouble();
			general.TO_BC = dataStream.readDouble();
			general.FROM_H2 = dataStream.readDouble();
			general.ENERGY_PER_REDSTONE = dataStream.readDouble();
			general.VOICE_PORT = dataStream.readInt();
			general.maxUpgradeMultiplier = dataStream.readInt();
			general.activeType = EnergyType.values()[dataStream.readInt()];
	
			general.TO_TE = general.TO_BC*10;
			general.FROM_TE = general.FROM_BC/10;
	
			usage.enrichmentChamberUsage = dataStream.readDouble();
			usage.osmiumCompressorUsage = dataStream.readDouble();
			usage.combinerUsage = dataStream.readDouble();
			usage.crusherUsage = dataStream.readDouble();
			usage.factoryUsage = dataStream.readDouble();
			usage.metallurgicInfuserUsage = dataStream.readDouble();
			usage.purificationChamberUsage = dataStream.readDouble();
			usage.energizedSmelterUsage = dataStream.readDouble();
			usage.digitalMinerUsage = dataStream.readDouble();
			usage.electricPumpUsage = dataStream.readDouble();
			usage.rotaryCondensentratorUsage = dataStream.readDouble();
			usage.oxidationChamberUsage = dataStream.readDouble();
			usage.chemicalInfuserUsage = dataStream.readDouble();
			usage.chemicalInjectionChamberUsage = dataStream.readDouble();
			usage.precisionSawmillUsage = dataStream.readDouble();
			usage.chemicalDissolutionChamberUsage = dataStream.readDouble();
			usage.chemicalWasherUsage = dataStream.readDouble();
			usage.chemicalCrystallizerUsage = dataStream.readDouble();
			usage.seismicVibratorUsage = dataStream.readDouble();
			usage.fluidicPlenisherUsage = dataStream.readDouble();
	
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
