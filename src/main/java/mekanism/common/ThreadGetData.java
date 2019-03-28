package mekanism.common;

import java.util.List;
import mekanism.common.util.MekanismUtils;
import net.minecraftforge.common.ForgeVersion;

/**
 * Thread used to retrieve data from the Mekanism server.
 *
 * @author AidanBrady
 */
public class ThreadGetData extends Thread {

    public ThreadGetData() {
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        List<String> ret = MekanismUtils.getHTML("http://aidancbrady.com/data/versions/Mekanism.txt");

        Mekanism.latestVersionNumber = "null";
        Mekanism.recentNews = "null";

        for (String s : ret) {
            String[] text = s.split(":");

            if (text.length == 3 && !text[0].contains("UTF-8") && !text[0].contains("HTML") && !text[0]
                  .contains("http")) {
                if (Version.get(text[0]) != null && Version.get(text[0]).equals(Version.get(ForgeVersion.mcVersion))) {
                    Mekanism.latestVersionNumber = text[1];
                    Mekanism.recentNews = text[2];

                    break;
                }
            }
        }

        MekanismUtils.updateDonators();
    }
}
