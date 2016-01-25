package mcp.mobius.waila.api;

import java.util.regex.Pattern;

public class SpecialChars {

	public static String MCStyle  = "\u00A7";
	
	public static String BLACK    = MCStyle + "0";
	public static String DBLUE    = MCStyle + "1";
	public static String DGREEN   = MCStyle + "2";
	public static String DAQUA    = MCStyle + "3";
	public static String DRED     = MCStyle + "4";
	public static String DPURPLE  = MCStyle + "5";
	public static String GOLD     = MCStyle + "6";
	public static String GRAY     = MCStyle + "7";
	public static String DGRAY    = MCStyle + "8";
	public static String BLUE     = MCStyle + "9";
	public static String GREEN    = MCStyle + "a";
	public static String AQUA     = MCStyle + "b";
	public static String RED      = MCStyle + "c";
	public static String LPURPLE  = MCStyle + "d";
	public static String YELLOW   = MCStyle + "e";
	public static String WHITE    = MCStyle + "f";	
	
	public static String OBF      = MCStyle + "k";
	public static String BOLD     = MCStyle + "l";
	public static String STRIKE   = MCStyle + "m";
	public static String UNDER    = MCStyle + "n";
	public static String ITALIC   = MCStyle + "o";
	public static String RESET    = MCStyle + "r";		

	public static String WailaStyle     = "\u00A4";
	public static String WailaIcon      = "\u00A5";
	public static String WailaRenderer  = "\u00A6";
	public static String TAB         = WailaStyle + WailaStyle +"a";
	public static String ALIGNRIGHT  = WailaStyle + WailaStyle +"b";
	public static String ALIGNCENTER = WailaStyle + WailaStyle +"c";	
	public static String HEART       = WailaStyle + WailaIcon  +"a";
	public static String HHEART      = WailaStyle + WailaIcon  +"b";
	public static String EHEART      = WailaStyle + WailaIcon  +"c";
	public static String RENDER      = WailaStyle + WailaRenderer +"a";
	
	public static final Pattern patternMinecraft = Pattern.compile("(?i)"  + MCStyle + "[0-9A-FK-OR]");
	public static final Pattern patternWaila     = Pattern.compile("(?i)(" + WailaStyle + "(?<type>..))");
	public static final Pattern patternRender    = Pattern.compile("(?i)(" + RENDER + "\\{(?<name>[^,}]*),?(?<args>[^}]*)\\})");
	public static final Pattern patternTab       = Pattern.compile("(?i)"  + TAB);
	public static final Pattern patternRight     = Pattern.compile("(?i)"  + ALIGNRIGHT);
	public static final Pattern patternCenter    = Pattern.compile("(?i)"  + ALIGNCENTER);
	public static final Pattern patternIcon      = Pattern.compile("(?i)(" + WailaStyle + WailaIcon + "(?<type>[0-9a-z]))");
	public static final Pattern patternLineSplit = Pattern.compile("(?i)(" + WailaStyle + WailaStyle + "[^" + WailaStyle + "]+|" + WailaStyle + WailaIcon + "[0-9A-Z]|" + WailaStyle + WailaRenderer + "a\\{([^,}]*),?([^}]*)\\}|[^" + WailaStyle + "]+)");
	
	/**
	 * Helper method to get a proper RENDER string. Just put the name of the renderer and the params in, and it will give back a directly usable String for the tooltip.
	 * @param name
	 * @param params
	 * @return
	 */
	public static String getRenderString(String name, String... params){
		String result = RENDER + "{" + name;
		for (String s : params){
			result += "," + s;
		}
		result += "}";
		return result;
	}
}
