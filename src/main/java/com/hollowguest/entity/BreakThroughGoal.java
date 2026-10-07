package com.hollowguest.entity;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/**
 * While hunting, if the Hollow Guest gets stuck, it opens any door in its way
 * and breaks other blocks between it and its target, one at a time.
 *
 * Needs the mobGriefing game rule on. Won't break unbreakable or very tough
 * blocks (like bedrock and obsidian), or blocks that hold items (like chests).
 */
public class BreakThroughGoal extends Goal {
    /** Blocks this hard or harder are left alone (obsidian is 50). */
    private static final float MAX_HARDNESS = 50.0F;
    /** Ticks without getting closer before it counts as stuck. */
    private static final int STUCK_TICKS = 30;
    /** How many blocks ahead it looks for something to break. */
    private static final double REACH = 2.5;

    private final HollowGuest guest;
    private BlockPos targetPos;
    private int breakTime;
    private int breakProgress;
    private int lastStage = -1;

    private double bestDistance = Double.MAX_VALUE;
    private int ticksWithoutProgress;

    public BreakThroughGoal(HollowGuest guest) {
        this.guest = guest;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    /** Called every tick by the entity so it knows when it is stuck. */
    public void trackProgress() {
        LivingEntity target = this.guest.getTarget();
        if (target == null || !this.guest.isNoticed()) {
            this.bestDistance = Double.MAX_VALUE;
            this.ticksWithoutProgress = 0;
            return;
        }
        double distance = this.guest.distanceToSqr(target);
        if (distance < this.bestDistance - 0.25) {
            this.bestDistance = distance;
            this.ticksWithoutProgress = 0;
        } else {
            this.ticksWithoutProgress++;
        }
    }

    private boolean isStuck() {
        return this.ticksWithoutProgress > STUCK_TICKS;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.guest.getTarget();
        if (!this.guest.isNoticed() || target == null || !target.isAlive()) {
            return false;
        }
        if (this.guest.distanceToSqr(target) < 4.0 || !this.isStuck()) {
            return false;
        }
        if (!(this.guest.level() instanceof ServerLevel level) || !EventHooks.canEntityGrief(level, this.guest)) {
            return false;
        }
        this.targetPos = this.findBlockInTheWay(target);
        return this.targetPos != null;
    }

    @Override
    public void start() {
        BlockState state = this.guest.level().getBlockState(this.targetPos);
        if (state.getBlock() instanceof DoorBlock door) {
            if (!door.isOpen(state)) {
                door.setOpen(this.guest, this.guest.level(), state, this.targetPos, true);
            }
            this.finish();
            return;
        }
        float hardness = state.getDestroySpeed(this.guest.level(), this.targetPos);
        this.breakTime = Mth.clamp((int) (hardness * 30.0F), 10, 240);
        this.breakProgress = 0;
        this.lastStage = -1;
        this.guest.getNavigation().stop();
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetPos != null
            && this.breakProgress <= this.breakTime
            && this.guest.isNoticed()
            && this.guest.getTarget() != null
            && isBreakable(this.guest.level().getBlockState(this.targetPos), this.guest, this.targetPos)
            && this.guest.blockPosition().closerToCenterThan(Vec3.atCenterOf(this.targetPos), REACH + 1.5);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.targetPos == null) {
            return;
        }
        this.guest.getLookControl().setLookAt(Vec3.atCenterOf(this.targetPos));
        if (this.guest.getRandom().nextInt(15) == 0) {
            this.guest.swing(this.guest.getUsedItemHand());
            this.guest.level().levelEvent(1019, this.targetPos, 0);
        }

        this.breakProgress++;
        int stage = (int) ((float) this.breakProgress / this.breakTime * 10.0F);
        if (stage != this.lastStage) {
            this.guest.level().destroyBlockProgress(this.guest.getId(), this.targetPos, stage);
            this.lastStage = stage;
        }

        if (this.breakProgress >= this.breakTime) {
            this.guest.level().destroyBlock(this.targetPos, true, this.guest);
            this.finish();
        }
    }

    @Override
    public void stop() {
        if (this.targetPos != null) {
            this.guest.level().destroyBlockProgress(this.guest.getId(), this.targetPos, -1);
        }
        this.targetPos = null;
    }

    private void finish() {
        if (this.targetPos != null) {
            this.guest.level().destroyBlockProgress(this.guest.getId(), this.targetPos, -1);
        }
        this.targetPos = null;
        this.ticksWithoutProgress = STUCK_TICKS - 10;
    }

    /** Walk from its body toward the target and return the first block that's in the way. */
    private BlockPos findBlockInTheWay(LivingEntity target) {
        Vec3 from = this.guest.position();
        Vec3 toTarget = target.position().subtract(from);
        Vec3 flat = new Vec3(toTarget.x, 0, toTarget.z);
        if (flat.lengthSqr() < 1.0E-4) {
            return null;
        }
        Vec3 step = flat.normalize().scale(0.5);
        int height = Mth.ceil(this.guest.getBbHeight());
        // Going up or down toward the target? Check one extra block that way.
        int minY = toTarget.y < -1.0 ? -1 : 0;
        int maxY = toTarget.y > 1.0 ? height : height - 1;

        // First pass: open any closed door in the way. Second pass: break a block.
        for (int pass = 0; pass < 2; pass++) {
            boolean doorsOnly = pass == 0;
            Vec3 point = from;
            for (double travelled = 0; travelled <= REACH; travelled += 0.5) {
                point = point.add(step);
                BlockPos base = BlockPos.containing(point);
                for (int dy = doorsOnly ? -2 : minY; dy <= maxY; dy++) {
                    BlockPos pos = base.above(dy);
                    BlockState state = this.guest.level().getBlockState(pos);
                    boolean isDoor = state.getBlock() instanceof DoorBlock;
                    if (isDoor == doorsOnly && isBreakable(state, this.guest, pos)) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isBreakable(BlockState state, HollowGuest guest, BlockPos pos) {
        if (state.isAir() || state.getCollisionShape(guest.level(), pos).isEmpty()) {
            return false;
        }
        if (state.getBlock() instanceof DoorBlock door) {
            return !door.isOpen(state);
        }
        if (state.hasBlockEntity()) {
            return false;
        }
        float hardness = state.getDestroySpeed(guest.level(), pos);
        return hardness >= 0.0F && hardness < MAX_HARDNESS;
    }
}
