package mekanism.generators.common;

import mekanism.common.item.ItemMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.item.ItemHohlraum;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

@ObjectHolder(MekanismGenerators.MODID)
public enum GeneratorsItem {
    SOLAR_PANEL(new ItemMekanism("solar_panel")),
    HOHLRAUM(new ItemHohlraum()),
    TURBINE_BLADE(new ItemMekanism("turbine_blade") {
        @Override
        public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
            return MekanismUtils.getTileEntitySafe(world, pos) instanceof TileEntityTurbineRotor;
        }
    });

    private final Item item;

    GeneratorsItem(Item item) {
        this.item = item;
        //TODO: This part is needed (or more accurately it being registered against MekanismGenerators instead of Mekanism)
        item.setRegistryName(new ResourceLocation(MekanismGenerators.MODID, item.getRegistryName().getPath()));
    }

    public Item getItem() {
        return item;
    }

    public ItemStack getItemStack() {
        return getItemStack(1);
    }

    public ItemStack getItemStack(int size) {
        return new ItemStack(getItem(), size);
    }

    public static void registerItems(IForgeRegistry<Item> registry) {
        for (GeneratorsItem generatorsItem : values()) {
            registry.register(generatorsItem.getItem());
        }
    }
}