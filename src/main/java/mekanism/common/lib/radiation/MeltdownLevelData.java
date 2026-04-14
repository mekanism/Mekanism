package mekanism.common.lib.radiation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.MethodsAreNotNullByDefault;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismAttachmentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.TypedOutputList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@MethodsAreNotNullByDefault
@ParametersAreNonnullByDefault
@EventBusSubscriber(modid = Mekanism.MODID)
public class MeltdownLevelData implements ValueIOSerializable {

    private final List<Meltdown> meltdowns = new ArrayList<>();

    public void createMeltdown(BlockPos minPos, BlockPos maxPos, double magnitude, double chance, float radius, UUID multiblockID) {
        meltdowns.add(new Meltdown(minPos, maxPos, magnitude, chance, radius, multiblockID));
    }

    @SubscribeEvent
    public static void tickWorld(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            MeltdownLevelData existingData = level.getExistingDataOrNull(MekanismAttachmentTypes.MELTDOWN_DATA);
            if (existingData != null) {
                existingData.tick(serverLevel);
            }
        }
    }

    public void tick(ServerLevel world) {
        if (meltdowns.isEmpty()) {
            return;
        }
        //noinspection Java8CollectionRemoveIf - We can't replace it with removeIf as it has a capturing lambda
        for (Iterator<Meltdown> iterator = meltdowns.iterator(); iterator.hasNext(); ) {
            Meltdown meltdown = iterator.next();
            if (meltdown.update(world)) {
                iterator.remove();
            }
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        //TODO - 1.21.11: Re-evaluate if we want this to be stored under, as previously it was just as a list without needing a key
        // Also figure out if this properly supports being lenient if say one meltdown source is of a broken format
        for (Meltdown meltdown : input.listOrEmpty(SerializationConstants.VALUE, Meltdown.CODEC)) {
            this.meltdowns.add(meltdown);
        }
    }

    @Override
    public void serialize(ValueOutput output) {
        if (!meltdowns.isEmpty()) {
            TypedOutputList<Meltdown> meltdownOutput = output.list(SerializationConstants.VALUE, Meltdown.CODEC);
            for (Meltdown meltdown : meltdowns) {
                meltdownOutput.add(meltdown);
            }
        }
    }
}
