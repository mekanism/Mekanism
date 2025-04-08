package mekanism.additions.common;

import mekanism.additions.client.AdditionsClient;
import mekanism.additions.common.block.BlockObsidianTNT;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.entity.EntityBalloon;
import mekanism.additions.common.item.ItemBalloon;
import mekanism.additions.common.registries.AdditionsBiomeModifierSerializers;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsCreativeTabs;
import mekanism.additions.common.registries.AdditionsDataComponents;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.additions.common.registries.AdditionsSounds;
import mekanism.additions.common.registries.AdditionsStructureModifierSerializers;
import mekanism.additions.common.voice.VoiceServerManager;
import mekanism.common.Mekanism;
import mekanism.common.base.IModModule;
import mekanism.common.lib.Version;
import mekanism.common.registration.impl.ItemRegistryObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.NotNull;

@Mod(MekanismAdditions.MODID)
public class MekanismAdditions implements IModModule {

    public static final String MODID = "mekanismadditions";

    public static MekanismAdditions instance;

    /**
     * MekanismTools version number
     */
    public final Version versionNumber;

    /**
     * The VoiceServer manager for walkie-talkies
     */
    public static VoiceServerManager voiceManager;

    public MekanismAdditions(ModContainer modContainer, IEventBus modEventBus) {
        Mekanism.addModule(instance = this);
        //Set our version number to match the neoforge.mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(modContainer);
        MekanismAdditionsConfig.registerConfigs(modContainer);

        NeoForge.EVENT_BUS.addListener(this::serverStarting);
        NeoForge.EVENT_BUS.addListener(this::serverStopping);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(MekanismAdditionsConfig::onConfigLoad);
        AdditionsDataComponents.DATA_COMPONENTS.register(modEventBus);
        AdditionsItems.ITEMS.register(modEventBus);
        AdditionsBlocks.BLOCKS.register(modEventBus);
        AdditionsCreativeTabs.CREATIVE_TABS.register(modEventBus);
        AdditionsEntityTypes.ENTITY_TYPES.register(modEventBus);
        AdditionsSounds.SOUND_EVENTS.register(modEventBus);
        AdditionsBiomeModifierSerializers.BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
        AdditionsStructureModifierSerializers.STRUCTURE_MODIFIER_SERIALIZERS.register(modEventBus);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    @Override
    public Version getVersion() {
        return versionNumber;
    }

    @Override
    public String getName() {
        return "Additions";
    }

    @Override
    public void resetClient() {
        AdditionsClient.reset();
    }

    @Override
    public void launchClient(Connection connection) {
        AdditionsClient.launch(connection);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            //Dispenser behavior
            DispenserBlock.registerBehavior(AdditionsBlocks.OBSIDIAN_TNT, new DefaultDispenseItemBehavior() {
                @NotNull
                @Override
                protected ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
                    BlockPos blockpos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
                    if (BlockObsidianTNT.createAndAddEntity(source.level(), blockpos, null)) {
                        source.level().gameEvent(null, GameEvent.ENTITY_PLACE, blockpos);
                        stack.shrink(1);
                        return stack;
                    }
                    //Otherwise, if something went very wrong, eject it as a normal item
                    return super.execute(source, stack);
                }
            });
            DefaultDispenseItemBehavior balloonBehavior = new DefaultDispenseItemBehavior() {
                @NotNull
                @Override
                protected ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
                    ServerLevel level = source.level();
                    Position position = DispenserBlock.getDispensePosition(source, 1, new Vec3(-0.5, -3.5, -0.5));
                    EntityBalloon balloon = EntityBalloon.create(level, position.x(), position.y(), position.z(), ((ItemBalloon) stack.getItem()).getColor());
                    if (balloon == null) {
                        //Otherwise, if something went very wrong, eject it as a normal item
                        return super.execute(source, stack);
                    }
                    stack.shrink(1);
                    level.addFreshEntity(balloon);
                    level.gameEvent(null, GameEvent.ENTITY_PLACE, new Vec3(position.x(), position.y(), position.z()));
                    return stack;
                }
            };
            for (ItemRegistryObject<ItemBalloon> balloon : AdditionsItems.BALLOONS.values()) {
                DispenserBlock.registerBehavior(balloon, balloonBehavior);
            }
        });
        Mekanism.logger.info("Loaded 'Mekanism: Additions' module.");
    }

    private void serverStarting(ServerStartingEvent event) {
        if (MekanismAdditionsConfig.additions.voiceServerEnabled.get()) {
            if (voiceManager == null) {
                voiceManager = new VoiceServerManager();
            }
            voiceManager.start();
        }
    }

    private void serverStopping(ServerStoppingEvent event) {
        if (voiceManager != null) {
            voiceManager.stop();
            voiceManager = null;
        }
    }
}