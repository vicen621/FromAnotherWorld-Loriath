package acats.fromanotherworld.entity.thing.resultant;

import acats.fromanotherworld.entity.interfaces.PossibleDisguisedThing;
import acats.fromanotherworld.entity.interfaces.TentacleThing;
import acats.fromanotherworld.entity.render.thing.Tentacle;
import acats.fromanotherworld.entity.thing.ThingEntity;
import acats.fromanotherworld.registry.ParticleRegistry;
import acats.fromanotherworld.tags.EntityTags;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class AbsorberThingEntity extends ThingEntity implements TentacleThing {
    public static final int ABSORB_TIME = 120;

    public static final Predicate<LivingEntity> STANDARD = (livingEntity) ->
            (livingEntity.getType().isIn(EntityTags.HUMANOIDS) || livingEntity.getType().isIn(EntityTags.QUADRUPEDS)) &&
                    !((PossibleDisguisedThing) livingEntity).isAssimilated();

    private static final TrackedData<Integer> ABSORB_PROGRESS;
    private static final TrackedData<Integer> ABSORB_TARGET_ID;
    public final List<Tentacle> absorbTentacles;

    @Override
    public float tentacleOriginOffset() {
        return this.getHeight() * 0.5F;
    }

    protected AbsorberThingEntity(EntityType<? extends AbsorberThingEntity> entityType, World world, boolean canHaveSpecialAbilities) {
        super(entityType, world, canHaveSpecialAbilities);
        absorbTentacles = new ArrayList<>();
        for (int i = 0; i < 25; i++){
            absorbTentacles.add(new Tentacle(this,
                    60,
                    new Vec3d(this.getRandom().nextDouble() - 0.5D, this.getRandom().nextDouble(), this.getRandom().nextDouble() - 0.5D)));
        }
    }
    protected AbsorberThingEntity(EntityType<? extends AbsorberThingEntity> entityType, World world){
        this(entityType, world, true);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(ABSORB_PROGRESS, 0);
        this.dataTracker.startTracking(ABSORB_TARGET_ID, 0);
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity absorbTarget = this.getAbsorbTarget();
        if (absorbTarget != null && this.getAbsorbProgress() > 0 && absorbTarget.isAlive()){
            this.tickAbsorb(absorbTarget);
        }
        else if (this.getWorld().isClient()){
            for (Tentacle tentacle:
                    absorbTentacles) {
                tentacle.tick(null);
            }
        }
    }

    public void tickAbsorb(@NotNull LivingEntity victim){
        if (this.getWorld().isClient()){
            for(int i = 0; i < this.getAbsorbProgress() / 10; ++i) {
                this.getWorld().addParticle(ParticleRegistry.THING_GORE, victim.getParticleX(0.6D), victim.getRandomBodyY(), victim.getParticleZ(0.6D), 0.0D, 0.0D, 0.0D);
            }
            for (Tentacle tentacle:
                    absorbTentacles) {
                tentacle.tick(victim);
            }
        }
        else{
            this.updateTether(victim);
        }
    }

    private void updateTether(@NotNull LivingEntity victim){
        float f = this.distanceTo(victim);
        if (f > 2.0F){
            double d = (this.getX() - victim.getX()) / (double)f;
            double e = (this.getY() - victim.getY()) / (double)f;
            double g = (this.getZ() - victim.getZ()) / (double)f;
            victim.setVelocity(victim.getVelocity().add(Math.copySign(d * d * 0.1, d), Math.copySign(e * e * 0.1, e), Math.copySign(g * g * 0.1, g)));
        }
    }

    public void setAbsorbTarget(LivingEntity absorbTarget){
        this.dataTracker.set(ABSORB_TARGET_ID, absorbTarget.getId());
    }
    public void setAbsorbTargetID(int absorbTargetID){
        this.dataTracker.set(ABSORB_TARGET_ID, absorbTargetID);
    }

    @Nullable
    public LivingEntity getAbsorbTarget(){
        return (LivingEntity) this.getWorld().getEntityById(this.getAbsorbTargetID());
    }
    public int getAbsorbTargetID(){
        return this.dataTracker.get(ABSORB_TARGET_ID);
    }

    public void setAbsorbProgress(int absorbProgress){
        this.dataTracker.set(ABSORB_PROGRESS, absorbProgress);
    }

    public int getAbsorbProgress(){
        return this.dataTracker.get(ABSORB_PROGRESS);
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        if (target.equals(this.getAbsorbTarget()))
            return false;
        return super.canTarget(target);
    }

    public abstract void grow(LivingEntity otherParent);

    public void growInto(EntityType<? extends ThingEntity> next){
        if (next != null){
            ThingEntity nextThing = next.create(this.getWorld());
            if (nextThing != null){
                nextThing.setPosition(this.getPos());
                nextThing.initializeFrom(this);
                this.getWorld().spawnEntity(nextThing);
                if (this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING))
                    nextThing.grief(0, 1);
            }
        }
        this.discard();
    }

    static {
        ABSORB_PROGRESS = DataTracker.registerData(AbsorberThingEntity.class, TrackedDataHandlerRegistry.INTEGER);
        ABSORB_TARGET_ID = DataTracker.registerData(AbsorberThingEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }
}
