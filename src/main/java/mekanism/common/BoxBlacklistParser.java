package mekanism.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import mekanism.common.config.MekanismConfigHelper;

public final class BoxBlacklistParser {

    //TODO: Convert this to a toml file
    private static File boxBlacklistFile = MekanismConfigHelper.CONFIG_DIR.resolve("BoxBlacklist.txt").toFile();

    private BoxBlacklistParser() {
    }

    public static void load() {
        try {
            generateFiles();
            readBlacklist();
        } catch (Exception e) {
            Mekanism.logger.warn("Couldn't load Cardboard Box blacklist", e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void generateFiles() throws IOException {
        if (!boxBlacklistFile.exists()) {
            boxBlacklistFile.createNewFile();
            writeExamples();
        }
    }

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void readBlacklist() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(boxBlacklistFile))) {
            int entries = 0;
            String readingLine;
            int line = 0;

            while ((readingLine = reader.readLine()) != null) {
                line++;
                if (readingLine.startsWith("#") || readingLine.trim().isEmpty()) {
                    continue;
                }

                String[] split = readingLine.split(" ");
                if (split.length != 2 || !isInteger(split[split.length - 1])) {
                    Mekanism.logger.error("BoxBlacklist.txt: Couldn't parse blacklist data on line " + line);
                    continue;
                }

                //TODO: Add config file support again for this
                /*String blockName = split[0].trim();
                Block block = Block.getBlockFromName(blockName);
                if (block == null) {
                    Mekanism.logger.error("BoxBlacklist.txt: Couldn't find specified block on line " + line);
                    continue;
                }
                MekanismAPI.addBoxBlacklist(block, Integer.parseInt(split[split.length - 1]));*/
                entries++;
            }
            Mekanism.logger.info("Finished loading Cardboard Box blacklist (loaded " + entries + " entries)");
        }
    }

    private static void writeExamples() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(boxBlacklistFile))) {
            writer.append("# Use this file to tell Mekanism which blocks should not be picked up by a cardboard box.");
            writer.newLine();
            writer.append("# Proper syntax is \"NAME META\". Example (for stone):");
            writer.newLine();
            writer.append("# minecraft:stone 0");
        }
    }
}