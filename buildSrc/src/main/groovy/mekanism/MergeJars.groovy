package mekanism

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.util.PatternFilterable

import java.util.function.BinaryOperator

class MergeJars {

    static List<String> getGeneralPathsToExclude(Project project) {
        List<String> toExclude = new ArrayList<>()
        toExclude.add('META-INF/mods.toml')
        toExclude.add('META-INF/accesstransformer.cfg')
        int baseLength = "$project.buildDir/generated/".length()
        project.fileTree(dir: "$project.buildDir/generated/assets/").each {
            File file -> toExclude.add(file.getPath().substring(baseLength))
        }
        project.fileTree(dir: "$project.buildDir/generated/data/").each {
            File file -> toExclude.add(file.getPath().substring(baseLength))
        }
        return toExclude
    }

    static Closure createExcludeClosure(List<String> baseExcludeData, String... extraExclusions) {
        List<String> toExcludeFromAll = new ArrayList<>(baseExcludeData)
        for (String extraExclusion : extraExclusions) {
            toExcludeFromAll.add(extraExclusion)
        }
        return { CopySpec c ->
            c.exclude(toExcludeFromAll)
        }
    }

    static void merge(Project project, SourceSet... sourceSets) {
        //Generate folders, merge the access transformers and mods.toml files
        project.mkdir("$project.buildDir/generated/META-INF")
        (new File("$project.buildDir/generated/META-INF/accesstransformer.cfg")).text = mergeATs(sourceSets)
        (new File("$project.buildDir/generated/META-INF/mods.toml")).text = mergeModsTOML(sourceSets)
        //Delete the data directory so that we don't accidentally leak bad old data into it
        project.file("$project.buildDir/generated/assets").deleteDir()
        project.file("$project.buildDir/generated/data").deleteDir()
        //And then recreate the directory so we can put stuff in it
        project.mkdir("$project.buildDir/generated/assets")
        project.mkdir("$project.buildDir/generated/data")
        mergeAtlases(project, sourceSets)
        mergeTags(project, sourceSets)
    }

    static List<Closure> getGeneratedClosures(Map<String, ?> versionProperties) {
        List<Closure> generated = new ArrayList<>()
        generated.add({ CopySpec c ->
            c.include('META-INF/mods.toml')
            c.expand(versionProperties)
        })
        generated.add({ CopySpec c ->
            c.include('META-INF/accesstransformer.cfg', 'assets/**', 'data/**')
        })
        return generated
    }

    private static String mergeATs(SourceSet... sourceSets) {
        String text = ""
        for (SourceSet sourceSet : sourceSets) {
            sourceSet.resources.matching { PatternFilterable pf ->
                pf.include('META-INF/accesstransformer.cfg')
            }.each { file ->
                text = text.isEmpty() ? file.getText() : text + "\n" + file.getText()
            }
        }
        return text
    }

    private static String mergeModsTOML(SourceSet... sourceSets) {
        String text = ""
        for (SourceSet sourceSet : sourceSets) {
            sourceSet.resources.matching { PatternFilterable pf ->
                pf.include('META-INF/mods.toml')
            }.each { file ->
                if (text.isEmpty()) {
                    //Nothing added yet, take it all
                    text = file.getText()
                } else {
                    //Otherwise add all but the first four lines (which are duplicated between the files)
                    String[] lines = file.getText().split("\n")
                    for (int i = 4; i < lines.length; i++) {
                        text = text + "\n" + lines[i]
                    }
                }
            }
        }
        return text
    }

    private static Map<String, List<String>> getReverseLookup(Project project, Closure filter, SourceSet... sourceSets) {
        Map<String, List<String>> reverseLookup = new HashMap<>()
        for (SourceSet sourceSet : sourceSets) {
            sourceSet.resources.srcDirs.each { srcDir ->
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

    private static void mergeAtlases(Project project, SourceSet... sourceSets) {
        Closure atlasFilter = { PatternFilterable pf -> pf.include('**/assets/*/atlases/**/*.json') }
        Map<String, List<String>> reverseAtlasLookup = getReverseLookup(project, atlasFilter, sourceSets)
        //Go through the reverse atlas lookup and if there are multiple sourceSets that contain the same atlas
        // properly merge that atlas
        reverseAtlasLookup.each { atlas, atlasPaths ->
            if (atlasPaths.size() > 1) {
                mergeSimpleJson(project, atlas, atlasPaths, (a, b) -> {
                    a.sources += b.sources
                    return a
                })
            }
        }
    }

    private static void mergeTags(Project project, SourceSet... sourceSets) {
        Closure tagFilter = { PatternFilterable pf -> pf.include('**/data/*/tags/**/*.json') }
        Map<String, List<String>> reverseTags = getReverseLookup(project, tagFilter, sourceSets)
        //Go through the reverse tag index and if there are multiple sourceSets that contain the same tag
        // properly merge that tag
        reverseTags.each { tag, tagPaths ->
            if (tagPaths.size() > 1) {
                mergeSimpleJson(project, tag, tagPaths, (a, b) -> {
                    a.values += b.values
                    return a
                })
            }
        }
    }

    private static void mergeSimpleJson(Project project, String jsonPath, List<String> paths, BinaryOperator<Object> appender) {
        //println(jsonPath + " appeared " + paths.size() + " times")
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
            File outputFile = new File("$project.buildDir/generated" + jsonPath)
            //Make all parent directories needed
            outputFile.getParentFile().mkdirs()
            outputFile.text = JsonOutput.toJson(outputAsJson)
        }
    }
}