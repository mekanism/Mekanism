package mekanism.common.integration.jsonthings;

import dev.gigaherz.jsonthings.things.parsers.ThingResourceManager;
import mekanism.common.integration.jsonthings.parser.JsonGasParser;
import mekanism.common.integration.jsonthings.parser.JsonInfuseTypeParser;
import mekanism.common.integration.jsonthings.parser.JsonPigmentParser;
import mekanism.common.integration.jsonthings.parser.JsonSlurryParser;
import net.minecraftforge.eventbus.api.IEventBus;

public class JsonThingsIntegration {

    public static void hook(IEventBus bus) {
        ThingResourceManager resourceManager = ThingResourceManager.instance();
        resourceManager.registerParser(new JsonGasParser(bus));
        resourceManager.registerParser(new JsonInfuseTypeParser(bus));
        resourceManager.registerParser(new JsonPigmentParser(bus));
        resourceManager.registerParser(new JsonSlurryParser(bus));
    }
}