package neoforge

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

import java.util.regex.Matcher

/**
 * From <a href="https://github.com/neoforged/NeoForge/blob/26.1.x/buildSrc/src/main/groovy/neoforge.formatting-conventions.gradle">NeoForge</a>
 *
 * License: <a href="https://github.com/neoforged/NeoForge/blob/26.1.x/LICENSE.txt">LGPL 2.1</a>
 */
abstract class GeneratePackageInfos extends DefaultTask {
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract ConfigurableFileCollection getFiles();

    @TaskAction
    void generatePackageInfos() {
        getFiles().each { javaFile ->
            def packageInfoFile = new File(javaFile.parent, 'package-info.java')
            if (!packageInfoFile.exists()) {
                def pkgName = javaFile.toString().replaceAll(Matcher.quoteReplacement(File.separator), '/')
                //Note: Changed from neo's package to our package
                pkgName = pkgName.substring(pkgName.indexOf('mekanism/'), pkgName.lastIndexOf('/'))
                pkgName = pkgName.replaceAll('/', '.')

                def pkgInfoText = """
                    |@NullMarked
                    |package $pkgName;
                    |
                    |import org.jspecify.annotations.NullMarked;
                """.stripMargin().trim()

                packageInfoFile.text = pkgInfoText
            }
        }
    }
}