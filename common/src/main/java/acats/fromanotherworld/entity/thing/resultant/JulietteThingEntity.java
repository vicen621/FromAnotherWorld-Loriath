package acats.fromanotherworld.entity.thing.resultant;

import acats.fromanotherworld.entity.goal.AbsorbGoal;
import acats.fromanotherworld.entity.goal.FleeOnFireGoal;
import acats.fromanotherworld.entity.goal.ThingAttackGoal;
import acats.fromanotherworld.registry.EntityRegistry;
import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

import static acats.fromanotherworld.constants.Variants.JULIETTE;

public class JulietteThingEntity extends AbsorberThingEntity {

    public JulietteThingEntity(EntityType<? extends JulietteThingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        this.addThingTargets(false);
        this.goalSelector.add(0, new FleeOnFireGoal(this, 16.0F, 1.2, 1.5));
        this.goalSelector.add(1, new AbsorbGoal(this, STANDARD));
        this.goalSelector.add(2, new ThingAttackGoal(this, 1.0D, false));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0D));
    }

    public static DefaultAttributeContainer.Builder createJulietteThingAttributes(){
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7.0D).add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0D);
    }

    private <E extends GeoEntity> PlayState predicate(AnimationState<E> event) {
        if (this.isThingFrozen())
            return PlayState.STOP;
        if (event.isMoving()){
            if (this.isAttacking()){
                event.getController().setAnimation(RawAnimation.begin().thenLoop("animation.juliette_thing.chase"));
            }
            else{
                event.getController().setAnimation(RawAnimation.begin().thenLoop("animation.juliette_thing.walk"));
            }
        }
        else{
            event.getController().setAnimation(RawAnimation.begin().thenLoop("animation.juliette_thing.idle"));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void onDeath(DamageSource source) {
        if (random.nextInt(3) == 0){
            CrawlerEntity crawlerEntity = EntityRegistry.CRAWLER.get().create(this.world);
            if (crawlerEntity != null) {
                crawlerEntity.setPosition(this.getPos());
                crawlerEntity.initializeFrom(this);
                crawlerEntity.setVictimType(JULIETTE);
                this.world.spawnEntity(crawlerEntity);
            }
        }
        super.onDeath(source);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public Strength getFormStrength() {
        return Strength.STANDARD;
    }

    @Override
    public void grow(LivingEntity otherParent) {
        this.growInto(EntityRegistry.SPLIT_FACE.get());
    }
}
