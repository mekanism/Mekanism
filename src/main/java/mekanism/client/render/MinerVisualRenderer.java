package mekanism.client.render;

import java.util.HashMap;
import java.util.Map;

import mekanism.client.render.MekanismRenderer.DisplayInteger;

public final class MinerVisualRenderer 
{
	private Map<MinerRenderData, DisplayInteger> cachedVisuals = new HashMap<MinerRenderData, DisplayInteger>();
	
	public class MinerRenderData
	{
		public int minY;
		public int maxY;
		public int radius;
		
		public MinerRenderData(int min, int max, int rad)
		{
			minY = min;
			maxY = max;
			radius = rad;
		}
		
		@Override
		public boolean equals(Object data)
		{
			return data instanceof MinerRenderData && super.equals(data) && ((MinerRenderData)data).minY == minY && 
					((MinerRenderData)data).maxY == maxY && ((MinerRenderData)data).radius == radius;
		}

		@Override
		public int hashCode()
		{
			int code = 1;
			code = 31 * code + super.hashCode();
			code = 31 * code + minY;
			code = 31 * code + maxY;
			code = 31 * code + radius;
			return code;
		}
	}
}
