package mekanism

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class MergeServiceDescriptors extends MergeServiceTask {

    @InputFiles
    abstract FileCollection resources

    @OutputDirectory
    final DirectoryProperty getOutputDirectory() {
        return generatedDir
    }

    @TaskAction
    protected void merge() {
        def toExclude = []
        mergeServices(toExclude, resources)
    }
}