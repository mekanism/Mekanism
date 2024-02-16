package mekanism.client.texture;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import mekanism.common.Mekanism;
import mekanism.common.entity.RobitPrideSkinData;
import mekanism.common.lib.Color;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.util.EnumUtils;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.PathProvider;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class PrideRobitTextureProvider implements DataProvider {

    private final PackOutput output;
    private final ExistingFileHelper helper;

    private static final String ROBIT_SKIN_PATH = "textures/entity/robit";

    public PrideRobitTextureProvider(PackOutput output, ExistingFileHelper helper) {
        this.output = output;
        this.helper = helper;
    }

    @NotNull
    @Override
    @SuppressWarnings("UnstableApiUsage")
    public CompletableFuture<?> run(@NotNull CachedOutput cache) {
        return CompletableFuture.runAsync(() -> {
            PathProvider pathProvider = output.createPathProvider(Target.RESOURCE_PACK, ROBIT_SKIN_PATH);
            try {
                Resource resource = helper.getResource(MekanismRobitSkins.BASE.location(), PackType.CLIENT_RESOURCES, ".png", ROBIT_SKIN_PATH);
                try (InputStream inputStream = resource.open();
                     NativeImage sourceImage = NativeImage.read(inputStream);
                     NativeImage writableImage = new NativeImage(sourceImage.format(), sourceImage.getWidth(), sourceImage.getHeight(), false)) {
                    //Set initial image data, we can just use one writable version as we always edit the same pixels,
                    // so we will overwrite any changes we make. In theory, we could just edit the loaded source directly,
                    // but it is a bit safer to just copy it into its own spot in memory
                    writableImage.copyFrom(sourceImage);
                    for (RobitPrideSkinData skinData : EnumUtils.PRIDE_SKINS) {
                        String baseFileName = skinData.lowerCaseName();
                        //generate all textures
                        for (int rotationIndex = 0; rotationIndex < skinData.getColor().length; rotationIndex++) {
                            for (int chainIndex = 0; chainIndex < 4; chainIndex++) {
                                int maxStripeIndex = chainIndex == 1 || chainIndex == 3 ? 3 : 9;
                                int y = switch (chainIndex) {
                                    case 0 -> 4;
                                    case 2 -> 0;
                                    default -> 2;
                                };
                                for (int stripeIndex = 0; stripeIndex < maxStripeIndex; stripeIndex++) {
                                    int x = 15 + switch (chainIndex) {
                                        case 0 -> 8 - stripeIndex;
                                        case 1 -> 2 - stripeIndex;
                                        default -> stripeIndex;
                                        case 3 -> stripeIndex + 6;
                                    };
                                    int abgr = abgr(stripeIndex, chainIndex, rotationIndex, skinData);
                                    writableImage.setPixelRGBA(x, y, abgr);
                                    writableImage.setPixelRGBA(x, y + 1, abgr);
                                }
                            }
                            //Save the image
                            String fileName = baseFileName;
                            if (rotationIndex != 0) {
                                fileName += rotationIndex + 1;
                            }
                            Path path = pathProvider.file(Mekanism.rl(fileName), "png");
                            byte[] bytes = writableImage.asByteArray();
                            Hasher hasher = Hashing.sha1().newHasher();
                            hasher.putBytes(bytes);
                            cache.writeIfNeeded(path, bytes, hasher.hash());
                        }
                    }
                }
            } catch (IOException exception) {
                Mekanism.logger.error("Couldn't create robit textures", exception);
            }
        }, Util.backgroundExecutor());
    }

    /**
     * @param stripeIndex Stripe Index on the chain.
     * @param chainIndex  The chain index (bottom, front(left), top, back(right)). It starts on the bottom to hide a potential seam on the bottom/back connection
     *
     * @return Color at that position
     */
    private int abgr(int stripeIndex, int chainIndex, int rotationIndex, RobitPrideSkinData data) {
        //offset it by 12, so the pride flag always starts at the top by default
        int index = stripeIndex + rotationIndex + 12;
        if (chainIndex > 2) {
            index += 9;
        }
        if (chainIndex > 1) {
            index += 3;
        }
        if (chainIndex > 0) {
            index += 9;
        }
        int[] colors = data.getColor();
        return Color.argbToFromABGR(colors[index % colors.length]);
    }

    @NotNull
    @Override
    public String getName() {
        return "Robit Texture Provider";
    }
}