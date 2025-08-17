package mekanism.common.content.miner;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import mekanism.api.SerializationConstants;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.lib.WildcardMatcher;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

public class MinerTagFilter extends MinerFilter<MinerTagFilter> implements ITagFilter<MinerTagFilter> {

    public static final MapCodec<MinerTagFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> baseMinerCodec(instance)
          .and(Codec.STRING.fieldOf(SerializationConstants.TAG).forGetter(MinerTagFilter::getTagName))
          .apply(instance, MinerTagFilter::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MinerTagFilter> STREAM_CODEC = StreamCodec.composite(
          baseMinerStreamCodec(MinerTagFilter::new), Function.identity(),
          ByteBufCodecs.STRING_UTF8, MinerTagFilter::getTagName,
          (filter, tagName) -> {
              filter.tagName = tagName;
              return filter;
          }
    );

    private String tagName;

    public MinerTagFilter() {
    }

    protected MinerTagFilter(boolean enabled, Item replaceTarget, boolean requiresReplacement, String tagName) {
        super(enabled, replaceTarget, requiresReplacement);
        this.tagName = tagName;
    }

    public MinerTagFilter(MinerTagFilter filter) {
        super(filter);
        tagName = filter.tagName;
    }

    @Override
    public boolean canFilter(BlockState state) {
        return state.getTags().anyMatch(tag -> WildcardMatcher.matches(tagName, tag));
    }

    @Override
    public boolean hasBlacklistedElement() {
        return TagCache.tagHasMinerBlacklisted(tagName);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + tagName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        MinerTagFilter other = (MinerTagFilter) o;
        return tagName.equals(other.tagName);
    }

    @Override
    public MinerTagFilter clone() {
        return new MinerTagFilter(this);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.MINER_TAG_FILTER;
    }

    @Override
    public void setTagName(String name) {
        tagName = name;
    }

    @Override
    public String getTagName() {
        return tagName;
    }
}