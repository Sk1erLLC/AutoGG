package club.sk1er.mods.autogg.detectors;

import net.minecraft.client.entity.EntityPlayerSP;

public interface IDetector {
    boolean detect(String detector, EntityPlayerSP player);
}
