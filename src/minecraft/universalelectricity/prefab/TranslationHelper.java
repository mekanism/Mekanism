package universalelectricity.prefab;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.LanguageRegistry;

/**
 * A class to help you out with translations.
 * 
 * @author Calclavia
 * 
 */
public class TranslationHelper
{
	/**
	 * Loads all the language files for a mod. This supports the loading of "child" language files
	 * for sub-languages to be loaded all from one file instead of creating multiple of them. An
	 * example of this usage would be different Spanish sub-translations (es_MX, es_YU).
	 * 
	 * @param languagePath - The path to the mod's language file folder.
	 * @param languageSupported - The languages supported. E.g: new String[]{"en_US", "en_AU",
	 * "en_UK"}
	 * @return The amount of language files loaded successfully.
	 */
	public static int loadLanguages(String languagePath, String[] languageSupported)
	{
		int languages = 0;

		/**
		 * Load all languages.
		 */
		for (String language : languageSupported)
		{
			LanguageRegistry.instance().loadLocalization(languagePath + language + ".properties", language, false);

			if (LanguageRegistry.instance().getStringLocalization("children", language) != "")
			{
				try
				{
					String[] children = LanguageRegistry.instance().getStringLocalization("children", language).split(",");

					for (String child : children)
					{
						if (child != "" || child != null)
						{
							LanguageRegistry.instance().loadLocalization(languagePath + language + ".properties", child, false);
							languages++;
						}
					}
				}
				catch (Exception e)
				{
					FMLLog.severe("Failed to load a child language file.");
					e.printStackTrace();
				}
			}

			languages++;
		}

		return languages;
	}

	/**
	 * Gets the local text of your translation based on the given key. This will look through your
	 * mod's translation file that was previously registered. Make sure you enter the full name
	 * 
	 * @param key - e.g tile.block.name
	 * @return The translated string or the default English translation if none was found.
	 */
	public static String getLocal(String key)
	{
		String text = LanguageRegistry.instance().getStringLocalization(key);

		if (text == null || text == "")
		{
			text = LanguageRegistry.instance().getStringLocalization(key, "en_US");
		}

		return text;
	}
}
