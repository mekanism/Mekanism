package mekanism.client.model;

import javax.annotation.Nonnull;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public abstract class BaseBlockModelProvider extends BlockModelProvider {

    public BaseBlockModelProvider(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
        super(generator, modid, existingFileHelper);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Block model provider: " + modid;
    }
}