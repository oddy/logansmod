package com.hollowguest.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The Hollow Guest. It ignores you until you acknowledge it: look straight at it,
 * say its name in chat, or hurt it. Then it hunts you, opening and breaking doors
 * and climbing walls to reach you.
 */
public class HollowGuest extends Monster {
    private static final EntityDataAccessor<Boolean> DATA_NOTICED =
        SynchedEntityData.defineId(HollowGuest.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CLIMBING =
        SynchedEntityData.defineId(HollowGuest.class, EntityDataSerializers.BOOLEAN);

    /** How far away a player can be and still "notice" it by looking. */
    public static final double NOTICE_RANGE = 64.0;
    /** Ticks with nobody to chase before it goes back to waiting (20 ticks = 1 second). */
    public static final int CALM_DOWN_TICKS = 400;

    private int calmTicks;

    public HollowGuest(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 20;
        this.getNavigation().setCanOpenDoors(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 60.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.ATTACK_DAMAGE, 8.0)
            .add(Attributes.FOLLOW_RANGE, 48.0)
            .add(Attributes.STEP_HEIGHT, 1.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_NOTICED, false);
        builder.define(DATA_CLIMBING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreakDoorGoal(this, difficulty -> true) {
            @Override
            public boolean canUse() {
                return HollowGuest.this.isNoticed() && super.canUse();
            }
        });
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, false));
        this.goalSelector.addGoal(3, new OpenDoorGoal(this, false));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 24.0F) {
            @Override
            public boolean canUse() {
                return !HollowGuest.this.isNoticed() && super.canUse();
            }
        });
    }

    public boolean isNoticed() {
        return this.entityData.get(DATA_NOTICED);
    }

    private void setNoticed(boolean noticed) {
        this.entityData.set(DATA_NOTICED, noticed);
    }

    /** Something acknowledged the Hollow Guest. It starts hunting {@code who}. */
    public void notice(LivingEntity who) {
        if (!this.isNoticed()) {
            this.setNoticed(true);
            this.playSound(SoundEvents.ENDERMAN_STARE, 2.0F, 0.5F);
        }
        this.calmTicks = 0;
        if (who != null && who.isAlive() && !(who instanceof Player player && (player.isSpectator() || player.isCreative()))) {
            this.setTarget(who);
        }
    }

    private boolean isBeingWatchedBy(Player player) {
        return this.distanceToSqr(player) < NOTICE_RANGE * NOTICE_RANGE
            && this.isLookingAtMe(player, 0.025, true, false, this.getEyeY());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        this.entityData.set(DATA_CLIMBING, this.horizontalCollision);

        if (!this.isNoticed()) {
            if (this.tickCount % 3 == 0) {
                for (Player player : this.level().players()) {
                    if (!player.isSpectator() && this.isBeingWatchedBy(player)) {
                        this.notice(player);
                        break;
                    }
                }
            }
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            Player closest = this.level().getNearestPlayer(this, 48.0);
            if (closest != null && !closest.isSpectator() && !closest.isCreative()) {
                this.setTarget(closest);
                this.calmTicks = 0;
            } else if (++this.calmTicks > CALM_DOWN_TICKS) {
                this.setNoticed(false);
                this.setTarget(null);
                this.calmTicks = 0;
            }
        } else {
            this.calmTicks = 0;
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && source.getEntity() instanceof LivingEntity attacker) {
            this.notice(attacker);
        }
        return hurt;
    }

    @Override
    public boolean onClimbable() {
        return this.entityData.get(DATA_CLIMBING);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SKELETON_STEP, 0.15F, 0.8F);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Noticed", this.isNoticed());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setNoticed(input.getBooleanOr("Noticed", false));
    }
}
