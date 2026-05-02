package mekanism.client.model.props;

import com.mojang.serialization.MapCodec;
import mekanism.common.item.ItemConfigurationCard;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ConfigCardEncoded implements ConditionalItemModelProperty {
    public static final ConfigCardEncoded INSTANCE = new ConfigCardEncoded();
    public static final MapCodec<ConfigCardEncoded> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public MapCodec<? extends ConditionalItemModelProperty> type() {
        return CODEC;
    }

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        return itemStack.getItem() instanceof ItemConfigurationCard card && card.hasData(itemStack);
    }
}
