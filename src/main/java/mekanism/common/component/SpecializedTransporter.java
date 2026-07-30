package mekanism.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.tier.TransporterTier;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record SpecializedTransporter(TransporterTier equivalentTier, Component extraDetails) implements TooltipProvider {

    public static final Codec<SpecializedTransporter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          TransporterTier.CODEC.optionalFieldOf(SerializationConstants.TIER, TransporterTier.BASIC).forGetter(SpecializedTransporter::equivalentTier),
          ComponentSerialization.CODEC.fieldOf(SerializationConstants.DETAILS).forGetter(SpecializedTransporter::extraDetails)
    ).apply(instance, SpecializedTransporter::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpecializedTransporter> STREAM_CODEC = StreamCodec.composite(
          TransporterTier.STREAM_CODEC, SpecializedTransporter::equivalentTier,
          ComponentSerialization.STREAM_CODEC, SpecializedTransporter::extraDetails,
          SpecializedTransporter::new
    );

    public SpecializedTransporter(ILangEntry extraDetails) {
        this(TransporterTier.BASIC, extraDetails.translateColored(EnumColor.DARK_RED));
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
        equivalentTier.addToTooltip(context, builder, flag, components);
        builder.accept(extraDetails);
    }
}