package mekanism.defense.common;

import mekanism.common.tag.BaseTagProvider;
import net.minecraft.data.DataGenerator;

public class DefenseTagProvider extends BaseTagProvider {

    public DefenseTagProvider(DataGenerator gen) {
        super(gen, MekanismDefense.MODID);
    }

    @Override
    protected void registerTags() {
    }
}