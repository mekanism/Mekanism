package mekanism.client.render.tileentity;

import java.util.HashMap;
import java.util.Map;

import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.tileentity.RenderDynamicTank.RenderData;
import mekanism.client.render.tileentity.RenderDynamicTank.ValveRenderData;
import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderThermoelectricBoiler extends TileEntitySpecialRenderer
{
	private static Map<RenderData, DisplayInteger[]> cachedLowerFluids = new HashMap<RenderData, DisplayInteger[]>();
	private static Map<RenderData, DisplayInteger[]> cachedUpperFluids = new HashMap<RenderData, DisplayInteger[]>();
	private static Map<ValveRenderData, DisplayInteger> cachedValveFluids = new HashMap<ValveRenderData, DisplayInteger>();
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityBoilerCasing)tileEntity, x, y, z, partialTick);
	}

	public void renderAModelAt(TileEntityBoilerCasing tileEntity, double x, double y, double z, float partialTick)
	{
		if(tileEntity.clientHasStructure && tileEntity.isRendering && tileEntity.structure != null)
		{
			if(tileEntity.structure.waterStored != null && tileEntity.structure.waterStored.amount != 0)
			{
				
			}
			
			if(tileEntity.structure.steamStored != null && tileEntity.structure.steamStored.amount != 0)
			{
				
			}
		}
	}
	
	public static void resetDisplayInts()
	{
		cachedLowerFluids.clear();
		cachedUpperFluids.clear();
		cachedValveFluids.clear();
	}
}
