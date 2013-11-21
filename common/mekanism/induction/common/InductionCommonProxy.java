/**
 * 
 */
package mekanism.induction.common;

import mekanism.common.Mekanism;
import mekanism.generators.common.MekanismGenerators;
import mekanism.induction.common.inventory.container.ContainerBattery;
import mekanism.induction.common.inventory.container.ContainerMultimeter;
import mekanism.induction.common.tileentity.TileEntityBattery;
import mekanism.induction.common.tileentity.TileEntityEMContractor;
import mekanism.induction.common.tileentity.TileEntityMultimeter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.common.network.IGuiHandler;

/**
 * @author Calclavia
 * 
 */
public class InductionCommonProxy implements IGuiHandler
{
	public void registerRenderers() {}
	
	public void loadConfiguration()
	{
		Mekanism.configuration.load();
		MekanismInduction.FURNACE_WATTAGE = (float)Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "Furnace Wattage Per Tick", MekanismInduction.FURNACE_WATTAGE).getDouble(MekanismInduction.FURNACE_WATTAGE);
		MekanismInduction.SOUND_FXS = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "Tesla Sound FXs", MekanismInduction.SOUND_FXS).getBoolean(MekanismInduction.SOUND_FXS);
		MekanismInduction.MAX_CONTRACTOR_DISTANCE = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "Max EM Contractor Path", MekanismInduction.MAX_CONTRACTOR_DISTANCE).getInt(MekanismInduction.MAX_CONTRACTOR_DISTANCE);

		TileEntityEMContractor.ACCELERATION = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "Contractor Item Acceleration", TileEntityEMContractor.ACCELERATION).getDouble(TileEntityEMContractor.ACCELERATION);
		TileEntityEMContractor.MAX_REACH = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "Contractor Max Item Reach", TileEntityEMContractor.MAX_REACH).getInt(TileEntityEMContractor.MAX_REACH);
		TileEntityEMContractor.MAX_SPEED = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "Contractor Max Item Speed", TileEntityEMContractor.MAX_SPEED).getDouble(TileEntityEMContractor.MAX_SPEED);
		TileEntityEMContractor.PUSH_DELAY = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "Contractor Item Push Delay", TileEntityEMContractor.PUSH_DELAY).getInt(TileEntityEMContractor.PUSH_DELAY);
		Mekanism.configuration.save();
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileEntityMultimeter)
		{
			return new ContainerMultimeter(player.inventory, ((TileEntityMultimeter) tileEntity));
		}
		else if (tileEntity instanceof TileEntityBattery)
		{
			return new ContainerBattery(player.inventory, ((TileEntityBattery) tileEntity));
		}

		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

	public void renderElectricShock(World world, Vector3 start, Vector3 target, float r, float g, float b, boolean split)
	{

	}

	public void renderElectricShock(World world, Vector3 start, Vector3 target, float r, float g, float b)
	{
		renderElectricShock(world, start, target, r, g, b, true);
	}

	public void renderElectricShock(World world, Vector3 start, Vector3 target, Vector3 color)
	{
		renderElectricShock(world, start, target, (float) color.x, (float) color.y, (float) color.z);
	}

	public void renderElectricShock(World world, Vector3 start, Vector3 target, Vector3 color, boolean split)
	{
		renderElectricShock(world, start, target, (float) color.x, (float) color.y, (float) color.z, split);
	}

	public void renderElectricShock(World world, Vector3 start, Vector3 target)
	{
		renderElectricShock(world, start, target, true);
	}

	public void renderElectricShock(World world, Vector3 start, Vector3 target, boolean b)
	{
		renderElectricShock(world, start, target, 0.55f, 0.7f, 1f, b);
	}

	public boolean isFancy()
	{
		return false;
	}
}
