package mekanism.common.item.block;

import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import net.minecraft.world.level.block.Block;

public class FrequencyBlockItem extends MekanismBlockItem implements IFrequencyItem {

    private final FrequencyType<?> frequencyType;

    public FrequencyBlockItem(Block block, FrequencyType<?> frequencyType, Properties properties) {
        this.frequencyType = frequencyType;
        super(block, properties);
    }

    @Override
    public final FrequencyType<?> getFrequencyType() {
        return frequencyType;
    }
}