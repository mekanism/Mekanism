package mekanism.generators.common;

import javax.annotation.Nonnull;
import mekanism.api.IItemProvider;
import mekanism.common.item.ItemMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.item.ItemHohlraum;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.registries.IForgeRegistry;

public enum GeneratorsItem implements IItemProvider {
    SOLAR_PANEL(new ItemMekanism(MekanismGenerators.MODID, "solar_panel")),
    HOHLRAUM(new ItemHohlraum()),
    TURBINE_BLADE(new ItemMekanism(MekanismGenerators.MODID, "turbine_blade") {
        @Override
        public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
            return MekanismUtils.getTileEntitySafe(world, pos) instanceof TileEntityTurbineRotor;
        }
    });

    private final Item item;

    GeneratorsItem(Item item) {
        this.item = item;
    }

    @Override
    @Nonnull
    public Item getItem() {
        return item;
    }

    public static void registerItems(IForgeRegistry<Item> registry) {
        for (GeneratorsItem generatorsItem : values()) {
            registry.register(generatorsItem.getItem());
        }
    }
}