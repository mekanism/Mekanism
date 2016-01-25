/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.blueprints;

/** Schematics recorded in the blueprints can restrict situations where a blueprint can be used. A same schematic class
 * can have different permissions depending on its contents. It's particularly useful when fixing a schematic, if
 * blueprints that saved the previous version should not be built because of a bug (such as dupe bug on inventories). */
public enum BuildingPermission {
    /** No restrictions, blueprints using this schematic are good in all contexts. */
    ALL,

    /** This blueprints containing this schematic can only be used in creative. Maybe the block can't be crafted in
     * survival in the first place, or the content of the schematic is known to have dupe bugs. */
    CREATIVE_ONLY,

    /** Blueprints containing this schematic should not be built. This is typically used when a critical problems have
     * been fixed, but older versions of the schematic are too badly broken to be retreived. */
    NONE,
}
