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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.oredictionificator.OredictionificatorFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.integration.computer.MethodHelpData.Param;
import mekanism.common.integration.computer.MethodHelpData.Returns;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.factory.TileEntityItemStackGasToItemStackFactory;
import mekanism.common.tile.laser.TileEntityBasicLaser;
import mekanism.common.tile.multiblock.TileEntityInductionPort;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.tile.qio.TileEntityQIOComponent;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.CsvOutput.Builder;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class ComputerHelpProvider implements DataProvider {
    private static final String[] METHOD_CSV_HEADERS = {"Class", "Method Name", "Params", "Returns", "Restriction", "Requires Public Security", "Description"};
    private static final String[] ENUM_CSV_HEADERS = {"Type Name", "Values"};
    private final PackOutput.PathProvider pathProvider;
    private final String modid;

    public ComputerHelpProvider(PackOutput output, String modid) {
        this.modid = modid;
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "computer_help");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        FactoryRegistry.load();
        Map<Class<?>, List<MethodHelpData>> helpData = FactoryRegistry.getHelpData();
        Map<Class<?>, List<String>> enumValues = getEnumValues(helpData);
        return CompletableFuture.allOf(
              makeMethodsJson(pOutput, helpData),
              makeMethodsCsv(pOutput, helpData),
              makeEnumsCsv(pOutput, enumValues),
              makeEnumsJson(pOutput, enumValues)
        );
    }

    @NotNull
    private CompletableFuture<?> makeMethodsJson(CachedOutput pOutput, Map<Class<?>, List<MethodHelpData>> helpData) {
        JsonElement jsonElement = METHODS_DATA_CODEC.encodeStart(JsonOps.INSTANCE, helpData).getOrThrow(false, e -> {
            throw new RuntimeException(e);
        });
        return DataProvider.saveStable(pOutput, jsonElement, this.pathProvider.json(new ResourceLocation(this.modid, "methods")));
    }

    @NotNull
    private CompletableFuture<?> makeEnumsJson(CachedOutput pOutput, Map<Class<?>, List<String>> enumValues) {
        JsonElement jsonElement = ENUMS_CODEC.encodeStart(JsonOps.INSTANCE, enumValues).getOrThrow(false, e -> {
            throw new RuntimeException(e);
        });
        return DataProvider.saveStable(pOutput, jsonElement, this.pathProvider.json(new ResourceLocation(this.modid, "enums")));
    }

    @NotNull
    private CompletableFuture<?> makeMethodsCsv(CachedOutput pOutput, Map<Class<?>, List<MethodHelpData>> helpData) {
        return saveCSV(pOutput, this.pathProvider.file(new ResourceLocation(this.modid, "methods"), "csv"), METHOD_CSV_HEADERS, output -> {
            List<Pair<String, List<MethodHelpData>>> friendlyList = helpData.entrySet().stream().map(entry -> Pair.of(getFriendlyName(entry.getKey()), entry.getValue())).sorted(Entry.comparingByKey()).toList();
            for (Pair<String, List<MethodHelpData>> entry : friendlyList) {
                String friendlyClassName = entry.getKey();
                List<MethodHelpData> methods = entry.getValue();
                for (MethodHelpData method : methods) {
                    output.writeRow(
                          friendlyClassName,
                          method.methodName(),
                          method.params() != null ? method.params().stream().map(param -> param.name()+": "+param.type()).collect(Collectors.joining(", ")) : "",
                          csvReturnsValue(method.returns()),
                          method.restriction() != MethodRestriction.NONE ? method.restriction().name() : "",
                          method.requiresPublicSecurity(),
                          method.description() != null ? method.description() : ""
                    );
                }
            }
        });
    }

    @NotNull
    private CompletableFuture<?> makeEnumsCsv(CachedOutput pOutput, Map<Class<?>, List<String>> enumValues) {
        //gather the enums into a sorted map
        return saveCSV(pOutput, this.pathProvider.file(new ResourceLocation(this.modid, "enums"), "csv"), ENUM_CSV_HEADERS, csvOutput ->{
            for (Entry<Class<?>, List<String>> entry : enumValues.entrySet()) {
                Class<?> clazz = entry.getKey();
                List<String> values = entry.getValue();
                csvOutput.writeRow(clazz.getSimpleName(), String.join(", ", values));
            }
        });
    }

    @NotNull
    private static Map<Class<?>, List<String>> getEnumValues(Map<Class<?>, List<MethodHelpData>> helpData) {
        Map<Class<?>, List<String>> enumToValues = new TreeMap<>(Comparator.comparing(Class::getSimpleName));
        helpData.forEach((unused, methods)->{
            for (MethodHelpData method : methods) {
                if (method.returns().javaType().isEnum()) {
                    Class<?> jType = method.returns().javaType();
                    enumToValues.put(jType, method.returns().values());
                }
                for (Class<?> extraClass : method.returns().javaExtra()) {
                    if (extraClass.isEnum()) {
                        enumToValues.put(extraClass, MethodHelpData.getEnumConstantNames(extraClass));
                    }
                }
                if (method.params() != null) {
                    for (Param param : method.params()) {
                        if (param.values() != null) {
                            enumToValues.put(param.javaType(), param.values());
                        }
                    }
                }
            }
        });
        return enumToValues;
    }

    private static String csvReturnsValue(Returns returns) {
        if (returns == Returns.NOTHING) {
            return "";
        }
        return returns.type();
    }

    @Override
    public String getName() {
        return "ComputerHelp: " + modid;
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

    private static String getFriendlyName(Class<?> clazz) {
        return FRIENDLY_NAME_CACHE.computeIfAbsent(clazz, clz->{
            if (FRIENDLY_NAMES.containsKey(clz)) {
                return FRIENDLY_NAMES.get(clz);
            }
            String simpleName = clz.getSimpleName();
            if (simpleName.startsWith("TileEntity")) {
                simpleName = simpleName.substring(10);
            }
            if (simpleName.endsWith("MultiblockData")) {
                simpleName = simpleName.substring(0, simpleName.indexOf("MultiblockData"));
                simpleName = MULTIBLOCK_DATA_NAMES.getOrDefault(simpleName, splitClassName(simpleName));
                return simpleName + " Multiblock (formed)";
            }
            if (simpleName.startsWith("TileComponent")) {
                simpleName = splitClassName(simpleName.substring(13));
                if ("Config".equals(simpleName)) {
                    simpleName = "Side Configuration";
                }
                return "Machine with "+simpleName+" Component";
            }

            return splitClassName(simpleName);
        });
    }

    private static String splitClassName(String simpleName) {
        return CLASS_NAME_SPLITTER.splitAsStream(simpleName).collect(Collectors.joining(" "));
    }

    @FunctionalInterface
    interface IOConsumer<T> {
        void accept(T value) throws IOException;
    }

    private static final Codec<Class<?>> CLASS_TO_FRIENDLY_NAME_CODEC = ExtraCodecs.stringResolverCodec(ComputerHelpProvider::getFriendlyName, p->null);
    private static final Codec<Map<Class<?>, List<MethodHelpData>>> METHODS_DATA_CODEC = Codec.unboundedMap(CLASS_TO_FRIENDLY_NAME_CODEC, MethodHelpData.CODEC.listOf());
    private static final Codec<Map<Class<?>, List<String>>> ENUMS_CODEC = Codec.unboundedMap(MethodHelpData.CLASS_TO_STRING_CODEC, Codec.STRING.listOf());

    private static final Map<Class<?>, String> FRIENDLY_NAMES = Map.ofEntries(
          Map.entry(IFilter.class, "Filter Wrapper"),
          Map.entry(IItemStackFilter.class, "Filter Wrapper (ItemStack)"),
          Map.entry(IModIDFilter.class, "Filter Wrapper (Mod Id)"),
          Map.entry(ITagFilter.class, "Filter Wrapper (Tag)"),
          Map.entry(MinerFilter.class, "Filter Wrapper (Digital Miner)"),
          Map.entry(OredictionificatorFilter.class, "Filter Wrapper (Oredictionificator)"),
          Map.entry(OredictionificatorItemFilter.class, "Filter Wrapper (Oredictionificator Item)"),
          Map.entry(QIOFilter.class, "Filter Wrapper (QIO)"),
          Map.entry(SorterFilter.class, "Filter Wrapper (Logistical Sorter)"),
          Map.entry(ComputerEnergyHelper.class, "API Global: computerEnergyHelper"),
          Map.entry(ComputerFilterHelper.class, "API Global: computerFilterHelper"),
          Map.entry(MultiblockData.class, "Multiblock (formed)"),
          Map.entry(TileEntityMekanism.class, "Generic Mekanism Machine"),
          Map.entry(TileEntityFactory.class, "Factory Machine"),
          Map.entry(TileEntityBasicLaser.class, "Laser"),
          Map.entry(TileEntityInductionPort.class, "Induction Matrix Port"),
          Map.entry(TileEntityMultiblock.class, "Multiblock"),
          Map.entry(TileEntityProgressMachine.class, "Machine with Recipe Progress"),
          Map.entry(TileEntityQIOComponent.class, "QIO Machine"),
          Map.entry(TileEntityAdvancedElectricMachine.class, "Compressing/Injecting/Purifying Machine"),
          Map.entry(TileEntityItemStackGasToItemStackFactory.class, "Compressing/Injecting/Purifying Factory"),
          Map.entry(TileEntityQIOFilterHandler.class, "QIO Machine with Filter")
    );

    private static final Map<String, String> MULTIBLOCK_DATA_NAMES = Map.of(
          "Boiler", "Boiler",
          "Evaporation", "Thermal Evaporation",
          "Matrix", "Induction Matrix",
          "Tank", "Dynamic Tank",
          "Turbine", "Industrial Turbine"
    );

    private static final Map<Class<?>, String> FRIENDLY_NAME_CACHE = new HashMap<>();

    private static final Pattern CLASS_NAME_SPLITTER = Pattern.compile("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
}
