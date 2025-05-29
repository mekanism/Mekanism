package mekanism.common.content.miner;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.SerializationConstants;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class MinerFilter<FILTER extends MinerFilter<FILTER>> extends BaseFilter<FILTER> {

    protected static <FILTER extends MinerFilter<FILTER>> P3<Mu<FILTER>, Boolean, Item, Boolean> baseMinerCodec(Instance<FILTER> instance) {
        return baseCodec(instance)
              .and(BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf(SerializationConstants.REPLACE_TARGET, Items.AIR).forGetter(filter -> filter.replaceTarget))
              .and(Codec.BOOL.optionalFieldOf(SerializationConstants.REQUIRES_REPLACEMENT, false).forGetter(filter -> filter.requiresReplacement))
              ;
    }

    protected static <FILTER extends MinerFilter<FILTER>> StreamCodec<RegistryFriendlyByteBuf, FILTER> baseMinerStreamCodec(Supplier<FILTER> constructor) {
        return StreamCodec.composite(
              baseStreamCodec(constructor), Function.identity(),
              ByteBufCodecs.registry(Registries.ITEM), filter -> filter.replaceTarget,
              ByteBufCodecs.BOOL, filter -> filter.requiresReplacement,
              (filter, replaceTarget, requiresReplacement) -> {
                  filter.replaceTarget = replaceTarget;
                  filter.requiresReplacement = requiresReplacement;
                  return filter;
              }
        );
    }

    @SyntheticComputerMethod(getter = "getReplaceTarget", setter = "setReplaceTarget", threadSafeGetter = true, threadSafeSetter = true)
    public Item replaceTarget = Items.AIR;
    @SyntheticComputerMethod(getter = "getRequiresReplacement", setter = "setRequiresReplacement", threadSafeSetter = true, threadSafeGetter = true)
    public boolean requiresReplacement;

    protected MinerFilter() {
    }

    protected MinerFilter(boolean enabled, Item replaceTarget, boolean requiresReplacement) {
        super(enabled);
        this.replaceTarget = replaceTarget;
        this.requiresReplacement = requiresReplacement;
    }

    protected MinerFilter(FILTER filter) {
        super(filter);
        replaceTarget = filter.replaceTarget;
        requiresReplacement = filter.requiresReplacement;
    }

    public boolean replaceTargetMatches(@NotNull Item target) {
        return replaceTarget != Items.AIR && replaceTarget == target;
    }

    public abstract boolean canFilter(BlockState state);

    @ComputerMethod
    public abstract boolean hasBlacklistedElement();

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + replaceTarget.hashCode();
        result = 31 * result + Boolean.hashCode(requiresReplacement);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        MinerFilter<?> other = (MinerFilter<?>) o;
        return requiresReplacement == other.requiresReplacement && replaceTarget == other.replaceTarget;
    }

    @Override
    @ComputerMethod(threadSafe = true)
    public abstract FILTER clone();
}