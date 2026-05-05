package mekanism.client.model.itemtint;

import com.mojang.serialization.MapCodec;
import mekanism.api.text.EnumColor;
import mekanism.common.item.interfaces.IColoredItem;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class ColorComponent implements ItemTintSource {

    public static final ColorComponent INSTANCE = new ColorComponent();
    public static final MapCodec<ColorComponent> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        Item item = stack.getItem();
        if (item instanceof IColoredItem) {
            EnumColor color = stack.get(MekanismDataComponents.COLOR);
            if (color == null) {
                return 0xFF555555;
            }
            return color.getPackedColor();
        }
        return -1;
    }

    @Override
    public MapCodec<ColorComponent> type() {
        return MAP_CODEC;
    }
}
