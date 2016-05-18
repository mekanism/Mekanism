package mekanism.generators.common;

import mekanism.common.item.ItemMekanism;
import mekanism.generators.common.item.ItemHohlraum;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("MekanismGenerators")
public class GeneratorsItems
{
	public static final Item SolarPanel = new ItemMekanism().setUnlocalizedName("SolarPanel");
	public static final ItemHohlraum Hohlraum = (ItemHohlraum)new ItemHohlraum().setUnlocalizedName("Hohlraum");
	public static final Item TurbineBlade = new ItemMekanism() {
		@Override
		public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player)
		{
			return world.getTileEntity(pos) instanceof TileEntityTurbineRotor;
		}
	}.setUnlocalizedName("TurbineBlade");

	public static void register()
	{
		GameRegistry.registerItem(SolarPanel, "SolarPanel");
		GameRegistry.registerItem(Hohlraum, "Hohlraum");
		GameRegistry.registerItem(TurbineBlade, "TurbineBlade");
		
		MekanismGenerators.proxy.registerItemRenders();
	}
}
