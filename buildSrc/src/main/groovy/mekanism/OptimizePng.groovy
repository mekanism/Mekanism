package mekanism

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileType
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges

import javax.inject.Inject

abstract class OptimizePng extends DefaultTask {

    @InputFiles
    @Incremental
    final FileCollection inputFiles

    @Inject
    OptimizePng(ObjectFactory objects) {
        this.inputFiles = objects.fileCollection()
    }

    @Inject
    protected abstract ExecOperations getExecOperations()

    @TaskAction
    void execute(InputChanges inputChanges) {
        for (def fileChange : inputChanges.getFileChanges(inputFiles)) {
            if (fileChange.changeType == ChangeType.REMOVED || fileChange.fileType != FileType.FILE) {
                //Don't care about files that were removed
                continue
            }
            def file = fileChange.file
            //Minimize/optimize all png files, requires optipng on the PATH
            // Credits: BrainStone
            long size = file.length()
            execOperations.exec { spec -> spec.commandLine('optipng', '-q', '-o7', '-zm1-9', '-strip', 'all', file) }
            long newSize = file.length()
            if (newSize < size) {
                System.out.format(Locale.ROOT, 'Reduced File size of %s from %d bytes to %d bytes (reduced by %.2f%%)\n',
                        file, size, newSize, ((double) (size - newSize)) / ((double) size) * 100.0)
            }
        }
    }
}