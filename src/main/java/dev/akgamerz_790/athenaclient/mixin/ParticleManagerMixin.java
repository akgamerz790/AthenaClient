package dev.akgamerz_790.athenaclient.mixin;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    private static final int MAX_PARTICLES = 2000;
    private static int particleCount = 0;

    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
    private void limitParticles(Particle particle, CallbackInfo ci) {
        if (particleCount >= MAX_PARTICLES) {
            ci.cancel();
            return;
        }
        particleCount++;
    }

    @Inject(method = "clearParticles", at = @At("HEAD"))
    private void onClearParticles(CallbackInfo ci) {
        particleCount = 0;
    }
}