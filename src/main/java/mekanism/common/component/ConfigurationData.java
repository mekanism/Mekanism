package mekanism.common.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jspecify.annotations.Nullable;

public record ConfigurationData(@Nullable CompoundTag configuration) implements TooltipProvider {

    public static final ConfigurationData NONE = new ConfigurationData(null);

    public static final Codec<ConfigurationData> CODEC = ExtraCodecs.optionalEmptyMap(CompoundTag.CODEC).xmap(
          configuration -> new ConfigurationData(configuration.orElse(null)),
          configuration -> Optional.ofNullable(configuration.configuration())
    );
    public static final StreamCodec<ByteBuf, ConfigurationData> STREAM_CODEC = ByteBufCodecs.optional(ByteBufCodecs.TRUSTED_COMPOUND_TAG).map(
          configuration -> new ConfigurationData(configuration.orElse(null)),
          configuration -> Optional.ofNullable(configuration.configuration())
    );

    public ConfigurationData {
        if (configuration != null && configuration.isEmpty()) {
            configuration = null;
        }
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
        Component name;
        if (configuration == null) {
            name = MekanismLang.NONE.translate();
        } else {
            name = configuration.getString(SerializationConstants.DATA_NAME)
                  .map(TextComponentUtil::translate)
                  .orElseGet(MekanismLang.NONE::translate);
        }
        builder.accept(MekanismLang.CONFIG_CARD_HAS_DATA.translateColored(EnumColor.GRAY, EnumColor.INDIGO, name));
    }
}