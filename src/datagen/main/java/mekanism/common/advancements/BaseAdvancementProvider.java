package mekanism.common.advancements;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public abstract class BaseAdvancementProvider extends AdvancementProvider {

    private final String modid;

    public BaseAdvancementProvider(DataGenerator generator, ExistingFileHelper existingFileHelper, String modid) {
        super(generator, existingFileHelper);
        this.modid = modid;
    }

    @Nonnull
    @Override
    public String getName() {
        return super.getName() + ": " + modid;
    }

    @Override
    protected abstract void registerAdvancements(@Nonnull Consumer<Advancement> consumer, @Nonnull ExistingFileHelper existingFileHelper);

    protected ExtendedAdvancementBuilder advancement(MekanismAdvancement advancement) {
        return ExtendedAdvancementBuilder.advancement(advancement, fileHelper);
    }
}