package com.hollowguest;

import com.hollowguest.entity.HollowGuest;
import java.util.Locale;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(HollowGuestMod.MOD_ID)
public class HollowGuestMod {
    public static final String MOD_ID = "hollowguest";

    public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<HollowGuest>> HOLLOW_GUEST =
        ENTITY_TYPES.registerEntityType("hollow_guest", HollowGuest::new, MobCategory.MONSTER,
            builder -> builder.sized(0.6F, 3.25F).eyeHeight(3.0F).clientTrackingRange(10));

    public static final DeferredItem<SpawnEggItem> HOLLOW_GUEST_SPAWN_EGG =
        ITEMS.registerItem("hollow_guest_spawn_egg", SpawnEggItem::new,
            properties -> properties.spawnEgg(HOLLOW_GUEST.get()));

    public HollowGuestMod(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(HollowGuestMod::onAttributes);
        modEventBus.addListener(HollowGuestMod::onSpawnPlacements);
        modEventBus.addListener(HollowGuestMod::onCreativeTabs);
        NeoForge.EVENT_BUS.addListener(HollowGuestMod::onChat);
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(HOLLOW_GUEST.get(), HollowGuest.createAttributes().build());
    }

    private static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(HOLLOW_GUEST.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private static void onCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(HOLLOW_GUEST_SPAWN_EGG);
        }
    }

    /** Saying its name in chat counts as acknowledging it. */
    private static void onChat(ServerChatEvent event) {
        String text = event.getRawText().toLowerCase(Locale.ROOT);
        if (text.contains("hollow guest") || text.contains("hollowguest")) {
            ServerPlayer player = event.getPlayer();
            player.level().getEntitiesOfClass(HollowGuest.class, player.getBoundingBox().inflate(128.0), HollowGuest::isAlive)
                .forEach(guest -> guest.notice(player));
        }
    }
}
