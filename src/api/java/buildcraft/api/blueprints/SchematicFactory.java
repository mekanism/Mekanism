/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;

public abstract class SchematicFactory<S extends Schematic> {

	private static final HashMap<String, SchematicFactory<?>> factories = new HashMap<String, SchematicFactory<?>>();

	private static final HashMap<Class<? extends Schematic>, SchematicFactory<?>> schematicToFactory = new HashMap<Class<? extends Schematic>, SchematicFactory<?>>();

	protected abstract S loadSchematicFromWorldNBT(NBTTagCompound nbt, MappingRegistry registry)
			throws MappingNotFoundException;

	public void saveSchematicToWorldNBT (NBTTagCompound nbt, S object, MappingRegistry registry) {
		nbt.setString("factoryID", getClass().getCanonicalName());
	}

	public static Schematic createSchematicFromWorldNBT(NBTTagCompound nbt, MappingRegistry registry)
			throws MappingNotFoundException {
		String factoryName = nbt.getString("factoryID");

		if (factories.containsKey(factoryName)) {
			return factories.get(factoryName).loadSchematicFromWorldNBT(nbt, registry);
		} else {
			return null;
		}
	}

	public static void registerSchematicFactory(Class<? extends Schematic> clas, SchematicFactory<?> factory) {
		schematicToFactory.put(clas, factory);
		factories.put(factory.getClass().getCanonicalName(), factory);
	}

	public static SchematicFactory getFactory(Class<? extends Schematic> clas) {
		Class superClass = clas.getSuperclass();

		if (schematicToFactory.containsKey(clas)) {
			return schematicToFactory.get(clas);
		} else if (superClass != null) {
			return getFactory(superClass);
		} else {
			return null;
		}
	}

}
