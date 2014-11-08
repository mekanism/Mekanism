package buildcraft.api.blueprints;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;

public interface ISchematicRegistry {
	void registerSchematicBlock(Block block, Class<? extends Schematic> clazz, Object... params);
	void registerSchematicBlock(Block block, int meta, Class<? extends Schematic> clazz, Object... params);
	void registerSchematicEntity(
			Class<? extends Entity> entityClass,
			Class<? extends SchematicEntity> schematicClass, Object... params);
	
	boolean isSupported(Block block, int metadata);
}
