package mekanism.client.lang;

import java.io.IOException;
import java.util.List;
import mekanism.api.text.IHasTranslationKey;
import mekanism.client.lang.FormatSplitter.Component;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraftforge.common.data.LanguageProvider;

public abstract class BaseLanguageProvider extends LanguageProvider {

    private final ConvertibleLanguageProvider[] altProviders;

    public BaseLanguageProvider(DataGenerator gen, String modid) {
        super(gen, modid, "en_us");
        altProviders = new ConvertibleLanguageProvider[]{
              new UpsideDownLanguageProvider(gen, modid)
        };
    }

    protected void add(IHasTranslationKey key, String value) {
        add(key.getTranslationKey(), value);
    }

    @Override
    public void add(String key, String value) {
        super.add(key, value);
        if (altProviders.length > 0) {
            List<Component> splitEnglish = FormatSplitter.split(value);
            for (ConvertibleLanguageProvider provider : altProviders) {
                provider.convert(key, splitEnglish);
            }
        }
    }

    @Override
    public void act(DirectoryCache cache) throws IOException {
        super.act(cache);
        if (altProviders.length > 0) {
            for (ConvertibleLanguageProvider provider : altProviders) {
                provider.act(cache);
            }
        }
    }
}