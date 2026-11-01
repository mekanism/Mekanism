package mekanism

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.util.PatternFilterable

import javax.inject.Inject

abstract class MergeServiceTask extends DefaultTask {

    static Closure<PatternFilterable> serviceFilter = { PatternFilterable pf -> pf.include('**/META-INF/services/*') }

    //Not an output itself as it defaults to the shared build/generated directory, which other tasks also write to
    @Internal
    final DirectoryProperty generatedDir

    @OutputDirectory
    final Provider<Directory> generatedMetaInf

    MergeServiceTask() {
        generatedDir = objectFactory.directoryProperty().convention(projectLayout.buildDirectory.dir('generated'))
        generatedMetaInf = generatedDir.dir('META-INF')
    }

    @Inject
    protected abstract ObjectFactory getObjectFactory()

    @Inject
    protected abstract ProjectLayout getProjectLayout()

    protected void mergeServices(List<String> toExclude, FileCollection files) {
        for (def entry : getReverseLookup(serviceFilter, files, 'META-INF').entrySet()) {
            mergeSimpleLines(toExclude, entry.key, entry.value)
        }
    }

    protected Map<String, List<String>> getReverseLookup(Closure<PatternFilterable> filter, FileCollection files, String prefix) {
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

    protected void mergeSimpleLines(List<String> toExclude, String outputPath, List<String> paths) {
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

    protected void writeOutputFile(List<String> toExclude, String outputPath, String text) {
        toExclude.add(outputPath)
        def outputFile = generatedDir.file(outputPath).get().asFile
        //Make all parent directories needed
        outputFile.parentFile.mkdirs()
        outputFile.text = text
    }
}
