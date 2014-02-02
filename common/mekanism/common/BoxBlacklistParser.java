package mekanism.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import mekanism.api.MekanismAPI;

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
			
			if(readingLine.startsWith("#"))
			{
				continue;
			}
			
			String[] split = readingLine.split(":");
			
			if(split.length != 2 || !isInteger(split[0]) || !isInteger(split[1]))
			{
				System.err.println("[Mekanism] BoxBlacklist.txt: Couldn't parse blacklist data on line " + line);
			}
			
			MekanismAPI.addBoxBlacklist(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
			entries++;
		}
		
		reader.close();
		
		System.out.println("[Mekanism] Finished loading Cardboard Box blacklist (loaded " + entries + " entries)");
	}
	
	private static void writeExamples() throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(boxBlacklistFile));
		
		writer.append("# Use this file to tell Mekanism which blocks should not be picked up by a cardboard box.");
		writer.newLine();
		
		writer.append("# Proper syntax is \"ID:META\". Example (for stone):");
		writer.newLine();
		
		writer.append("# 1:0");
		
		writer.flush();
		writer.close();
	}
}
