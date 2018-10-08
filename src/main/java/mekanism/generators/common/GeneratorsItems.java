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
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

@ObjectHolder("mekanismgenerators")
public class GeneratorsItems
{
	public static final Item SolarPanel = new ItemMekanism();
	public static final ItemHohlraum Hohlraum = (ItemHohlraum)new ItemHohlraum();
	public static final Item TurbineBlade = new ItemMekanism() {
		@Override
		public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player)
		{
			return MekanismUtils.getTileEntitySafe(world, pos) instanceof TileEntityTurbineRotor;
		}
	};

	public static void registerItems(IForgeRegistry<Item> registry)
	{
		registry.register(init(SolarPanel, "SolarPanel"));
		registry.register(init(Hohlraum, "Hohlraum"));
		registry.register(init(TurbineBlade, "TurbineBlade"));
	}
	
	public static Item init(Item item, String name)
	{
		return item.setTranslationKey(name).setRegistryName("mekanismgenerators:" + name);
	}
}
