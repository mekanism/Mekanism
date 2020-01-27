package mekanism.client.render.block;

import mekanism.client.ClientProxy;
import mekanism.client.model.ModelChargepad;
import mekanism.client.model.ModelChemicalCrystallizer;
import mekanism.client.model.ModelChemicalDissolutionChamber;
import mekanism.client.model.ModelChemicalInfuser;
import mekanism.client.model.ModelChemicalOxidizer;
import mekanism.client.model.ModelChemicalWasher;
import mekanism.client.model.ModelDigitalMiner;
import mekanism.client.model.ModelElectricPump;
import mekanism.client.model.ModelElectrolyticSeparator;
import mekanism.client.model.ModelFluidicPlenisher;
import mekanism.client.model.ModelLaser;
import mekanism.client.model.ModelLaserAmplifier;
import mekanism.client.model.ModelLogisticalSorter;
import mekanism.client.model.ModelMetallurgicInfuser;
import mekanism.client.model.ModelPressurizedReactionChamber;
import mekanism.client.model.ModelQuantumEntangloporter;
import mekanism.client.model.ModelResistiveHeater;
import mekanism.client.model.ModelRotaryCondensentrator;
import mekanism.client.model.ModelSeismicVibrator;
import mekanism.client.model.ModelSolarNeutronActivator;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MachineRenderingHandler implements ISimpleBlockRenderingHandler
{
	private Minecraft mc = Minecraft.getMinecraft();
	
	public ModelElectricPump electricPump = new ModelElectricPump();
	public ModelMetallurgicInfuser metallurgicInfuser = new ModelMetallurgicInfuser();
	public ModelChargepad chargepad = new ModelChargepad();
	public ModelLogisticalSorter logisticalSorter = new ModelLogisticalSorter();
	public ModelDigitalMiner digitalMiner = new ModelDigitalMiner();
	public ModelRotaryCondensentrator rotaryCondensentrator = new ModelRotaryCondensentrator();
	public ModelChemicalOxidizer chemicalOxidizer = new ModelChemicalOxidizer();
	public ModelChemicalInfuser chemicalInfuser = new ModelChemicalInfuser();
	public ModelElectrolyticSeparator electrolyticSeparator = new ModelElectrolyticSeparator();
	public ModelChemicalDissolutionChamber chemicalDissolutionChamber = new ModelChemicalDissolutionChamber();
	public ModelChemicalWasher chemicalWasher = new ModelChemicalWasher();
	public ModelChemicalCrystallizer chemicalCrystallizer = new ModelChemicalCrystallizer();
	public ModelSeismicVibrator seismicVibrator = new ModelSeismicVibrator();
	public ModelPressurizedReactionChamber pressurizedReactionChamber = new ModelPressurizedReactionChamber();
	public ModelFluidicPlenisher fluidicPlenisher = new ModelFluidicPlenisher();
	public ModelLaser laser = new ModelLaser();
	public ModelLaserAmplifier laserAmplifier = new ModelLaserAmplifier();
	public ModelSolarNeutronActivator solarNeutronActivator = new ModelSolarNeutronActivator();
	public ModelResistiveHeater resistiveHeater = new ModelResistiveHeater();
	public ModelQuantumEntangloporter quantumEntangloporter = new ModelQuantumEntangloporter();

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		if(block == null || renderer == null || MachineType.get(block, metadata) == null)
		{
			return;
		}

		GL11.glPushMatrix();
		GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);

		MachineType type = MachineType.get(block, metadata);

		if(type == MachineType.ELECTRIC_PUMP)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glTranslatef(0.0F, -0.85F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ElectricPump.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) electricPump.render(0.0560F, true);
			else electricPump.render(0.0560F);
		}
		else if(type == MachineType.METALLURGIC_INFUSER)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(180F, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -1.0F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "MetallurgicInfuser.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) metallurgicInfuser.render(0.0625F, true);
			else metallurgicInfuser.render(0.0625F);
		}
		else if(type == MachineType.CHARGEPAD)
		{
			GL11.glRotatef(180F, 1.0F, 0.0F, 0.0F);
			GL11.glTranslatef(0.0F, -1.1F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Chargepad.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) chargepad.render(0.0625F, mc.renderEngine, true);
			else chargepad.render(0.0625F, mc.renderEngine);
		}
		else if(type == MachineType.LOGISTICAL_SORTER)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -1.0F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "LogisticalSorter.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) logisticalSorter.render(0.0625F, false, true);
			else logisticalSorter.render(0.0625F, false);
		}
		else if(type == MachineType.DIGITAL_MINER)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(-180F, 0.0F, 1.0F, 0.0F);
			GL11.glTranslatef(0.35F, 0.1F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "DigitalMiner.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) digitalMiner.render(0.022F, false, mc.renderEngine, true);
			else digitalMiner.render(0.022F, false, mc.renderEngine);
		}
		else if(type == MachineType.ROTARY_CONDENSENTRATOR)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(180F, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.05F, -0.96F, 0.05F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "RotaryCondensentrator.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) rotaryCondensentrator.render(0.0625F, true);
			else rotaryCondensentrator.render(0.0625F);
		}
		else if(type == MachineType.CHEMICAL_OXIDIZER)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -1.00F, 0.05F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ChemicalOxidizer.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) chemicalOxidizer.render(0.0625F, true);
			else chemicalOxidizer.render(0.0625F);
		}
		else if(type == MachineType.CHEMICAL_INFUSER)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(180f, 0.0F, 1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -0.96F, 0.05F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ChemicalInfuser.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) chemicalInfuser.render(0.0625F, true);
			else chemicalInfuser.render(0.0625F);
		}
		else if(type == MachineType.ELECTROLYTIC_SEPARATOR)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(-90F, 0.0F, 1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -1.0F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ElectrolyticSeparator.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) electrolyticSeparator.render(0.0625F, true);
			else electrolyticSeparator.render(0.0625F);
		}
		else if(type == MachineType.CHEMICAL_DISSOLUTION_CHAMBER)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(180F, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.05F, -0.96F, 0.05F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ChemicalDissolutionChamber.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) chemicalDissolutionChamber.render(0.0625F, true);
			else chemicalDissolutionChamber.render(0.0625F);
		}
		else if(type == MachineType.CHEMICAL_WASHER)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(180F, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.05F, -0.96F, 0.05F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ChemicalWasher.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) chemicalWasher.render(0.0625F, true);
			else chemicalWasher.render(0.0625F);
		}
		else if(type == MachineType.CHEMICAL_CRYSTALLIZER)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(180F, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.05F, -0.96F, 0.05F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ChemicalCrystallizer.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) chemicalCrystallizer.render(0.0625F, true);
			else chemicalCrystallizer.render(0.0625F);
		}
		else if(type == MachineType.SEISMIC_VIBRATOR)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(180F, 0.0F, -1.0F, 0.0F);
			GL11.glScalef(0.6F, 0.6F, 0.6F);
			GL11.glTranslatef(0.0F, -0.55F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "SeismicVibrator.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) seismicVibrator.render(0.0625F, true);
			else seismicVibrator.render(0.0625F);
		}
		else if(type == MachineType.PRESSURIZED_REACTION_CHAMBER)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(180F, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.05F, -0.96F, 0.05F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "PressurizedReactionChamber.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) pressurizedReactionChamber.render(0.0625F, true);
			else pressurizedReactionChamber.render(0.0625F);
		}
		else if(type == MachineType.FLUIDIC_PLENISHER)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(180F, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -0.85F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "FluidicPlenisher.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) fluidicPlenisher.render(0.0560F, true);
			else fluidicPlenisher.render(0.0560F);
		}
		else if(type == MachineType.LASER)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(90F, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -0.85F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Laser.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) laser.render(0.0560F, true);
			else laser.render(0.0560F);
		}
		else if(type == MachineType.LASER_AMPLIFIER)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(90F, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -0.85F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "LaserAmplifier.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) laserAmplifier.render(0.0560F, true);
			else laserAmplifier.render(0.0560F);
		}
		else if(type == MachineType.LASER_TRACTOR_BEAM)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(90F, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -0.85F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "LaserTractorBeam.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) laserAmplifier.render(0.0560F, true);
			else laserAmplifier.render(0.0560F);
		}
		else if(type == MachineType.RESISTIVE_HEATER)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(180F, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.05F, -0.96F, 0.05F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ResistiveHeater.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) resistiveHeater.render(0.0625F, false, mc.renderEngine, true);
			else resistiveHeater.render(0.0625F, false, mc.renderEngine);
		}
		else if(type == MachineType.SOLAR_NEUTRON_ACTIVATOR)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);
			GL11.glScalef(0.6F, 0.6F, 0.6F);
			GL11.glTranslatef(0.0F, -0.55F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "SolarNeutronActivator.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) solarNeutronActivator.render(0.0625F, true);
			else solarNeutronActivator.render(0.0625F);
		}
		else if(type == MachineType.QUANTUM_ENTANGLOPORTER)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(180F, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -1.0F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "QuantumEntangloporter.png"));
			if (mekanism.api.MekanismConfig.client.reducerendermachines) quantumEntangloporter.render(0.0625F, mc.renderEngine, true);
			else quantumEntangloporter.render(0.0625F, mc.renderEngine);
		}
		else {
			GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);
			MekanismRenderer.renderItem(renderer, metadata, block);
		}

		GL11.glPopMatrix();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		//Handled by CTMRenderingHandler
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		if (mekanism.api.MekanismConfig.client.reducerendermachines) return false;
		return true;
	}

	@Override
	public int getRenderId()
	{
		return ClientProxy.MACHINE_RENDER_ID;
	}
}
