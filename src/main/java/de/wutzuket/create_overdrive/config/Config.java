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

    public static final ModConfigSpec.IntValue ROTATOR_CAPACITY = BUILDER
            .comment("Energy capacity of the Rotator Input")
            .defineInRange("rotator_capacity", 100000, 1000, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ROTATOR_MAX_OUTPUT = BUILDER
            .comment("Maximum energy output of the Rotator Input per tick")
            .defineInRange("rotator_max_output", 1000, 100, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue FE_STRESS = BUILDER
            .comment("FE per 1000 unit of stress for the Rotator")
            .defineInRange("fe_stress", 1, 0.0, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue ROTATOR_EFFICIENCY = BUILDER
            .comment("Efficiency multiplier for energy conversion in the Rotator")
            .defineInRange("rotator_efficiency", 100, 0.1, 1);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static int radius;
    public static int rotator_capacity;
    public static int rotator_max_output;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            LOGGER.info("Konfiguration wird geladen");
            radius = RADIUS.get();
            rotator_capacity = ROTATOR_CAPACITY.get();
            rotator_max_output = ROTATOR_MAX_OUTPUT.get();
            Main.updateConfigValues();
        }
    }

    // Safe getters to avoid IllegalStateException if config is requested before it's loaded
    public static int getIntSafe(ModConfigSpec.IntValue val, int fallback) {
        try {
            return val.get();
        } catch (IllegalStateException ex) {
            LOGGER.debug("Config value requested before load, returning fallback: {}", fallback);
            return fallback;
        }
    }

    public static double getDoubleSafe(ModConfigSpec.DoubleValue val, double fallback) {
        try {
            return val.get();
        } catch (IllegalStateException ex) {
            LOGGER.debug("Config value requested before load, returning fallback: {}", fallback);
            return fallback;
        }
    }
}

