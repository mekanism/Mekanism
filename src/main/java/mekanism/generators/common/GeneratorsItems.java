package mekanism.generators.common;

import static mekanism.common.MekanismItems.init;
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
	public static final Item SolarPanel = init(new ItemMekanism(), "SolarPanel");
	public static final ItemHohlraum Hohlraum = (ItemHohlraum)init(new ItemHohlraum(), "Hohlraum");
	public static final Item TurbineBlade = init(new ItemMekanism() {
		@Override
		public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player)
		{
			return world.getTileEntity(pos) instanceof TileEntityTurbineRotor;
		}
	}, "TurbineBlade");

	public static void register()
	{
		GameRegistry.register(SolarPanel);
		GameRegistry.register(Hohlraum);
		GameRegistry.register(TurbineBlade);
		
		MekanismGenerators.proxy.registerItemRenders();
	}
}
