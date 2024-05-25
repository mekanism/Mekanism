package mekanism

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.util.PatternFilterable

import java.util.function.BinaryOperator
import java.util.function.Function

class MergeJars {
    static Closure<PatternFilterable> atlasFilter = { PatternFilterable pf -> pf.include('**/assets/*/atlases/**/*.json') }
    static Closure<PatternFilterable> serviceFilter = { PatternFilterable pf -> pf.include('**/META-INF/services/*') }
    static Closure<PatternFilterable> tagFilter = { PatternFilterable pf -> pf.include('**/data/*/tags/**/*.json') }
    static Function<SourceSet, Set<File>> resourceLister = (SourceSet sourceSet) -> sourceSet.resources.srcDirs
    static Function<SourceSet, Set<File>> annotationGeneratedLister = (SourceSet sourceSet) -> {
        Set<File> combined = new HashSet<>(sourceSet.resources.srcDirs)
        //Include things that might be generated as classes in addition to our normal resources
        combined.addAll(sourceSet.output.getClassesDirs().files)
        return combined
    }

    static List<String> getGeneralPathsToExclude(Project project, List<SourceSet> sourceSets) {
        List<String> toExclude = new ArrayList<>()
        toExclude.add('META-INF/neoforge.mods.toml')
        toExclude.add('META-INF/accesstransformer.cfg')
        //This file doesn't exist until compile time
        toExclude.add('META-INF/services/mekanism.common.integration.computer.IComputerMethodRegistry')
        addDuplicates(project, atlasFilter, sourceSets, toExclude)
        addDuplicates(project, tagFilter, sourceSets, toExclude)
        addDuplicates(project, serviceFilter, sourceSets, toExclude, annotationGeneratedLister)
        return toExclude
    }

    private static addDuplicates(Project project, Closure<PatternFilterable> filter, List<SourceSet> sourceSets, toExclude) {
        addDuplicates(project, filter, sourceSets, toExclude, resourceLister)
    }

    private static addDuplicates(Project project, Closure<PatternFilterable> filter, List<SourceSet> sourceSets, toExclude, Function<SourceSet, Set<File>> fileLister) {
        getReverseLookup(project, filter, sourceSets, fileLister).each { name, paths ->
            if (paths.size() > 1) {
                toExclude.add(name.substring(1))
            }
        }
    }

    static Closure<CopySpec> createExcludeClosure(List<String> baseExcludeData, String... extraExclusions) {
        List<String> toExcludeFromAll = new ArrayList<>(baseExcludeData)
        for (String extraExclusion : extraExclusions) {
            toExcludeFromAll.add(extraExclusion)
        }
        return { CopySpec c ->
            c.exclude(toExcludeFromAll)
        }
    }

    static void merge(Project project, List<SourceSet> sourceSets) {
        //Generate folders, merge the access transformers and neoforge.mods.toml files
        project.mkdir(project.layout.buildDirectory.dir('generated/META-INF'))
        mergeBasic(project, sourceSets, 'META-INF/accesstransformer.cfg', (text, fileText) -> text + "\n" + fileText)
        mergeModsTOML(project, sourceSets)
        //Delete the data directory so that we don't accidentally leak bad old data into it
        project.file(project.layout.buildDirectory.dir('generated/assets')).deleteDir()
        project.file(project.layout.buildDirectory.dir('generated/data')).deleteDir()
        project.file(project.layout.buildDirectory.dir('generated/META-INF/services')).deleteDir()
        //And then recreate the directory so we can put stuff in it
        project.mkdir(project.layout.buildDirectory.dir('generated/assets'))
        project.mkdir(project.layout.buildDirectory.dir('generated/data'))
        project.mkdir(project.layout.buildDirectory.dir('generated/META-INF/services'))
        mergeAtlases(project, sourceSets)
        mergeTags(project, sourceSets)
        mergeServices(project, sourceSets)
    }

    static List<Closure> getGeneratedClosures(Map<String, ?> versionProperties) {
        List<Closure> generated = new ArrayList<>()
        generated.add({ CopySpec c ->
            c.include('META-INF/neoforge.mods.toml')
            c.expand(versionProperties)
        })
        generated.add({ CopySpec c ->
            c.include('META-INF/accesstransformer.cfg', 'META-INF/services/*', 'assets/**', 'data/**')
        })
        return generated
    }

    private static void mergeModsTOML(Project project, List<SourceSet> sourceSets) {
        mergeBasic(project, sourceSets, 'META-INF/neoforge.mods.toml', (text, fileText) -> {
            //Add all but the first four lines (which are duplicated between the files)
            String[] lines = fileText.split("\n")
            for (int i = 4; i < lines.length; i++) {
                text = text + "\n" + lines[i]
            }
            return text
        })
    }

    private static void mergeBasic(Project project, List<SourceSet> sourceSets, String name, BinaryOperator<String> appender) {
        String text = ""
        for (SourceSet sourceSet : sourceSets) {
            sourceSet.resources.matching { PatternFilterable pf ->
                pf.include(name)
            }.each { file ->
                text = text.isEmpty() ? file.getText() : appender.apply(text, file.getText())
            }
        }
        writeOutputFile(project, '/' + name, text)
    }

    private static Map<String, List<String>> getReverseLookup(Project project, Closure<PatternFilterable> filter, List<SourceSet> sourceSets) {
        return getReverseLookup(project, filter, sourceSets, resourceLister)
    }

    private static Map<String, List<String>> getReverseLookup(Project project, Closure<PatternFilterable> filter, List<SourceSet> sourceSets, Function<SourceSet, Set<File>> fileLister) {
        Map<String, List<String>> reverseLookup = new HashMap<>()
        for (SourceSet sourceSet : sourceSets) {
            fileLister.apply(sourceSet).each { srcDir ->
                int srcDirPathLength = srcDir.getPath().length()
                project.fileTree(srcDir).matching(filter).each { file ->
                    //Add the sourceSet to the reverse lookup
                    String path = file.getPath()
                    String trimmedPath = path.substring(srcDirPathLength)
                    if (!reverseLookup.containsKey(trimmedPath)) {
                        reverseLookup.put(trimmedPath, new ArrayList<>())
                    }
                    reverseLookup.get(trimmedPath).add(path)
                }
            }
        }
        return reverseLookup
    }

    private static void mergeAtlases(Project project, List<SourceSet> sourceSets) {
        Map<String, List<String>> reverseAtlasLookup = getReverseLookup(project, atlasFilter, sourceSets)
        //Go through the reverse atlas lookup and if there are multiple sourceSets that contain the same atlas
        // properly merge that atlas
        reverseAtlasLookup.each { atlas, atlasPaths ->
            mergeSimpleJson(project, atlas, atlasPaths, (a, b) -> {
                a.sources += b.sources
                return a
            })
        }
    }

    private static void mergeTags(Project project, List<SourceSet> sourceSets) {
        Map<String, List<String>> reverseTags = getReverseLookup(project, tagFilter, sourceSets)
        //Go through the reverse tag index and if there are multiple sourceSets that contain the same tag
        // properly merge that tag
        reverseTags.each { tag, tagPaths ->
            mergeSimpleJson(project, tag, tagPaths, (a, b) -> {
                a.values += b.values
                return a
            })
        }
    }

    private static void mergeSimpleJson(Project project, String outputPath, List<String> paths, BinaryOperator<Object> appender) {
        //println(outputPath + " appeared " + paths.size() + " times")
        if (paths.size() < 2) {
            //Skip any there is only a single element for
            return
        }
        Object outputAsJson = null
        paths.each { path ->
            Object json = new JsonSlurper().parse(project.file(path))
            if (outputAsJson == null) {
                outputAsJson = json
            } else {
                outputAsJson = appender.apply(outputAsJson, json)
            }
        }
        if (outputAsJson != null) {
            writeOutputFile(project, outputPath, JsonOutput.toJson(outputAsJson))
        }
    }

    private static void mergeServices(Project project, List<SourceSet> sourceSets) {
        Map<String, List<String>> reverseServices = getReverseLookup(project, serviceFilter, sourceSets, annotationGeneratedLister)
        reverseServices.each { tag, tagPaths -> mergeSimpleLines(project, tag, tagPaths) }
    }

    private static void mergeSimpleLines(Project project, String outputPath, List<String> paths) {
        //println(outputPath + " appeared " + paths.size() + " times")
        if (paths.size() < 2) {
            //Skip any there is only a single element for
            return
        }
        String text = ""
        paths.each { path ->
            def file = project.file(path)
            text = text.isEmpty() ? file.getText() : text + "\n" + file.getText()
        }
        writeOutputFile(project, outputPath, text)
    }

    private static void writeOutputFile(Project project, String outputPath, String text) {
        File outputFile = project.file(project.layout.buildDirectory.file('generated/' + outputPath))
        //Make all parent directories needed
        outputFile.getParentFile().mkdirs()
        outputFile.text = text
    }
}