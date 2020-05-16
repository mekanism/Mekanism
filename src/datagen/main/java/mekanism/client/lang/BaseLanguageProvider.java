package mekanism.client.lang;

import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.IHasTranslationKey;
import mekanism.client.lang.FormatSplitter.Component;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.registration.impl.FluidRegistryObject;
import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.item.BucketItem;
import net.minecraft.util.Util;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.fluids.ForgeFlowingFluid.Flowing;
import net.minecraftforge.fluids.ForgeFlowingFluid.Source;

public abstract class BaseLanguageProvider extends LanguageProvider {

    private final ConvertibleLanguageProvider[] altProviders;
    private final String modid;

    public BaseLanguageProvider(DataGenerator gen, String modid) {
        super(gen, modid, "en_us");
        this.modid = modid;
        altProviders = new ConvertibleLanguageProvider[]{
              new UpsideDownLanguageProvider(gen, modid)
        };
    }

    @Nonnull
    @Override
    public String getName() {
        return super.getName() + ": " + modid;
    }

    protected void add(IHasTranslationKey key, String value) {
        if (key instanceof IBlockProvider) {
            Block block = ((IBlockProvider) key).getBlock();
            if (Attribute.has(block, AttributeGui.class)) {
                add(Util.makeTranslationKey("container", block.getRegistryName()), value);
            }
        }
        add(key.getTranslationKey(), value);
    }

    protected void addFluid(FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> fluidRO, String name) {
        add(fluidRO.getStillFluid().getAttributes().getTranslationKey(), name);
        add(fluidRO.getFlowingFluid().getAttributes().getTranslationKey(), "Flowing " + name);
        add(fluidRO.getBlock(), name);
        add(fluidRO.getBucket(), name + " Bucket");
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