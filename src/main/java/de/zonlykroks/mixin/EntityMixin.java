package de.zonlykroks.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import de.zonlykroks.PNFEntityExtensions;
import de.zonlykroks.PetherNortalFix;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements PNFEntityExtensions {

    @Unique
    public boolean hasToBeLoaded = false;

    @Unique
    public int ticksToLive = 20 * 60;

    @Inject(method = "tickPortal", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;", shift = At.Shift.AFTER))
    public void setThroughPortal(CallbackInfo ci) {
        if( ((Entity)(Object)this) instanceof MobEntity) {
            this.hasToBeLoaded = true;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void decrementTTL(CallbackInfo ci) {
        if( ((Entity)(Object)this) instanceof MobEntity) {
            if (this.hasToBeLoaded) {
                this.petherNortalFix$decrementLeftToLive();
            }
        }
    }

    @WrapWithCondition(method = "discard", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;remove(Lnet/minecraft/entity/Entity$RemovalReason;)V"))
    public boolean checkIfToDiscard(Entity instance, Entity.RemovalReason reason) {
        if( ((Entity)(Object)this) instanceof MobEntity) {
            return !this.hasToBeLoaded;
        }

        return true;
    }

    @Override
    public void petherNortalFix$setTicksLeftToLive(int ttl) {
        this.ticksToLive = ttl;
    }

    @Override
    public void petherNortalFix$decrementLeftToLive() {
        if(this.ticksToLive > 0) {
            this.ticksToLive--;
            PetherNortalFix.toForceLoadChunks.add((Entity)(Object)this);
        }else {
            this.hasToBeLoaded = false;
            this.ticksToLive = 20 * 60;
            PetherNortalFix.toForceLoadChunks.remove((Entity)(Object)this);
        }
    }

    @Override
    public int petherNortalFix$getTicksLeftToLive() {
        return this.ticksToLive;
    }
}
