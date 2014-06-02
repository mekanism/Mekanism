package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.IModule;
import mekanism.common.Mekanism;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PacketConfigSync extends MekanismPacket
{
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
		Mekanism.cardboardSpawners = dataStream.readBoolean();
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
		Mekanism.FROM_H2 = dataStream.readDouble();
		Mekanism.ENERGY_PER_REDSTONE = dataStream.readDouble();
		Mekanism.VOICE_PORT = dataStream.readInt();
		Mekanism.maxUpgradeMultiplier = dataStream.readInt();

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
		Mekanism.oxidationChamberUsage = dataStream.readDouble();
		Mekanism.chemicalInfuserUsage = dataStream.readDouble();
		Mekanism.chemicalInjectionChamberUsage = dataStream.readDouble();
		Mekanism.precisionSawmillUsage = dataStream.readDouble();
		Mekanism.chemicalDissolutionChamberUsage = dataStream.readDouble();
		Mekanism.chemicalWasherUsage = dataStream.readDouble();
		Mekanism.chemicalCrystallizerUsage = dataStream.readDouble();
		Mekanism.seismicVibratorUsage = dataStream.readDouble();

		for(IModule module : Mekanism.modulesLoaded)
		{
			module.readConfig(dataStream);
		}

		Mekanism.proxy.onConfigSync();
	}

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
		dataStream.writeBoolean(Mekanism.cardboardSpawners);
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
		dataStream.writeDouble(Mekanism.FROM_H2);
		dataStream.writeDouble(Mekanism.ENERGY_PER_REDSTONE);
		dataStream.writeInt(Mekanism.VOICE_PORT);
		dataStream.writeInt(Mekanism.maxUpgradeMultiplier);

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
		dataStream.writeDouble(Mekanism.oxidationChamberUsage);
		dataStream.writeDouble(Mekanism.chemicalInfuserUsage);
		dataStream.writeDouble(Mekanism.chemicalInjectionChamberUsage);
		dataStream.writeDouble(Mekanism.precisionSawmillUsage);
		dataStream.writeDouble(Mekanism.chemicalDissolutionChamberUsage);
		dataStream.writeDouble(Mekanism.chemicalWasherUsage);
		dataStream.writeDouble(Mekanism.chemicalCrystallizerUsage);
		dataStream.writeDouble(Mekanism.seismicVibratorUsage);

		for(IModule module : Mekanism.modulesLoaded)
		{
			module.writeConfig(dataStream);
		}
	}

	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf buffer)
	{

	}

	@Override
	public void read(ChannelHandlerContext ctx, ByteBuf buffer)
	{

	}

	@Override
	public void handleClientSide(EntityPlayer player)
	{

	}

	@Override
	public void handleServerSide(EntityPlayer player)
	{

	}
}
