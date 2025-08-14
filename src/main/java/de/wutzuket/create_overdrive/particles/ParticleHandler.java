package de.wutzuket.create_overdrive.particles;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = "create_overdrive", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ParticleHandler {

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.FUSION_REACTOR_CASING_PARTICLE.get(),
                (spriteSet) -> (type, level, x, y, z, xSpeed, ySpeed, zSpeed) ->
                        new FusionReactorCasingParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet));
    }
}
