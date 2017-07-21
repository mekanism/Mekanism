package mekanism.generators.common;

import mekanism.common.item.ItemMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.item.ItemHohlraum;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("mekanismgenerators")
public class GeneratorsItems
{
	public static final Item SolarPanel = new ItemMekanism();
	public static final ItemHohlraum Hohlraum = (ItemHohlraum)new ItemHohlraum();
	public static final Item TurbineBlade = new ItemMekanism() {
		@Override
		public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player)
		{
			return MekanismUtils.getTileEntitySave(world, pos) instanceof TileEntityTurbineRotor;
		}
	};

	public static void register()
	{
		GameRegistry.register(init(SolarPanel, "SolarPanel"));
		GameRegistry.register(init(Hohlraum, "Hohlraum"));
		GameRegistry.register(init(TurbineBlade, "TurbineBlade"));
		
		MekanismGenerators.proxy.registerItemRenders();
	}
	
	public static Item init(Item item, String name)
	{
		return item.setUnlocalizedName(name).setRegistryName("mekanismgenerators:" + name);
	}
}
