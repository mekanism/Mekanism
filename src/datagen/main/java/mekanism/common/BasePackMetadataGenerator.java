package mekanism.common;

import java.util.Optional;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.DetectedVersion;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;

public class BasePackMetadataGenerator extends PackMetadataGenerator {

    public BasePackMetadataGenerator(PackOutput output, IHasTranslationKey description) {
        super(output);
        int minVersion = Integer.MAX_VALUE;
        int maxVersion = 0;
        for (PackType packType : PackType.values()) {
            int version = DetectedVersion.BUILT_IN.getPackVersion(packType);
            maxVersion = Math.max(maxVersion, version);
            minVersion = Math.min(minVersion, version);
        }
        add(PackMetadataSection.TYPE, new PackMetadataSection(
              TextComponentUtil.build(description),
              maxVersion,
              Optional.of(new InclusiveRange<>(minVersion, maxVersion))
        ));
    }
}