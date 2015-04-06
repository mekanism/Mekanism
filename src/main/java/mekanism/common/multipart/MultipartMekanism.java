package mekanism.common.multipart;

import mekanism.common.MekanismBlocks;
import mekanism.common.Tier;
import mekanism.common.block.BlockMachine.MachineType;

import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.event.FMLInterModComms;

import codechicken.microblock.BlockMicroMaterial;
import codechicken.microblock.MicroMaterialRegistry;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.MultipartGenerator;
import codechicken.multipart.TMultiPart;

import static mekanism.common.block.BlockMachine.MachineBlock.MACHINE_BLOCK_1;
import static mekanism.common.block.BlockMachine.MachineBlock.MACHINE_BLOCK_2;

public class MultipartMekanism implements IPartFactory
{
	public MultipartMekanism()
	{
		init();
	}

	public void init()
	{
		MultiPartRegistry.registerParts(this, new String[] {"mekanism:universal_cable_basic",
				"mekanism:universal_cable_advanced", "mekanism:universal_cable_elite",
				"mekanism:universal_cable_ultimate", "mekanism:mechanical_pipe",
				"mekanism:mechanical_pipe_basic", "mekanism:mechanical_pipe_advanced",
				"mekanism:mechanical_pipe_elite", "mekanism:mechanical_pipe_ultimate",
				"mekanism:pressurized_tube_basic", "mekanism:pressurized_tube_advanced",
				"mekanism:pressurized_tube_elite", "mekanism:pressurized_tube_ultimate",
				"mekanism:logistical_transporter_basic", "mekanism:logistical_transporter_advanced", 
				"mekanism:logistical_transporter_elite", "mekanism:logistical_transporter_ultimate", 
				"mekanism:restrictive_transporter", "mekanism:diversion_transporter", 
				"mekanism:heat_transmitter", "mekanism:glow_panel"});

		MultipartGenerator.registerPassThroughInterface("mekanism.api.IConfigurable");
		MultipartGenerator.registerPassThroughInterface("mekanism.api.energy.IStrictEnergyAcceptor");
		MultipartGenerator.registerPassThroughInterface("mekanism.api.gas.IGasHandler");
		MultipartGenerator.registerPassThroughInterface("mekanism.api.IHeatTransfer");
		MultipartGenerator.registerPassThroughInterface("mekanism.api.transmitters.IBlockableConnection");
		MultipartGenerator.registerPassThroughInterface("mekanism.api.transmitters.ITransmitterTile");
		MultipartGenerator.registerPassThroughInterface("mekanism.common.base.ITransporterTile");
		MultipartGenerator.registerPassThroughInterface("mekanism.common.base.ILogisticalTransporter");
		MultipartGenerator.registerPassThroughInterface("mekanism.common.base.ITileNetwork");
		MultipartGenerator.registerPassThroughInterface("cofh.api.energy.IEnergyHandler");

		registerMicroMaterials();
	}

	@Override
	public TMultiPart createPart(String name, boolean client)
	{
		if(name.equals("mekanism:universal_cable"))
		{
			return new PartUniversalCable(Tier.CableTier.BASIC);
		}
		else if(name.equals("mekanism:universal_cable_basic"))
		{
			return new PartUniversalCable(Tier.CableTier.BASIC);
		}
		else if(name.equals("mekanism:universal_cable_advanced"))
		{
			return new PartUniversalCable(Tier.CableTier.ADVANCED);
		}
		else if(name.equals("mekanism:universal_cable_elite"))
		{
			return new PartUniversalCable(Tier.CableTier.ELITE);
		}
		else if(name.equals("mekanism:universal_cable_ultimate"))
		{
			return new PartUniversalCable(Tier.CableTier.ULTIMATE);
		}
		else if(name.equals("mekanism:mechanical_pipe"))
		{
			return new PartMechanicalPipe(Tier.PipeTier.BASIC);
		}
		else if(name.equals("mekanism:mechanical_pipe_basic"))
		{
			return new PartMechanicalPipe(Tier.PipeTier.BASIC);
		}
		else if(name.equals("mekanism:mechanical_pipe_advanced"))
		{
			return new PartMechanicalPipe(Tier.PipeTier.ADVANCED);
		}
		else if(name.equals("mekanism:mechanical_pipe_elite"))
		{
			return new PartMechanicalPipe(Tier.PipeTier.ELITE);
		}
		else if(name.equals("mekanism:mechanical_pipe_ultimate"))
		{
			return new PartMechanicalPipe(Tier.PipeTier.ULTIMATE);
		}
		else if(name.equals("mekanism:pressurized_tube_basic") || name.equals("mekanism:pressurized_tube"))
		{
			return new PartPressurizedTube(Tier.TubeTier.BASIC);
		}
		else if(name.equals("mekanism:pressurized_tube_advanced"))
		{
			return new PartPressurizedTube(Tier.TubeTier.ADVANCED);
		}
		else if(name.equals("mekanism:pressurized_tube_elite"))
		{
			return new PartPressurizedTube(Tier.TubeTier.ELITE);
		}
		else if(name.equals("mekanism:pressurized_tube_ultimate"))
		{
			return new PartPressurizedTube(Tier.TubeTier.ULTIMATE);
		}
		else if(name.equals("mekanism:logistical_transporter_basic") || name.equals("mekanism:logistical_transporter"))
		{
			return new PartLogisticalTransporter(Tier.TransporterTier.BASIC);
		}
		else if(name.equals("mekanism:logistical_transporter_advanced"))
		{
			return new PartLogisticalTransporter(Tier.TransporterTier.ADVANCED);
		}
		else if(name.equals("mekanism:logistical_transporter_elite"))
		{
			return new PartLogisticalTransporter(Tier.TransporterTier.ELITE);
		}
		else if(name.equals("mekanism:logistical_transporter_ultimate"))
		{
			return new PartLogisticalTransporter(Tier.TransporterTier.ULTIMATE);
		}
		else if(name.equals("mekanism:restrictive_transporter"))
		{
			return new PartRestrictiveTransporter();
		}
		else if(name.equals("mekanism:diversion_transporter"))
		{
			return new PartDiversionTransporter();
		}
		else if(name.equals("mekanism:heat_transmitter"))
		{
			return new PartHeatTransmitter();
		}
		else if(name.equals("mekanism:glow_panel"))
		{
			return new PartGlowPanel();
		}

		return null;
	}

	public void registerMicroMaterials()
	{
		for(int i = 0; i < 16; i++)
		{
			MicroMaterialRegistry.registerMaterial(new PlasticMicroMaterial(MekanismBlocks.PlasticBlock, i), BlockMicroMaterial.materialKey(MekanismBlocks.PlasticBlock, i));
			MicroMaterialRegistry.registerMaterial(new PlasticMicroMaterial(MekanismBlocks.GlowPlasticBlock, i), BlockMicroMaterial.materialKey(MekanismBlocks.GlowPlasticBlock, i));
			MicroMaterialRegistry.registerMaterial(new PlasticMicroMaterial(MekanismBlocks.SlickPlasticBlock, i), BlockMicroMaterial.materialKey(MekanismBlocks.SlickPlasticBlock, i));
			MicroMaterialRegistry.registerMaterial(new PlasticMicroMaterial(MekanismBlocks.ReinforcedPlasticBlock, i), BlockMicroMaterial.materialKey(MekanismBlocks.ReinforcedPlasticBlock, i));
			MicroMaterialRegistry.registerMaterial(new PlasticMicroMaterial(MekanismBlocks.RoadPlasticBlock, i), BlockMicroMaterial.materialKey(MekanismBlocks.RoadPlasticBlock, i));

			FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(MekanismBlocks.BasicBlock, 1, i));
			
			if(!MachineType.get(MACHINE_BLOCK_1, i).hasModel)
			{
				FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(MekanismBlocks.MachineBlock, 1, i));
			}
			
			if(!MachineType.get(MACHINE_BLOCK_2, i).hasModel)
			{
				FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(MekanismBlocks.MachineBlock2, 1, i));
			}
		}
		
		FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(MekanismBlocks.BasicBlock2, 1, 0));
		FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(MekanismBlocks.CardboardBox));
	}
}
