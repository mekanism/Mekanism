package mekanism.common.content.miner;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.util.NBTUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class MinerFilter<FILTER extends MinerFilter<FILTER>> extends BaseFilter<FILTER> {

    public Item replaceTarget = Items.AIR;
    public boolean requiresReplacement;

    protected MinerFilter() {
    }

    protected MinerFilter(FILTER filter) {
        replaceTarget = filter.replaceTarget;
        requiresReplacement = filter.requiresReplacement;
    }

    public boolean replaceTargetMatches(@Nonnull Item target) {
        return replaceTarget != Items.AIR && replaceTarget == target;
    }

    public abstract boolean canFilter(BlockState state);

    public abstract boolean hasBlacklistedElement();

    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean(NBTConstants.REQUIRE_STACK, requiresReplacement);
        if (replaceTarget != Items.AIR) {
            nbtTags.putString(NBTConstants.REPLACE_STACK, replaceTarget.getRegistryName().toString());
        }
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        requiresReplacement = nbtTags.getBoolean(NBTConstants.REQUIRE_STACK);
        //TODO - 1.17: Remove this legacy loading branch
        if (nbtTags.contains(NBTConstants.REPLACE_STACK, NBT.TAG_COMPOUND)) {
            replaceTarget = ItemStack.of(nbtTags.getCompound(NBTConstants.REPLACE_STACK)).getItem();
        } else {
            replaceTarget = NBTUtils.readRegistryEntry(nbtTags, NBTConstants.REPLACE_STACK, ForgeRegistries.ITEMS, Items.AIR);
        }
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        buffer.writeBoolean(requiresReplacement);
        buffer.writeRegistryId(replaceTarget);
    }

    @Override
    public void read(PacketBuffer dataStream) {
        requiresReplacement = dataStream.readBoolean();
        replaceTarget = dataStream.readRegistryId();
    }

    public abstract boolean equals(Object filter);

    public abstract int hashCode();
}