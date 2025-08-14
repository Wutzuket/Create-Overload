package de.wutzuket.create_overdrive.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class FusionReactorCasingParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;

    public FusionReactorCasingParticle(ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.spriteSet = spriteSet;

        // Partikel-Eigenschaften ohne Physik
        this.lifetime = 40 + this.random.nextInt(20); // 2-3 Sekunden Lebensdauer
        this.gravity = 0.0F; // Keine Schwerkraft
        this.friction = 1.0F; // Keine Reibung (bleibt an Ort und Stelle)

        // Konstante Größe - keine Variation
        this.quadSize = 0.125F; // Feste Größe

        // Originalfarbe der Block-Textur beibehalten
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.alpha = 1.0F; // Bleibt konstant bei 100%

        // Keine Bewegung - Partikel bleibt statisch
        this.xd = 0.0D;
        this.yd = 0.0D;
        this.zd = 0.0D;

        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();

        // Sprite basierend auf Alter aktualisieren
        this.setSpriteFromAge(spriteSet);

        // Alpha bleibt konstant - keine Transparenz-Änderung
        this.alpha = 1.0F;

        // Größe bleibt konstant - keine Änderung
        this.quadSize = 0.125F;

        // Keine Bewegung - Geschwindigkeiten auf 0 setzen
        this.xd = 0.0D;
        this.yd = 0.0D;
        this.zd = 0.0D;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
