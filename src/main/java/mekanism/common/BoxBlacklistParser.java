package mekanism.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import mekanism.api.MekanismAPI;

import net.minecraft.block.Block;

public final class BoxBlacklistParser
{
	public static File mekanismDir = new File(Mekanism.proxy.getMinecraftDir(), "config/mekanism");
	public static File boxBlacklistFile = new File(mekanismDir, "BoxBlacklist.txt");

	public static void load()
	{
		try {
			generateFiles();
			readBlacklist();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static void generateFiles() throws IOException
	{
		mekanismDir.mkdirs();

		if(!boxBlacklistFile.exists())
		{
			boxBlacklistFile.createNewFile();
			writeExamples();
		}
	}

	private static boolean isInteger(String s)
	{
		try {
			Integer.parseInt(s);
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}

	private static void readBlacklist() throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(boxBlacklistFile));
		int entries = 0;

		String readingLine;
		int line = 0;

		while((readingLine = reader.readLine()) != null)
		{
			line++;

			if(readingLine.startsWith("#") || readingLine.trim().isEmpty())
			{
				continue;
			}

			String[] split = readingLine.split(":");

			if(split.length < 2 || split.length > 3 || !isInteger(split[split.length-1]))
			{
				Mekanism.logger.error("BoxBlacklist.txt: Couldn't parse blacklist data on line " + line);
				continue;
			}
			
			String blockName = (split.length == 2) ? split[0].trim() : split[0].trim() + ":" + split[1].trim();
			
			Block block = Block.getBlockFromName(blockName);
			
			if(block == null)
			{
				Mekanism.logger.error("BoxBlacklist.txt: Couldn't find specified block on line " + line);
				continue;
			}

			MekanismAPI.addBoxBlacklist(block, Integer.parseInt(split[split.length-1]));
			entries++;
		}

		reader.close();

		Mekanism.logger.info("Finished loading Cardboard Box blacklist (loaded " + entries + " entries)");
	}

	private static void writeExamples() throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(boxBlacklistFile));

		writer.append("# Use this file to tell Mekanism which blocks should not be picked up by a cardboard box.");
		writer.newLine();

		writer.append("# Proper syntax is \"NAME:META\". Example (for stone):");
		writer.newLine();

		writer.append("# stone:0");

		writer.flush();
		writer.close();
	}
}
