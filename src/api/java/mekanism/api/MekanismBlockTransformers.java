package mekanism.api;

import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/// Common placeholder of all the Item Abilities we create in Mekanism. It is still possible to just recreate the item abilities yourself to avoid referencing this class
///
/// @since 10.8.0
public class MekanismBlockTransformers {

    //TODO - 26.3: Docs and decide if this should be expose to the API, and if so should it be in tools or main mek
    public static final ResourceKey<BlockTransformer> PAXEL = createToolsKey("paxel");

    private static ResourceKey<BlockTransformer> createToolsKey(String name) {
        return ResourceKey.create(Registries.BLOCK_TRANSFORMER, Identifier.fromNamespaceAndPath(MekanismAPI.TOOLS_MODID, name));
    }
}