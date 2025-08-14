package de.wutzuket.create_overdrive.config;

import com.mojang.logging.LogUtils;
import de.wutzuket.create_overdrive.Main;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;

@EventBusSubscriber(modid = Main.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue RADIUS = BUILDER
            .comment("Radius of the particle accelerator structure")
            .defineInRange("radius", 10, 1, 100);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static int radius;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            LOGGER.info("Konfiguration wird geladen");
            radius = RADIUS.get();
            Main.updateConfigValues();
        }
    }
}