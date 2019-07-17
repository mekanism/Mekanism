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
    SOLAR_PANEL("solar_panel"),
    HOHLRAUM("hohlraum", new ItemHohlraum()),
    TURBINE_BLADE("turbine_blade", new ItemMekanism() {
        @Override
        public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
            return MekanismUtils.getTileEntitySafe(world, pos) instanceof TileEntityTurbineRotor;
        }
    });

    private final String name;
    private final Item item;

    GeneratorsItem(String name) {
        this(name, new ItemMekanism());
    }

    GeneratorsItem(String name, Item item) {
        this.item = item;
        this.name = name;
        //TODO: Maybe do some of this internally
        init();
    }

    private void init() {
        item.setTranslationKey(getTranslationKey());
        item.setRegistryName(new ResourceLocation(MekanismGenerators.MODID, name));
    }

    public String getTranslationKey() {
        return "item.mekanism." + name;
    }

    public String getName() {
        return name;
    }

    public Item getItem() {
        return item;
    }

    public static void registerItems(IForgeRegistry<Item> registry) {
        for (GeneratorsItem generatorsItem : values()) {
            registry.register(generatorsItem.getItem());
        }
    }
}