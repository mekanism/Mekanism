package mekanism.common.integration.computer;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.common.integration.computer.MethodHelpData.Param;
import mekanism.common.integration.computer.MethodHelpData.Returns;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.CsvOutput.Builder;
import net.minecraftforge.common.data.ExistingFileHelper;

@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class ComputerHelpProvider implements DataProvider {
    private static final String[] CSV_HEADERS = new String[]{"Class", "Method Name", "Params", "Returns", "Restriction", "Requires Public Security", "Description"};
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
        return CompletableFuture.allOf(
              DataProvider.saveStable(pOutput, element, this.pathProvider.json(new ResourceLocation(this.modid, "all"))),
              saveCSV(pOutput, this.pathProvider.file(new ResourceLocation(this.modid, "all"), "csv"), CSV_HEADERS, output -> {
                  for (Entry<Class<?>, List<MethodHelpData>> entry : helpData.entrySet()) {
                      Class<?> clazz = entry.getKey();
                      List<MethodHelpData> methods = entry.getValue();
                      for (MethodHelpData method : methods) {
                          output.writeRow(
                                clazz.getSimpleName(),
                                method.methodName(),
                                method.params() != null ? method.params().stream().map(Param::name).collect(Collectors.joining(", ")) : "",
                                csvReturnsValue(method.returns()),
                                method.restriction() != MethodRestriction.NONE ? method.restriction().name() : "",
                                method.requiresPublicSecurity(),
                                method.description() != null ? method.description() : ""
                          );
                      }
                  }
              })
        );
    }

    private static String csvReturnsValue(Returns returns) {
        if (returns == Returns.NOTHING) {
            return "";
        }
        if (returns.values() == null) {
            return returns.type();
        }
        return returns.javaType().getSimpleName()+" (\"" + String.join("\", \"", returns.values()) + "\")";
    }

    @Override
    public String getName() {
        return "ComputerHelp:" + modid;
    }

    //implementation isn't quite "stable" like json, but input is always sorted
    @SuppressWarnings("UnstableApiUsage")
    static CompletableFuture<?> saveCSV(CachedOutput pOutput, Path pPath, String[] headers, IOConsumer<CsvOutput> rowGenerator) {
        return CompletableFuture.runAsync(() -> {
            try {
                ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
                HashingOutputStream hashingoutputstream = new HashingOutputStream(Hashing.sha1(), bytearrayoutputstream);

                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(hashingoutputstream, StandardCharsets.UTF_8))) {
                    Builder builder = CsvOutput.builder();
                    for (String header : headers) {
                        builder.addColumn(header);
                    }
                    rowGenerator.accept(builder.build(writer));
                }

                pOutput.writeIfNeeded(pPath, bytearrayoutputstream.toByteArray(), hashingoutputstream.hash());
            } catch (IOException ioexception) {
                LOGGER.error("Failed to save file to {}", pPath, ioexception);
            }

        }, Util.backgroundExecutor());
    }

    @FunctionalInterface
    interface IOConsumer<T> {
        void accept(T value) throws IOException;
    }

    private static final Codec<Map<Class<?>, List<MethodHelpData>>> JSONCODEC = Codec.unboundedMap(MethodHelpData.CLASS_TO_STRING_CODEC, MethodHelpData.CODEC.listOf());
}
