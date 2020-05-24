package mekanism.common.tag;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagCollection;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public final class TagType<TYPE extends IForgeRegistryEntry<TYPE>> {

    public static TagType<Item> ITEM = new TagType<>("Item", "items", () -> ForgeRegistries.ITEMS, ItemTags::setCollection);
    public static TagType<Block> BLOCK = new TagType<>("Block", "blocks", () -> ForgeRegistries.BLOCKS, BlockTags::setCollection);
    public static TagType<EntityType<?>> ENTITY_TYPE = new TagType<>("Entity Type", "entity_types", () -> ForgeRegistries.ENTITIES, EntityTypeTags::setCollection);
    public static TagType<Fluid> FLUID = new TagType<>("Fluid", "fluids", () -> ForgeRegistries.FLUIDS, FluidTags::setCollection);
    public static TagType<Gas> GAS = new TagType<>("Gas", "gases", () -> MekanismAPI.GAS_REGISTRY, ChemicalTags.GAS::setCollection);
    public static TagType<InfuseType> INFUSE_TYPE = new TagType<>("Infuse Type", "infuse_types", () -> MekanismAPI.INFUSE_TYPE_REGISTRY, ChemicalTags.INFUSE_TYPE::setCollection);
    public static TagType<Pigment> PIGMENT = new TagType<>("Pigment", "pigments", () -> MekanismAPI.PIGMENT_REGISTRY, ChemicalTags.PIGMENT::setCollection);
    public static TagType<Slurry> SLURRY = new TagType<>("Slurry", "slurries", () -> MekanismAPI.SLURRY_REGISTRY, ChemicalTags.SLURRY::setCollection);

    private final Consumer<TagCollection<TYPE>> collectionSetter;
    private final Supplier<IForgeRegistry<TYPE>> registry;
    private final String name;
    private final String path;

    private TagType(String name, String path, Supplier<IForgeRegistry<TYPE>> registry, Consumer<TagCollection<TYPE>> collectionSetter) {
        this.name = name;
        this.path = path;
        this.collectionSetter = collectionSetter;
        this.registry = registry;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public void setCollection(@Nonnull TagCollection<TYPE> tagCollection) {
        collectionSetter.accept(tagCollection);
    }

    public IForgeRegistry<TYPE> getRegistry() {
        return registry.get();
    }
}