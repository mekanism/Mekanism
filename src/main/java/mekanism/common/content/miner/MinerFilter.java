package mekanism.common.content.miner;

import java.util.Objects;
import mekanism.api.NBTConstants;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public abstract class MinerFilter<FILTER extends MinerFilter<FILTER>> extends BaseFilter<FILTER> {

    @SyntheticComputerMethod(getter = "getReplaceTarget", setter = "setReplaceTarget", threadSafeGetter = true, threadSafeSetter = true)
    public Item replaceTarget = Items.AIR;
    @SyntheticComputerMethod(getter = "getRequiresReplacement", setter = "setRequiresReplacement", threadSafeSetter = true, threadSafeGetter = true)
    public boolean requiresReplacement;

    protected MinerFilter() {
    }

    protected MinerFilter(FILTER filter) {
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
    public CompoundTag write(CompoundTag nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean(NBTConstants.REQUIRE_STACK, requiresReplacement);
        if (replaceTarget != Items.AIR) {
            NBTUtils.writeRegistryEntry(nbtTags, NBTConstants.REPLACE_STACK, ForgeRegistries.ITEMS, replaceTarget);
        }
        return nbtTags;
    }

    @Override
    public void read(CompoundTag nbtTags) {
        super.read(nbtTags);
        requiresReplacement = nbtTags.getBoolean(NBTConstants.REQUIRE_STACK);
        replaceTarget = NBTUtils.readRegistryEntry(nbtTags, NBTConstants.REPLACE_STACK, ForgeRegistries.ITEMS, Items.AIR);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeBoolean(requiresReplacement);
        buffer.writeRegistryId(ForgeRegistries.ITEMS, replaceTarget);
    }

    @Override
    public void read(FriendlyByteBuf dataStream) {
        super.read(dataStream);
        requiresReplacement = dataStream.readBoolean();
        replaceTarget = dataStream.readRegistryIdSafe(Item.class);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), replaceTarget, requiresReplacement);
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