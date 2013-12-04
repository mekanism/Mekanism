package mekanism.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class RecipeLoader
{
	public static File recipeFile;
	
	public static void init(File dir)
	{
		recipeFile = new File(dir, "MekanismRecipes.txt");
		
		/*if(!recipeFile.exists())
		{
			try {
				recipeFile.createNewFile();
				writeExamples();
			} catch(Exception e) {}
		}*/
	}
	
	public static void writeExamples()
	{
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(recipeFile));
			
			write(writer, "Examples:");
			write(writer, "crafting:{5, 1, 6},{XXX,XOX,XXX},{X,{");
			
			writer.flush();
			writer.close();
		} catch(Exception e) {}
	}
	
	public static void write(BufferedWriter writer, String s) throws IOException
	{
		writer.append(s);
		writer.newLine();
	}
}
