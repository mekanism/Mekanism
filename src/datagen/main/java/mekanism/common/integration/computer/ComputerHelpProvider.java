package mekanism.common.integration.computer;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class ComputerHelpProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final ExistingFileHelper existingFileHelper;
    private final String modid;

    public ComputerHelpProvider(PackOutput output, ExistingFileHelper existingFileHelper, String modid) {
        this.modid = modid;
        this.existingFileHelper = existingFileHelper;
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "computerHelp");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        FactoryRegistry.load();
        Map<Class<?>, List<MethodHelpData>> helpData = FactoryRegistry.getHelpData();
        JsonElement element = JSONCODEC.encodeStart(JsonOps.INSTANCE, helpData).getOrThrow(false, e->{throw new RuntimeException(e);});
        return DataProvider.saveStable(pOutput, element, this.pathProvider.json(new ResourceLocation(this.modid, "all")));
    }

    @Override
    public String getName() {
        return "Computer:" + modid;
    }


    private static final Codec<Map<Class<?>, List<MethodHelpData>>> JSONCODEC = Codec.unboundedMap(MethodHelpData.CLASS_TO_STRING_CODEC, MethodHelpData.CODEC.listOf());
}
