package mekanism

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.jvm.tasks.Jar

import javax.inject.Inject

abstract class AllJar extends Jar {

    //TODO: Should this be declared as some sort of input?
    @Internal
    final DirectoryProperty generatedDir

    @InputFile
    final RegularFileProperty pathsToExclude
    @InputFiles
    abstract FileCollection apiOutput
    @InputFiles
    abstract FileCollection mainOutput
    @InputFiles
    FileCollection secondaryModuleOutputs

    @Inject
    AllJar(ProjectLayout projectLayout) {
        generatedDir = objectFactory.directoryProperty().convention(projectLayout.buildDirectory.dir('generated'))
        pathsToExclude = objectFactory.fileProperty()
        secondaryModuleOutputs = objectFactory.fileCollection()
    }

    @Override
    @TaskAction
    protected void copy() {
        //copy all the files except for ones we are going to include from the merged intersection
        from(apiOutput)

        //Note: We have to use from(FileCollection) as we want to ensure the pathsToExclude gets lazily evaluated, trying to lazily configure it
        // via a closure causes an additional spec to be added to this task at execution time, which causes it to error out. The easiest way for
        // us to do it, is to turn collections into file trees, and then use a matching block on them as that allows it to lazily evaluate the
        // filter as the collection is defined to stay up to date if the backing collection changes
        from(filterFiles(mainOutput, 'crafttweaker_parameter_names.json'))
        from(filterFiles(secondaryModuleOutputs, 'logo.png', 'pack.mcmeta'))

        //And finally copy over the generated files
        from(generatedDir.asFileTree.matching({ PatternFilterable pf ->
            pf.include(pathsToExclude.get().asFile.text.split('\n'))
        }))

        super.copy()
    }

    private FileCollection filterFiles(FileCollection collection, String... extraExclusions) {
        return collection.asFileTree.matching({ PatternFilterable pf ->
            pf.exclude(pathsToExclude.get().asFile.text.split('\n'))
            pf.exclude(extraExclusions)
        })
    }
}