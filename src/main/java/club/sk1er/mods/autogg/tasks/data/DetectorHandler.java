package club.sk1er.mods.autogg.tasks.data;

import club.sk1er.mods.autogg.detectors.IDetector;
import club.sk1er.mods.autogg.detectors.branding.ServerBrandingDetector;

public enum DetectorHandler {
    SERVER_BRANDING(new ServerBrandingDetector());

    private final IDetector detector;

    DetectorHandler(IDetector detector) {
        this.detector = detector;
    }

    public IDetector getDetector() {
        return detector;
    }
}
