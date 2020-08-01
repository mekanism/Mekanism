package mekanism.common.config.value;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import mekanism.common.config.IMekanismConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CachedResourceLocationListValue extends CachedResolvableConfigValue<List<ResourceLocation>, List<? extends String>> {

    private CachedResourceLocationListValue(IMekanismConfig config, ConfigValue<List<? extends String>> internal) {
        super(config, internal);
    }

    public static CachedResourceLocationListValue wrap(IMekanismConfig config, ConfigValue<List<? extends String>> internal) {
        return new CachedResourceLocationListValue(config, internal);
    }

    @Override
    protected List<ResourceLocation> resolve(List<? extends String> encoded) {
        //We ignore any strings that are invalid resource locations
        // validation should have happened before we got here, but in case something went wrong we don't want to crash and burn
        return encoded.stream().map(s -> ResourceLocation.tryCreate(s.toLowerCase(Locale.ROOT))).filter(Objects::nonNull).collect(Collectors.toCollection(() -> new ArrayList<>(encoded.size())));
    }

    @Override
    protected List<? extends String> encode(List<ResourceLocation> values) {
        return values.stream().map(ResourceLocation::toString).collect(Collectors.toCollection(() -> new ArrayList<>(values.size())));
    }
}