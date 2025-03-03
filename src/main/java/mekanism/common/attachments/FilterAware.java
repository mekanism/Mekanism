package mekanism.common.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.Mekanism;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
public record FilterAware(List<IFilter<?>> filters) {

    public static final FilterAware EMPTY = new FilterAware(Collections.emptyList());

    public static final Codec<FilterAware> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          BaseFilter.GENERIC_CODEC.listOf().promotePartial(error -> Mekanism.logger.error("Failed to load filters: {}", error))
                .fieldOf(SerializationConstants.FILTERS).forGetter(FilterAware::filters)
    ).apply(instance, FilterAware::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FilterAware> STREAM_CODEC = BaseFilter.GENERIC_STREAM_CODEC.apply(ByteBufCodecs.list())
          .map(FilterAware::new, FilterAware::filters);

    public FilterAware {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        filters = List.copyOf(filters);
    }

    private <FILTER extends IFilter<?>> Stream<FILTER> getEnabledStream(Class<FILTER> filterClass) {
        return filters.stream()
              .filter(IFilter::isEnabled)
              .filter(filterClass::isInstance)
              .map(filterClass::cast);
    }

    public <FILTER extends IFilter<?>> List<FILTER> getEnabled(Class<FILTER> filterClass) {
        //TODO - 1.20.4: Do we want to cache enabled filters like we do for the filter manager?
        return getEnabledStream(filterClass).toList();
    }

    public <FILTER extends IFilter<?>> boolean anyEnabledMatch(Class<FILTER> filterClass, Predicate<FILTER> validator) {
        return getEnabledStream(filterClass).anyMatch(validator);
    }
}