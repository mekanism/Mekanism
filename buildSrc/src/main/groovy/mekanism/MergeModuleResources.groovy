package mekanism

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternFilterable

import java.util.function.BinaryOperator

abstract class MergeModuleResources extends MergeServiceTask {

    private static Closure<PatternFilterable> atlasFilter = { PatternFilterable pf -> pf.include('**/assets/*/atlases/**/*.json') }
    private static Closure<PatternFilterable> dataMapFilter = { PatternFilterable pf -> pf.include('**/data/*/data_maps/**/*.json') }
    private static Closure<PatternFilterable> tagFilter = { PatternFilterable pf -> pf.include('**/data/*/tags/**/*.json') }

    @InputFiles
    abstract FileCollection resources
    @InputFiles
    abstract FileCollection annotationGenerated

    @OutputDirectory
    final Provider<Directory> generatedAssets
    @OutputDirectory
    final Provider<Directory> generatedData
    @OutputFile
    final Provider<RegularFile> pathsToExclude

    MergeModuleResources() {
        //Output directories we care about and produce
        generatedAssets = generatedDir.dir('assets')
        generatedData = generatedDir.dir('data')

        pathsToExclude = generatedDir.file('pathsToExclude.txt')
    }

    @TaskAction
    protected void merge() {
        //TODO: Ideally we would have something like ProcessResources does with StaleOutputCleaner to remove any files we previously created
        // by merging that we no longer need. Currently for now this technically isn't a problem as we specifically only include files that
        // we have written via pathsToExclude, but it would be better to be able to clean up after ourselves
        // Theoretically if we read an existing value of pathsToExclude we could then check the difference and delete any files that are no
        // longer being written?
        def toExclude = []
        mergeBasic(toExclude, resources, 'META-INF/accesstransformer.cfg', (text, fileText) -> text + '\n' + fileText)
        //Add all but the first four lines (which are duplicated between the files)
        mergeBasic(toExclude, resources, 'META-INF/neoforge.mods.toml', (text, fileText) -> text + '\n' + fileText.split('\n', 3)[2])

        mergeAtlases(toExclude)
        mergeDataMaps(toExclude)
        mergeTags(toExclude)
        //Include things that might be generated as classes in addition to our normal resources
        mergeServices(toExclude, resources + annotationGenerated)

        String text = ''
        for (def path : toExclude) {
            text = text.isEmpty() ? path : text + '\n' + path
        }
        pathsToExclude.get().asFile.text = text
    }

    private void mergeBasic(List<String> toExclude, FileCollection files, String name, BinaryOperator<String> appender) {
        String text = ''
        for (def file : files.asFileTree.matching({ PatternFilterable pf -> pf.include(name) })) {
            text = text.isEmpty() ? file.text : appender.apply(text, file.text)
        }
        writeOutputFile(toExclude, name, text)
    }

    private void mergeAtlases(List<String> toExclude) {
        BinaryOperator<Object> merger = (a, b) -> {
            a.sources += b.sources
            return a
        }
        //Go through the reverse atlas lookup and if there are multiple sourceSets that contain the same atlas
        // properly merge that atlas
        for (def entry : getReverseLookup(atlasFilter, resources, 'assets').entrySet()) {
            mergeSimpleJson(toExclude, entry.key, entry.value, merger)
        }
    }

    private void mergeDataMaps(List<String> toExclude) {
        //Go through the reverse atlas lookup and if there are multiple sourceSets that contain the same atlas
        // properly merge that atlas
        for (def entry : getReverseLookup(dataMapFilter, resources, 'data').entrySet()) {
            mergeDataMapJson(toExclude, entry.key, entry.value)
        }
    }

    private void mergeTags(List<String> toExclude) {
        BinaryOperator<Object> merger = (a, b) -> {
            a.values += b.values
            return a
        }
        //Go through the reverse tag index and if there are multiple sourceSets that contain the same tag
        // properly merge that tag
        for (def entry : getReverseLookup(tagFilter, resources, 'data').entrySet()) {
            mergeSimpleJson(toExclude, entry.key, entry.value, merger)
        }
    }

    private void mergeSimpleJson(List<String> toExclude, String outputPath, List<String> paths, BinaryOperator<Object> appender) {
        //logger.quiet('{} appeared {} times', outputPath, paths.size())
        if (paths.size() < 2) {
            //Skip any there is only a single element for
            return
        }
        Object outputAsJson = null

        for (def file : objectFactory.fileCollection().from(paths)) {
            Object json = new JsonSlurper().parse(file)
            if (outputAsJson == null) {
                outputAsJson = json
            } else {
                outputAsJson = appender.apply(outputAsJson, json)
            }
        }
        if (outputAsJson != null) {
            writeOutputFile(toExclude, outputPath, JsonOutput.toJson(outputAsJson))
        }
    }

    private void mergeDataMapJson(List<String> toExclude, String outputPath, List<String> paths) {
        //logger.quiet('{} appeared {} times', outputPath, paths.size())
        if (paths.size() < 2) {
            //Skip any there is only a single element for
            return
        }
        Object outputAsJson = null

        for (def file : objectFactory.fileCollection().from(paths)) {
            Object json = new JsonSlurper().parse(file)
            if (outputAsJson == null) {
                outputAsJson = json
            } else {
                for (def values : json.values) {
                    def target = values.getKey()
                    def existing = outputAsJson.values[target]
                    def value = values.getValue()
                    if (existing == null) {
                        outputAsJson.values[target] = value
                    } else if (existing instanceof List) {
                        if (value instanceof List) existing.addAll(value)
                        else existing.add(value)
                    } else {
                        if (value instanceof List) {
                            value.add(existing)
                        }
                        outputAsJson.values[target] = value
                    }
                }
            }
        }
        if (outputAsJson != null) {
            writeOutputFile(toExclude, outputPath, JsonOutput.toJson(outputAsJson))
        }
    }
}