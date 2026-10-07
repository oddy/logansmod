package com.hollowguest.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * When the normal path-finding can't find a way to the target (walls, closed houses),
 * the Hollow Guest walks straight at it anyway. That brings it up against the wall,
 * where it climbs or {@link BreakThroughGoal} breaks a way in. As soon as a real path
 * opens up, the normal melee attack goal (higher priority) takes over again.
 */
public class DirectChaseGoal extends Goal {
    private static final int RECHECK_TICKS = 20;

    private final HollowGuest guest;
    private final double speed;
    private int cooldown;

    public DirectChaseGoal(HollowGuest guest, double speed) {
        this.guest = guest;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean hasTarget() {
        LivingEntity target = this.guest.getTarget();
        return this.guest.isNoticed() && target != null && target.isAlive() && this.guest.distanceToSqr(target) > 2.0;
    }

    @Override
    public boolean canUse() {
        if (!this.hasTarget() || --this.cooldown > 0) {
            return false;
        }
        this.cooldown = RECHECK_TICKS;
        return this.guest.getNavigation().createPath(this.guest.getTarget(), 0) == null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.hasTarget();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.guest.getTarget();
        if (target == null) {
            return;
        }
        this.guest.getLookControl().setLookAt(target, 30.0F, 30.0F);
        this.guest.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), this.speed);
    }
}
