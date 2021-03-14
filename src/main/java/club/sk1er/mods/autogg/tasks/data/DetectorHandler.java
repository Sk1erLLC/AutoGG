package club.sk1er.mods.autogg.tasks.data;

import club.sk1er.mods.autogg.detectors.IDetector;
import club.sk1er.mods.autogg.detectors.branding.ServerBrandingDetector;
import club.sk1er.mods.autogg.detectors.ip.ServerIPDetector;

public enum DetectorHandler {
    SERVER_BRANDING(new ServerBrandingDetector()),
    SERVER_IP(new ServerIPDetector());

    private final IDetector detector;

    DetectorHandler(IDetector detector) {
        this.detector = detector;
    }

    public IDetector getDetector() {
        return detector;
    }
}
