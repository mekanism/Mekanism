package mekanism.common.advancements;

import mekanism.common.Mekanism;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import javax.annotation.Nonnull;

public class baseAdvancementProvider extends AdvancementProvider {

    private final String modid;

    public baseAdvancementProvider(DataGenerator generator, ExistingFileHelper helper, String modid) {
        super(generator, helper);
        this.modid = modid;
    }

    @Nonnull
    @Override
    public String getName() {
        return super.getName() + ": " + modid;
    }

    protected static String advancementLocation(String name) {
        return new ResourceLocation(Mekanism.MODID, "mekanism/" + name).toString();
    }

    protected static TranslatableComponent title(String name) {
        return advancement(name + ".title");
    }

    protected static TranslatableComponent description(String name) {
        return advancement(name + ".description");
    }

    protected static MutableComponent obfuscatedDescription(String name) {
        return advancement(name + ".description").setStyle(Style.EMPTY.withObfuscated(true));
    }

    protected static TranslatableComponent advancement(String name) {
        return new TranslatableComponent("advancements.mekanism." + name);
    }
}
