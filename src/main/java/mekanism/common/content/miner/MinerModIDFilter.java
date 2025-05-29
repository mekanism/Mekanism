package mekanism.common.content.miner;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import mekanism.api.SerializationConstants;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.lib.WildcardMatcher;
import mekanism.common.util.RegistryUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

public class MinerModIDFilter extends MinerFilter<MinerModIDFilter> implements IModIDFilter<MinerModIDFilter> {

    public static final MapCodec<MinerModIDFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> baseMinerCodec(instance)
          .and(Codec.STRING.fieldOf(SerializationConstants.MODID).forGetter(MinerModIDFilter::getModID))
          .apply(instance, MinerModIDFilter::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MinerModIDFilter> STREAM_CODEC = StreamCodec.composite(
          baseMinerStreamCodec(MinerModIDFilter::new), Function.identity(),
          ByteBufCodecs.STRING_UTF8, MinerModIDFilter::getModID,
          (filter, modID) -> {
              filter.modID = modID;
              return filter;
          }
    );

    private String modID;

    public MinerModIDFilter() {
    }

    protected MinerModIDFilter(boolean enabled, Item replaceTarget, boolean requiresReplacement, String modID) {
        super(enabled, replaceTarget, requiresReplacement);
        this.modID = modID;
    }

    public MinerModIDFilter(MinerModIDFilter filter) {
        super(filter);
        modID = filter.modID;
    }

    @Override
    public boolean canFilter(BlockState state) {
        ResourceLocation name = RegistryUtils.getName(state.getBlockHolder());
        return name != null && WildcardMatcher.matches(modID, name.getNamespace());
    }

    @Override
    public boolean hasBlacklistedElement() {
        return TagCache.modIDHasMinerBlacklisted(modID);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + modID.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        MinerModIDFilter other = (MinerModIDFilter) o;
        return modID.equals(other.modID);
    }

    @Override
    public MinerModIDFilter clone() {
        return new MinerModIDFilter(this);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.MINER_MODID_FILTER;
    }

    @Override
    public void setModID(String id) {
        modID = id;
    }

    @Override
    public String getModID() {
        return modID;
    }
}