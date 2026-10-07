package com.hollowguest.client;

import com.hollowguest.HollowGuestMod;
import com.hollowguest.entity.HollowGuest;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class HollowGuestRenderer extends MobRenderer<HollowGuest, HollowGuestRenderState, HollowGuestModel> {
    private static final Identifier TEXTURE =
        Identifier.fromNamespaceAndPath(HollowGuestMod.MOD_ID, "textures/entity/hollow_guest.png");

    public HollowGuestRenderer(EntityRendererProvider.Context context) {
        super(context, new HollowGuestModel(context.bakeLayer(HollowGuestClient.MODEL_LAYER)), 0.5F);
    }

    @Override
    public HollowGuestRenderState createRenderState() {
        return new HollowGuestRenderState();
    }

    @Override
    public void extractRenderState(HollowGuest entity, HollowGuestRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.noticed = entity.isNoticed();
        state.attacking = entity.isAggressive();
    }

    @Override
    public Identifier getTextureLocation(HollowGuestRenderState state) {
        return TEXTURE;
    }
}
