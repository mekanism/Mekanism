package mekanism.common.lib.chunkloading;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * MultiMap-style Map which uses a fastutils Long2ObjectMap
 */
public class ChunkMultimap extends Long2ObjectOpenHashMap<Set<BlockPos>> implements INBTSerializable<ListNBT> {

    private static final String ENTRIES_KEY = "entries";
    private static final String KEY_KEY = "key";

    public ChunkMultimap() {
    }

    public boolean add(ChunkPos key, BlockPos value) {
        return computeIfAbsent(key.asLong(), k -> new ObjectOpenHashSet<>()).add(value);
    }

    public void remove(ChunkPos key, BlockPos value) {
        Set<BlockPos> chunkEntries = this.get(key.asLong());
        if (chunkEntries != null) {
            chunkEntries.remove(value);
            if (chunkEntries.isEmpty()) {
                this.remove(key.asLong());
            }
        }
    }

    @Override
    public ListNBT serializeNBT() {
        ListNBT listOut = new ListNBT();
        this.long2ObjectEntrySet().fastForEach(entry -> {
            if (!entry.getValue().isEmpty()) {
                CompoundNBT nbtEntry = new CompoundNBT();
                listOut.add(nbtEntry);
                nbtEntry.putLong(KEY_KEY, entry.getLongKey());
                ListNBT nbtEntryList = new ListNBT();
                nbtEntry.put(ENTRIES_KEY, nbtEntryList);
                for (BlockPos blockPos : entry.getValue()) {
                    nbtEntryList.add(NBTUtil.writeBlockPos(blockPos));
                }
            }
        });
        return listOut;
    }

    @Override
    public void deserializeNBT(ListNBT entryList) {
        for (int i = 0; i < entryList.size(); i++) {
            CompoundNBT entry = entryList.getCompound(i);
            long key = entry.getLong(KEY_KEY);
            ListNBT blockPosList = entry.getList(ENTRIES_KEY, NBT.TAG_COMPOUND);
            Set<BlockPos> blockPosSet = new ObjectOpenHashSet<>();
            this.put(key, blockPosSet);
            for (int j = 0; j < blockPosList.size(); j++) {
                blockPosSet.add(NBTUtil.readBlockPos(blockPosList.getCompound(j)));
            }
        }
    }
}