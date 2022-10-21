package mekanism.defense.common;

import mekanism.common.tag.BaseTagProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class DefenseTagProvider extends BaseTagProvider {

    public DefenseTagProvider(DataGenerator gen, @Nullable ExistingFileHelper existingFileHelper) {
        super(gen, MekanismDefense.MODID, existingFileHelper);
    }

    @Override
    protected void registerTags() {
    }
}