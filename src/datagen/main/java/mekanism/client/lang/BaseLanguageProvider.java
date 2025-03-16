package mekanism.client.lang;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import mekanism.api.gear.ModuleData;
import mekanism.api.text.IHasTranslationKey;
import mekanism.client.lang.FormatSplitter.Component;
import mekanism.client.recipe_viewer.alias.IAliasedTranslation;
import mekanism.common.Mekanism;
import mekanism.common.advancements.MekanismAdvancement;
import mekanism.common.base.IModModule;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.config.IConfigTranslation;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.common.registration.impl.MekanismDamageType;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.jetbrains.annotations.NotNull;

public abstract class BaseLanguageProvider extends LanguageProvider {

    private final ConvertibleLanguageProvider[] altProviders;
    protected final String modName;
    protected final String basicModName;
    private final String modid;

    protected BaseLanguageProvider(PackOutput output, String modid) {
        this(output, modid, Mekanism.MOD_NAME);
    }

    protected BaseLanguageProvider(PackOutput output, String modid, IModModule module) {
        this(output, modid, Mekanism.MOD_NAME + ": " + module.getName());
    }

    private BaseLanguageProvider(PackOutput output, String modid, String modName) {
        super(output, modid, "en_us");
        this.modid = modid;
        this.modName = modName;
        this.basicModName = modName.replaceAll(":", "");
        altProviders = new ConvertibleLanguageProvider[]{
              new UpsideDownLanguageProvider(output, modid),
              new NonAmericanLanguageProvider(output, modid, "en_au"),
              new NonAmericanLanguageProvider(output, modid, "en_gb")
        };
    }

    protected void addPackData(IHasTranslationKey name, IHasTranslationKey packDescription) {
        add(name, modName);
        add(packDescription, "Resources used for " + modName);
    }

    protected void addModInfo(String description) {
        add("fml.menu.mods.info.description." + modid, description);
    }

    protected void addEntity(Holder<EntityType<?>> key, String value) {
        add(key.value().getDescriptionId(), value);
    }

    protected void addModuleConfig(ResourceLocation configKey, String value) {
        add(configKey.toLanguageKey("module"), value);
    }

    protected void addHolder(Holder<? extends IHasTranslationKey> key, String value) {
        add(key.value(), value);
    }

    protected void add(IHasTranslationKey key, String value) {
        if (key instanceof BlockRegistryObject<?, ?> blockHolder) {
            if (Attribute.matches(blockHolder, AttributeGui.class, attribute -> !attribute.hasCustomName())) {
                add(Util.makeDescriptionId("container", blockHolder.getId()), value);
            }
        }
        add(key.getTranslationKey(), value);
    }

    protected void add(BlockRegistryObject<?, ?> blockRO, String value, String containerName) {
        if (Attribute.matches(blockRO, AttributeGui.class, attribute -> !attribute.hasCustomName())) {
            add(Util.makeDescriptionId("container", blockRO.getId()), containerName);
            add(blockRO.getTranslationKey(), value);
        } else {
            throw new IllegalArgumentException(blockRO + " does not have a container name set.");
        }
    }

    protected void add(Holder<ModuleData<?>> moduleDataProvider, String name, String description) {
        ModuleData<?> moduleData = moduleDataProvider.value();
        add(moduleData.getTranslationKey(), name);
        add(moduleData.getDescriptionTranslationKey(), description);
    }

    protected void addFluid(FluidRegistryObject<?, ?, ?, ?, ?> fluidRO, String name) {
        add(fluidRO.getBlock(), name);
        add(fluidRO.getBucket(), name + " Bucket");
        add(ItemTags.create(Tags.Items.BUCKETS.location().withSuffix("/" + fluidRO.getName())), name + " Buckets");
    }

    protected void add(MekanismAdvancement advancement, String title, String description) {
        add(advancement.title(), title);
        add(advancement.description(), description);
    }

    protected void add(MekanismDamageType damageType, String value, String valueEscaping) {
        add(damageType, value);
        add(damageType.getTranslationKey() + ".player", valueEscaping);
    }

    private String getConfigSectionTranslationPath(IMekanismConfig config) {
        String baseConfigFolder = Mekanism.MOD_NAME.toLowerCase(Locale.ROOT);
        String fileName = config.getFileName().replaceAll("[^a-zA-Z0-9]+", ".").toLowerCase(Locale.ROOT);
        return modid + ".configuration.section." + baseConfigFolder + "." + fileName + ".toml";
    }

    protected void addConfigs(Collection<IMekanismConfig> configs) {
        add(modid + ".configuration.title", modName + " Config");
        for (IMekanismConfig config : configs) {
            String key = getConfigSectionTranslationPath(config);
            add(key, config.getTranslation());
            add(key + ".title", modName + " - " + config.getTranslation());
        }
    }

    protected void addConfigs(IConfigTranslation... translations) {
        for (IConfigTranslation translation : translations) {
            add(translation, translation.title());
            add(translation.getTranslationKey() + ".tooltip", translation.tooltip());
            String button = translation.button();
            if (button != null) {
                add(translation.getTranslationKey() + ".button", button);
            }
        }
    }

    protected void addAliases(IAliasedTranslation... translations) {
        for (IAliasedTranslation translation : translations) {
            add(translation, translation.getAlias());
        }
    }

    protected void addAlias(String path, String translation) {
        add(Util.makeDescriptionId("alias", ResourceLocation.fromNamespaceAndPath(modid, path)), translation);
    }

    @Override
    public void add(@NotNull String key, @NotNull String value) {
        if (value.contains("%s")) {
            throw new IllegalArgumentException("Values containing substitutions should use explicit numbered indices: " + key + " - " + value);
        }
        super.add(key, value);
        if (altProviders.length > 0) {
            List<Component> splitEnglish = FormatSplitter.split(value);
            for (ConvertibleLanguageProvider provider : altProviders) {
                provider.convert(key, value, splitEnglish);
            }
        }
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(@NotNull CachedOutput cache) {
        CompletableFuture<?> future = super.run(cache);
        if (altProviders.length > 0) {
            CompletableFuture<?>[] futures = new CompletableFuture[altProviders.length + 1];
            futures[0] = future;
            for (int i = 0; i < altProviders.length; i++) {
                futures[i + 1] = altProviders[i].run(cache);
            }
            return CompletableFuture.allOf(futures);
        }
        return future;
    }
}
