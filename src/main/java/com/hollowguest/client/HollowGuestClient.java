package com.hollowguest.client;

import com.hollowguest.HollowGuestMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = HollowGuestMod.MOD_ID, dist = Dist.CLIENT)
public class HollowGuestClient {
    public static final ModelLayerLocation MODEL_LAYER =
        new ModelLayerLocation(Identifier.fromNamespaceAndPath(HollowGuestMod.MOD_ID, "hollow_guest"), "main");

    public HollowGuestClient(IEventBus modEventBus) {
        modEventBus.addListener(HollowGuestClient::onLayers);
        modEventBus.addListener(HollowGuestClient::onRenderers);
    }

    private static void onLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MODEL_LAYER, HollowGuestModel::createBodyLayer);
    }

    private static void onRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(HollowGuestMod.HOLLOW_GUEST.get(), HollowGuestRenderer::new);
    }
}
