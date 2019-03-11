package mekanism.client.render;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraftforge.fluids.FluidStack;


/**
 * Map which uses FluidStacks as keys, ignoring amount.
 * Primary use: caching FluidStack aware fluid rendering (NBT, yay)
 */
public class FluidRenderMap<V> extends Object2ObjectOpenCustomHashMap<FluidStack,V>
{
	public FluidRenderMap(){
		super(FluidHashStrategy.INSTANCE);
	}

	/**
	 * Implements equals & hashCode that ignore FluidStack#amount
	 */
	public static class FluidHashStrategy implements Hash.Strategy<FluidStack>{

		public static FluidHashStrategy INSTANCE = new FluidHashStrategy();

		@Override
		public int hashCode(FluidStack stack)
		{
			if (stack == null){
				return 0;
			}
			int code = 1;
			code = 31*code + stack.getFluid().hashCode();
			if (stack.tag != null)
				code = 31*code + stack.tag.hashCode();
			return code;
		}

		@Override
		public boolean equals(FluidStack a, FluidStack b)
		{
			if (a == null){
				return b==null;
			}
			return a.isFluidEqual(b);
		}
	}
}
