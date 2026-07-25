package mekanism.common.tests;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.nio.file.Path;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.testframework.conf.ClientConfiguration;
import net.neoforged.testframework.conf.Feature;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.impl.MutableTestFramework;
import net.neoforged.testframework.summary.GitHubActionsStepSummaryDumper;
import net.neoforged.testframework.summary.JUnitSummaryDumper;

@Mod(MekanismTests.MODID)
public class MekanismTests {

    public static final String MODID = "mekanismtests";

    public MekanismTests(IEventBus modBus, ModContainer container) {
        //More or less a copy of net.neoforged.neoforge.eventtest.internal.TestsMod but with a few tweaks
        final MutableTestFramework framework = FrameworkConfiguration.builder(rl("tests"))
              .clientConfiguration(() -> ClientConfiguration.builder()
                    .toggleOverlayKey(InputConstants.KEY_O)
                    .openManagerKey(InputConstants.KEY_M)
                    .build())
              .enable(Feature.CLIENT_SYNC, Feature.TEST_STORE)
              //TODO: Figure out which dumpers we want to enable and how they work
              .dumpers(new JUnitSummaryDumper(Path.of("gameTest/")), new GitHubActionsStepSummaryDumper())
              .build().create();

        framework.init(modBus, container);

        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> {
            final LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("tests");
            framework.registerCommands(node);
            event.getDispatcher().register(node);
        });
    }

    public static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}