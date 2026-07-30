package mekanism.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.neoforged.neoforge.transfer.item.ItemResource;

public record LockData(ItemResource lock) implements TooltipProvider {

    public static final LockData EMPTY = new LockData(ItemResource.EMPTY);

    public static final Codec<LockData> CODEC = RecordCodecBuilder.<LockData>create(instance -> instance.group(
          ItemResource.OPTIONAL_CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(LockData::lock)
    ).apply(instance, LockData::create)).orElse(
          (Consumer<String>) error -> Mekanism.logger.error("Failed to load stored lock data: {}", error),
          EMPTY
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, LockData> STREAM_CODEC = ItemResource.STREAM_CODEC.map(LockData::create, LockData::lock);

    public static LockData create(ItemResource lock) {
        if (lock.isEmpty()) {
            return EMPTY;
        }
        return new LockData(lock);
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
        if (!lock.isEmpty()) {
            builder.accept(MekanismLang.LOCKED.translateColored(EnumColor.AQUA, EnumColor.GRAY, lock));
        }
    }
}