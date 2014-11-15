/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.gates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class GateExpansions {

	private static final Map<String, IGateExpansion> expansions = new HashMap<String, IGateExpansion>();
	private static final ArrayList<IGateExpansion> expansionIDs = new ArrayList<IGateExpansion>();

	private GateExpansions() {
	}

	public static void registerExpansion(IGateExpansion expansion) {
		registerExpansion(expansion.getUniqueIdentifier(), expansion);
	}

	public static void registerExpansion(String identifier, IGateExpansion expansion) {
		expansions.put(identifier, expansion);
		expansionIDs.add(expansion);
	}

	public static IGateExpansion getExpansion(String identifier) {
		return expansions.get(identifier);
	}

	public static Set<IGateExpansion> getExpansions() {
		Set<IGateExpansion> set = new HashSet<IGateExpansion>();
		set.addAll(expansionIDs);
		return set;
	}
	
	// The code below is used by networking.
	
	public static IGateExpansion getExpansionByID(int id) {
		return expansionIDs.get(id);
	}
	
	public static int getExpansionID(IGateExpansion expansion) {
		return expansionIDs.indexOf(expansion);
	}
}
