package mekanism.common.classloading;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassEnumerator
{
	private static Class<?> loadClass(String className)
	{
		try {
			return Class.forName(className);
		} catch(ClassNotFoundException e) {
			throw new RuntimeException("Unexpected ClassNotFoundException loading class '" + className + "'");
		}
	}

	private static void processDir(File directory, String pkgname, ArrayList<Class<?>> classes)
	{
		String[] files = directory.list();

		for(int i = 0; i < files.length; i++)
		{
			String fileName = files[i];
			String className = null;

			if(fileName.endsWith(".class"))
			{
				className = pkgname + '.' + fileName.substring(0, fileName.length() - 6);
			}

			if(className != null)
			{
				classes.add(loadClass(className));
			}

			File subdir = new File(directory, fileName);

			if(subdir.isDirectory())
			{
				processDir(subdir, pkgname + '.' + fileName, classes);
			}
		}
	}

	private static void processJar(URL resource, String pkgname, ArrayList<Class<?>> classes)
	{
		String relPath = pkgname.replace('.', '/');
		String resPath = resource.getPath();
		String jarPath = resPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");

		JarFile jarFile;

		try {
			jarFile = new JarFile(jarPath);
		} catch(IOException e) {
			throw new RuntimeException("Unexpected IOException reading JAR File '" + jarPath + "'", e);
		}

		Enumeration<JarEntry> entries = jarFile.entries();

		while(entries.hasMoreElements())
		{
			JarEntry entry = entries.nextElement();
			String entryName = entry.getName();
			String className = null;

			if(entryName.endsWith(".class") && entryName.startsWith(relPath) && entryName.length() > (relPath.length() + "/".length()))
			{
				className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
			}

			if(className != null)
			{
				classes.add(loadClass(className));
			}
		}
	}

	public static ArrayList<Class<?>> getClassesForPackage(Package pkg)
	{
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

		try {
			String pkgname = pkg.getName();
			String relPath = pkgname.replace('.', '/');

			URL resource = ClassLoader.getSystemClassLoader().getResource(relPath);

			if(resource == null)
			{
				throw new RuntimeException("Unexpected problem: No resource for " + relPath);
			}

			resource.getPath();

			if(resource.toString().startsWith("jar:"))
			{
				processJar(resource, pkgname, classes);
			}
			else {
				processDir(new File(resource.getPath()), pkgname, classes);
			}
		} catch(Exception e) {
			System.err.println("[Mekanism] Error while loading classes in package " + pkg);
			e.printStackTrace();
		}

		return classes;
	}
}