package mekanism.common.tag;

import com.mojang.datafixers.util.Either;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public record TagType<TYPE>(String name, NonNullSupplier<Either<IForgeRegistry<TYPE>, Registry<TYPE>>> registry) {

    public static final TagType<Item> ITEM = new TagType<>("Item", () -> Either.left(ForgeRegistries.ITEMS));
    public static final TagType<Block> BLOCK = new TagType<>("Block", () -> Either.left(ForgeRegistries.BLOCKS));
    public static final TagType<EntityType<?>> ENTITY_TYPE = new TagType<>("Entity Type", () -> Either.left(ForgeRegistries.ENTITY_TYPES));
    public static final TagType<Fluid> FLUID = new TagType<>("Fluid", () -> Either.left(ForgeRegistries.FLUIDS));
    public static final TagType<BlockEntityType<?>> BLOCK_ENTITY_TYPE = new TagType<>("Block Entity Type", () -> Either.left(ForgeRegistries.BLOCK_ENTITY_TYPES));
    public static final TagType<GameEvent> GAME_EVENT = new TagType<>("Game Event", () -> Either.right(Registry.GAME_EVENT));
    public static final TagType<Gas> GAS = new TagType<>("Gas", () -> Either.left(MekanismAPI.gasRegistry()));
    public static final TagType<InfuseType> INFUSE_TYPE = new TagType<>("Infuse Type", () -> Either.left(MekanismAPI.infuseTypeRegistry()));
    public static final TagType<Pigment> PIGMENT = new TagType<>("Pigment", () -> Either.left(MekanismAPI.pigmentRegistry()));
    public static final TagType<Slurry> SLURRY = new TagType<>("Slurry", () -> Either.left(MekanismAPI.slurryRegistry()));

    public Either<IForgeRegistry<TYPE>, Registry<TYPE>> getRegistry() {
        return registry.get();
    }
}