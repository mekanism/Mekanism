package mekanism.defense.common;

import javax.annotation.Nullable;
import mekanism.common.tag.BaseTagProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class DefenseTagProvider extends BaseTagProvider {

    public DefenseTagProvider(DataGenerator gen, @Nullable ExistingFileHelper existingFileHelper) {
        super(gen, MekanismDefense.MODID, existingFileHelper);
    }

    @Override
    protected void registerTags() {
    }
}