package mekanism.common.integration.computer;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thiakil.yamlops.SnakeYamlOps;
import com.thiakil.yamlops.YamlHelper;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.common.MekanismDataGenerator;
import mekanism.common.MekanismDataGenerator.IOConsumer;
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
import mekanism.common.tile.factory.TileEntityItemStackChemicalToItemStackFactory;
import mekanism.common.tile.laser.TileEntityBasicLaser;
import mekanism.common.tile.multiblock.TileEntityInductionPort;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.tile.qio.TileEntityQIOComponent;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler;
import mekanism.common.util.MekCodecs;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.CsvOutput.Builder;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Node;

@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class ComputerHelpProvider implements DataProvider {

    private static final String[] METHOD_CSV_HEADERS = {"Class", "Method Name", "Params", "Returns", "Restriction", "Requires Public Security", "Description"};
    private static final String[] ENUM_CSV_HEADERS = {"Type Name", "Values"};
    private final CompletableFuture<HolderLookup.Provider> registries;
    private final PackOutput.PathProvider pathProvider;
    private final String modid;

    public ComputerHelpProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modid) {
        this.modid = modid;
        this.registries = registries;
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "computer_help");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return this.registries.thenCompose(lookupProvider -> {
            //Manually initialize the computer method factory registry
            FactoryRegistry.load();

            //gather common data
            Map<Class<?>, List<MethodHelpData>> helpData = FactoryRegistry.getHelpData();
            Map<Class<?>, List<String>> enumValues = getEnumValues(helpData);

            //generate
            return CompletableFuture.allOf(
                  makeJson(output, lookupProvider, helpData, METHODS_DATA_CODEC, "methods"),
                  makeJekyllData(output, helpData, enumValues),
                  makeMethodsCsv(output, helpData),
                  makeJson(output, lookupProvider, enumValues, ENUMS_CODEC, "enums"),
                  makeEnumsCsv(output, enumValues)
            );
        });
    }

    @NotNull
    private <DATA> CompletableFuture<?> makeJson(CachedOutput output, HolderLookup.Provider lookupProvider, DATA helpData, Codec<DATA> codec, String path) {
        return DataProvider.saveStable(output, lookupProvider, codec, helpData, this.pathProvider.json(ResourceLocation.fromNamespaceAndPath(this.modid, path)));
    }

    @NotNull
    private CompletableFuture<?> makeJekyllData(CachedOutput output, Map<Class<?>, List<MethodHelpData>> methods, Map<Class<?>, List<String>> enumValues) {
        return CompletableFuture.runAsync(() -> {
            JekyllData jekyllData = new JekyllData(methods, enumValues, BaseComputerHelper.BUILTIN_TABLES.get());
            Node frontMatterNode = YamlHelper.sortMappingKeys(JekyllData.CODEC.encodeStart(new SnakeYamlOps(), jekyllData).getOrThrow(), Comparator.naturalOrder());
            MekanismDataGenerator.save(output, os -> {
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                    writer.write("---\n");
                    YamlHelper.dump(writer, frontMatterNode, YAML_OPTIONS);
                    writer.write("---\n");
                }
            }, this.pathProvider.file(ResourceLocation.fromNamespaceAndPath(this.modid, "jekyll"), "md")).join();
        });
    }

    @NotNull
    private CompletableFuture<?> makeMethodsCsv(CachedOutput pOutput, Map<Class<?>, List<MethodHelpData>> helpData) {
        return saveCSV(pOutput, this.pathProvider.file(ResourceLocation.fromNamespaceAndPath(this.modid, "methods"), "csv"), METHOD_CSV_HEADERS, output -> {
            //NB: list is used as the IOException will be captured in saveCSV
            List<Map.Entry<String, List<MethodHelpData>>> friendlyList = helpData.entrySet().stream()
                  .map(entry -> Map.entry(getFriendlyName(entry.getKey()), entry.getValue()))
                  .sorted(Entry.comparingByKey())
                  .toList();
            for (Map.Entry<String, List<MethodHelpData>> entry : friendlyList) {
                String friendlyClassName = entry.getKey();
                for (MethodHelpData method : entry.getValue()) {
                    output.writeRow(
                          friendlyClassName,
                          method.methodName(),
                          method.params() == null ? "" : method.params().stream().map(param -> param.name() + ": " + param.type()).collect(Collectors.joining(", ")),
                          csvReturnsValue(method.returns()),
                          method.restriction() == MethodRestriction.NONE ? "" : method.restriction().name(),
                          method.requiresPublicSecurity(),
                          method.description() == null ? "" : method.description()
                    );
                }
            }
        });
    }

    @NotNull
    private CompletableFuture<?> makeEnumsCsv(CachedOutput pOutput, Map<Class<?>, List<String>> enumValues) {
        //gather the enums into a sorted map
        return saveCSV(pOutput, this.pathProvider.file(ResourceLocation.fromNamespaceAndPath(this.modid, "enums"), "csv"), ENUM_CSV_HEADERS, csvOutput -> {
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
        for (List<MethodHelpData> methods : helpData.values()) {
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
        }
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

    //implementation isn't quite "stable" like json, input MUST be pre-sorted
    //the computer help uses TreeMaps to maintain order
    @SuppressWarnings("UnstableApiUsage")
    static CompletableFuture<?> saveCSV(CachedOutput pOutput, Path pPath, String[] headers, IOConsumer<CsvOutput> rowGenerator) {
        return CompletableFuture.runAsync(() -> {
            try (ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream()) {
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
        return FRIENDLY_NAME_CACHE.computeIfAbsent(clazz, clz -> {
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
                return "Machine with " + simpleName + " Component";
            }

            return splitClassName(simpleName);
        });
    }

    private static String splitClassName(String simpleName) {
        return CLASS_NAME_SPLITTER.splitAsStream(simpleName).collect(Collectors.joining(" "));
    }

    private static final Codec<Class<?>> CLASS_TO_FRIENDLY_NAME_CODEC = Codec.stringResolver(ComputerHelpProvider::getFriendlyName, p -> null);
    private static final Codec<Map<Class<?>, List<MethodHelpData>>> METHODS_DATA_CODEC = Codec.unboundedMap(CLASS_TO_FRIENDLY_NAME_CODEC, MethodHelpData.CODEC.listOf());
    private static final Codec<Map<Class<?>, List<String>>> ENUMS_CODEC = Codec.unboundedMap(MekCodecs.CLASS_TO_STRING_CODEC, Codec.STRING.listOf());

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
          Map.entry(TileEntityItemStackChemicalToItemStackFactory.class, "Compressing/Infusing/Injecting/Purifying Factory"),
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

    private static final DumperOptions YAML_OPTIONS = Util.make(new DumperOptions(), dop -> {
        dop.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dop.setLineBreak(DumperOptions.LineBreak.UNIX);
    });

    record JekyllData(Map<Class<?>, List<MethodHelpData>> methods, Map<Class<?>, List<String>> enums, Map<Class<?>, TableType> builtInTables) {

        static Codec<JekyllData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              METHODS_DATA_CODEC.fieldOf(SerializationConstants.METHODS).forGetter(JekyllData::methods),
              ENUMS_CODEC.fieldOf(SerializationConstants.ENUMS).forGetter(JekyllData::enums),
              TableType.TABLE_MAP_CODEC.fieldOf(SerializationConstants.BUILT_IN_TABLES).forGetter(JekyllData::builtInTables)
        ).apply(instance, JekyllData::new));
    }
}