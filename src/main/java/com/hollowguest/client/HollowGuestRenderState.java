package com.hollowguest.client;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class HollowGuestRenderState extends LivingEntityRenderState {
    /** True once it has noticed someone and is hunting. */
    public boolean noticed;
    public boolean attacking;
}
