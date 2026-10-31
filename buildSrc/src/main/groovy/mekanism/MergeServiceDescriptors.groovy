package mekanism

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

abstract class MergeServiceDescriptors extends MergeServiceTask {

    @InputFiles
    abstract FileCollection resources

    @TaskAction
    protected void merge() {
        def toExclude = []
        mergeServices(toExclude, resources)
    }
}