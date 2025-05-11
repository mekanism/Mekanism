package mekanism

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternFilterable

import javax.inject.Inject
import java.util.function.BinaryOperator

abstract class MergeModuleResources extends DefaultTask {

    private static Closure<PatternFilterable> atlasFilter = { PatternFilterable pf -> pf.include('**/assets/*/atlases/**/*.json') }
    private static Closure<PatternFilterable> tagFilter = { PatternFilterable pf -> pf.include('**/data/*/tags/**/*.json') }
    static Closure<PatternFilterable> serviceFilter = { PatternFilterable pf -> pf.include('**/META-INF/services/*') }

    @Internal
    final DirectoryProperty generatedDir

    @InputFiles
    abstract FileCollection resources
    @InputFiles
    abstract FileCollection annotationGenerated

    @OutputDirectory
    final Provider<Directory> generatedAssets
    @OutputDirectory
    final Provider<Directory> generatedData
    @OutputDirectory
    final Provider<Directory> generatedMetaInf
    @OutputFile
    final Provider<RegularFile> pathsToExclude

    MergeModuleResources() {
        generatedDir = objectFactory.directoryProperty().convention(projectLayout.buildDirectory.dir('generated'))
        //Output directories we care about and produce
        generatedAssets = generatedDir.dir('assets')
        generatedData = generatedDir.dir('data')
        generatedMetaInf = generatedDir.dir('META-INF')

        pathsToExclude = generatedDir.file('pathsToExclude.txt')
    }

    @Inject
    protected abstract ObjectFactory getObjectFactory()

    @Inject
    protected abstract ProjectLayout getProjectLayout()

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
        mergeBasic(toExclude, resources, 'META-INF/neoforge.mods.toml', (text, fileText) -> text + '\n' + fileText.split('\n', 5)[4])

        mergeAtlases(toExclude)
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

    private Map<String, List<String>> getReverseLookup(Closure<PatternFilterable> filter, FileCollection files, String prefix) {
        Map<String, List<String>> reverseLookup = [:]
        for (def file : files.asFileTree.matching(filter)) {
            //Add the sourceSet to the reverse lookup
            def split = file.path.split(prefix, 2)
            if (split.length == 2) {
                //Theoretically this should always be the case
                String trimmedPath = prefix + split[1]
                def paths = reverseLookup[trimmedPath]
                if (paths == null) {
                    reverseLookup[trimmedPath] = paths = []
                }
                paths.add(file.path)
            } else {
                logger.error('Failed to split path "{}" by prefix: "{}"', file.path, prefix)
            }
        }
        return reverseLookup
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

    private void mergeServices(List<String> toExclude, FileCollection files) {
        for (def entry : getReverseLookup(serviceFilter, files, 'META-INF').entrySet()) {
            mergeSimpleLines(toExclude, entry.key, entry.value)
        }
    }

    private void mergeSimpleLines(List<String> toExclude, String outputPath, List<String> paths) {
        //logger.quiet('{} appeared {} times', outputPath, paths.size())
        if (paths.size() < 2) {
            //Skip any there is only a single element for
            return
        }
        String text = ''
        for (def file : objectFactory.fileCollection().from(paths)) {
            text = text.isEmpty() ? file.text : text + '\n' + file.text
        }
        writeOutputFile(toExclude, outputPath, text)
    }

    private void writeOutputFile(List<String> toExclude, String outputPath, String text) {
        toExclude.add(outputPath)
        def outputFile = generatedDir.file(outputPath).get().asFile
        //Make all parent directories needed
        outputFile.parentFile.mkdirs()
        outputFile.text = text
    }
}