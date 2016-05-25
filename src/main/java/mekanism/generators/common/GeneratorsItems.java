package mekanism.generators.common;

import mekanism.common.item.ItemMekanism;
import mekanism.generators.common.item.ItemHohlraum;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("MekanismGenerators")
public class GeneratorsItems
{
	public static final Item SolarPanel = new ItemMekanism().setUnlocalizedName("SolarPanel");
	public static final ItemHohlraum Hohlraum = (ItemHohlraum)new ItemHohlraum().setUnlocalizedName("Hohlraum");
	public static final Item TurbineBlade = new ItemMekanism() {
		@Override
		public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player)
		{
			return world.getTileEntity(pos) instanceof TileEntityTurbineRotor;
		}
	}.setUnlocalizedName("TurbineBlade");

	public static void register()
	{
		GameRegistry.register(SolarPanel);
		GameRegistry.register(Hohlraum);
		GameRegistry.register(TurbineBlade);
		
		MekanismGenerators.proxy.registerItemRenders();
	}
}
