/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.blueprints;

public final class BuilderAPI {
    public static ISchematicRegistry schematicRegistry;
    public static ISchematicHelper schematicHelper;

    private static final long MJ = 1000 * 1000;
    public static final long BREAK_POWER = 16 * MJ;// micro Mj
    public static final long BUILD_POWER = 24 * MJ;// micro Mj

    private BuilderAPI() {}
}
