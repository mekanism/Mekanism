package mekanism.common.upgrade;

import java.util.List;
import mekanism.common.tile.component.ITileComponent;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.ProblemReporter.PathElement;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface IUpgradeData {

    static CompoundTag readComponents(Provider provider, List<ITileComponent> components, PathElement pathElement) {
        try (var reporter = new ProblemReporter.ScopedCollector(pathElement, readComponentsLogger)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, provider);
            for (ITileComponent component : components) {
                component.write(output);
            }
            return output.buildResult();
        }
    }

    Logger readComponentsLogger = LoggerFactory.getLogger("Mekanism UpgradeData");
}