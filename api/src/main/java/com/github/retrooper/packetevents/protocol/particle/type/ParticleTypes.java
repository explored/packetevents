/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.retrooper.packetevents.protocol.particle.type;

import com.github.retrooper.packetevents.protocol.particle.data.*;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.TypesBuilder;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ParticleTypes {
    private static final Map<String, ParticleType> PARTICLE_TYPE_MAP = new HashMap<>();
    private static final Map<Byte, Map<Integer, ParticleType>> PARTICLE_TYPE_ID_MAP = new HashMap<>();
    private static final TypesBuilder TYPES_BUILDER = new TypesBuilder("particle/particle_type_mappings");

    public static ParticleType define(String key, Function<PacketWrapper<?>, ParticleData> readDataFunction, BiConsumer<PacketWrapper<?>, ParticleData> writeDataFunction) {
        TypesBuilderData data = TYPES_BUILDER.define(key);
        ParticleType particleType = new ParticleType() {
            private final int[] ids = data.getData();

            @Override
            public ResourceLocation getName() {
                return data.getName();
            }

            @Override
            public int getId(ClientVersion version) {
                int index = TYPES_BUILDER.getDataIndex(version);
                return ids[index];
            }

            @Override
            public Function<PacketWrapper<?>, ParticleData> readDataFunction() {
                return readDataFunction;
            }

            @Override
            public BiConsumer<PacketWrapper<?>, ParticleData> writeDataFunction() {
                return writeDataFunction;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof ParticleType) {
                    return getName().equals(((ParticleType) obj).getName());
                }
                return false;
            }
        };

        PARTICLE_TYPE_MAP.put(particleType.getName().toString(), particleType);
        for (ClientVersion version : TYPES_BUILDER.getVersions()) {
            int index = TYPES_BUILDER.getDataIndex(version);
            Map<Integer, ParticleType> typeIdMap = PARTICLE_TYPE_ID_MAP.computeIfAbsent((byte) index, k -> new HashMap<>());
            typeIdMap.put(particleType.getId(version), particleType);
        }
        return particleType;
    }

    public static ParticleType define(String key) {
        //Define the particle type with empty functions
        return define(key, wrapper -> {
            //Empty particle data
            return new ParticleData();
        }, (wrapper, data) -> {
            //By default don't write any particle data
        });
    }

    //with minecraft:key
    public static ParticleType getByName(String name) {
        return PARTICLE_TYPE_MAP.get(name);
    }

    public static ParticleType getById(ClientVersion version, int id) {
        int index = TYPES_BUILDER.getDataIndex(version);
        Map<Integer, ParticleType> typeIdMap = PARTICLE_TYPE_ID_MAP.get((byte) index);
        return typeIdMap.get(id);
    }

    @Deprecated // Removed in 1.20.5
    public static final ParticleType AMBIENT_ENTITY_EFFECT = define("ambient_entity_effect");
    public static final ParticleType ANGRY_VILLAGER = define("angry_villager");
    public static final ParticleType BLOCK = define("block", ParticleBlockStateData::read, (wrapper, data) -> ParticleBlockStateData.write(wrapper, (ParticleBlockStateData) data));
    public static final ParticleType BLOCK_MARKER = define("block_marker", ParticleBlockStateData::read, (wrapper, data) -> ParticleBlockStateData.write(wrapper, (ParticleBlockStateData) data));
    public static final ParticleType BUBBLE = define("bubble");
    public static final ParticleType CLOUD = define("cloud");
    public static final ParticleType CRIT = define("crit");
    public static final ParticleType DAMAGE_INDICATOR = define("damage_indicator");
    public static final ParticleType DRAGON_BREATH = define("dragon_breath");
    public static final ParticleType DRIPPING_LAVA = define("dripping_lava");
    public static final ParticleType FALLING_LAVA = define("falling_lava");
    public static final ParticleType LANDING_LAVA = define("landing_lava");
    public static final ParticleType DRIPPING_WATER = define("dripping_water");
    public static final ParticleType FALLING_WATER = define("falling_water");
    public static final ParticleType DUST = define("dust", ParticleDustData::read, (wrapper, data) -> ParticleDustData.write(wrapper, (ParticleDustData) data));
    public static final ParticleType DUST_COLOR_TRANSITION = define("dust_color_transition", ParticleDustColorTransitionData::read, (wrapper, data) -> ParticleDustColorTransitionData.write(wrapper, (ParticleDustColorTransitionData) data));
    public static final ParticleType EFFECT = define("effect");
    public static final ParticleType ELDER_GUARDIAN = define("elder_guardian");
    public static final ParticleType ENCHANTED_HIT = define("enchanted_hit");
    public static final ParticleType ENCHANT = define("enchant");
    public static final ParticleType END_ROD = define("end_rod");
    public static final ParticleType ENTITY_EFFECT = define("entity_effect");
    public static final ParticleType EXPLOSION_EMITTER = define("explosion_emitter");
    public static final ParticleType EXPLOSION = define("explosion");
    public static final ParticleType SONIC_BOOM = define("sonic_boom");
    public static final ParticleType FALLING_DUST = define("falling_dust", ParticleBlockStateData::read, (wrapper, data) -> ParticleBlockStateData.write(wrapper, (ParticleBlockStateData) data));
    public static final ParticleType FIREWORK = define("firework");
    public static final ParticleType FISHING = define("fishing");
    public static final ParticleType FLAME = define("flame");
    public static final ParticleType SCULK_SOUL = define("sculk_soul");
    public static final ParticleType SCULK_CHARGE = define("sculk_charge", ParticleSculkChargeData::read, (wrapper, data) -> ParticleSculkChargeData.write(wrapper, (ParticleSculkChargeData) data));
    public static final ParticleType SCULK_CHARGE_POP = define("sculk_charge_pop");
    public static final ParticleType SOUL_FIRE_FLAME = define("soul_fire_flame");
    public static final ParticleType SOUL = define("soul");
    public static final ParticleType FLASH = define("flash");
    public static final ParticleType HAPPY_VILLAGER = define("happy_villager");
    public static final ParticleType COMPOSTER = define("composter");
    public static final ParticleType HEART = define("heart");
    public static final ParticleType INSTANT_EFFECT = define("instant_effect");
    public static final ParticleType ITEM = define("item", ParticleItemStackData::read, (wrapper, data) -> ParticleItemStackData.write(wrapper, (ParticleItemStackData) data));
    public static final ParticleType VIBRATION = define("vibration", ParticleVibrationData::read, (wrapper, data) -> ParticleVibrationData.write(wrapper, (ParticleVibrationData) data));
    public static final ParticleType ITEM_SLIME = define("item_slime");
    public static final ParticleType ITEM_SNOWBALL = define("item_snowball");
    public static final ParticleType LARGE_SMOKE = define("large_smoke");
    public static final ParticleType LAVA = define("lava");
    public static final ParticleType MYCELIUM = define("mycelium");
    public static final ParticleType NOTE = define("note");
    public static final ParticleType POOF = define("poof");
    public static final ParticleType PORTAL = define("portal");
    public static final ParticleType RAIN = define("rain");
    public static final ParticleType SMOKE = define("smoke");
    public static final ParticleType SNEEZE = define("sneeze");
    public static final ParticleType SPIT = define("spit");
    public static final ParticleType SQUID_INK = define("squid_ink");
    public static final ParticleType SWEEP_ATTACK = define("sweep_attack");
    public static final ParticleType TOTEM_OF_UNDYING = define("totem_of_undying");
    public static final ParticleType UNDERWATER = define("underwater");
    public static final ParticleType SPLASH = define("splash");
    public static final ParticleType WITCH = define("witch");
    public static final ParticleType BUBBLE_POP = define("bubble_pop");
    public static final ParticleType CURRENT_DOWN = define("current_down");
    public static final ParticleType BUBBLE_COLUMN_UP = define("bubble_column_up");
    public static final ParticleType NAUTILUS = define("nautilus");
    public static final ParticleType DOLPHIN = define("dolphin");
    public static final ParticleType CAMPFIRE_COSY_SMOKE = define("campfire_cosy_smoke");
    public static final ParticleType CAMPFIRE_SIGNAL_SMOKE = define("campfire_signal_smoke");
    public static final ParticleType DRIPPING_HONEY = define("dripping_honey");
    public static final ParticleType FALLING_HONEY = define("falling_honey");
    public static final ParticleType LANDING_HONEY = define("landing_honey");
    public static final ParticleType FALLING_NECTAR = define("falling_nectar");
    public static final ParticleType FALLING_SPORE_BLOSSOM = define("falling_spore_blossom");
    public static final ParticleType ASH = define("ash");
    public static final ParticleType CRIMSON_SPORE = define("crimson_spore");
    public static final ParticleType WARPED_SPORE = define("warped_spore");
    public static final ParticleType SPORE_BLOSSOM_AIR = define("spore_blossom_air");
    public static final ParticleType DRIPPING_OBSIDIAN_TEAR = define("dripping_obsidian_tear");
    public static final ParticleType FALLING_OBSIDIAN_TEAR = define("falling_obsidian_tear");
    public static final ParticleType LANDING_OBSIDIAN_TEAR = define("landing_obsidian_tear");
    public static final ParticleType REVERSE_PORTAL = define("reverse_portal");
    public static final ParticleType WHITE_ASH = define("white_ash");
    public static final ParticleType SMALL_FLAME = define("small_flame");
    public static final ParticleType SNOWFLAKE = define("snowflake");
    public static final ParticleType DRIPPING_DRIPSTONE_LAVA = define("dripping_dripstone_lava");
    public static final ParticleType FALLING_DRIPSTONE_LAVA = define("falling_dripstone_lava");
    public static final ParticleType DRIPPING_DRIPSTONE_WATER = define("dripping_dripstone_water");
    public static final ParticleType FALLING_DRIPSTONE_WATER = define("falling_dripstone_water");
    public static final ParticleType GLOW_SQUID_INK = define("glow_squid_ink");
    public static final ParticleType GLOW = define("glow");
    public static final ParticleType WAX_ON = define("wax_on");
    public static final ParticleType WAX_OFF = define("wax_off");
    public static final ParticleType ELECTRIC_SPARK = define("electric_spark");
    public static final ParticleType SCRAPE = define("scrape");
    public static final ParticleType SHRIEK = define("shriek", ParticleShriekData::read, (wrapper, data) -> ParticleShriekData.write(wrapper, (ParticleShriekData) data));
    //Added in 1.19.3, BUT REMOVED in 1.20, replaced with CHERRY_LEAVES
    public static final ParticleType DRIPPING_CHERRY_LEAVES = define("dripping_cherry_leaves");
    public static final ParticleType FALLING_CHERRY_LEAVES = define("falling_cherry_leaves");
    public static final ParticleType LANDING_CHERRY_LEAVES = define("landing_cherry_leaves");

    // Added in 1.20
    public static final ParticleType CHERRY_LEAVES = define("cherry_leaves");
    public static final ParticleType EGG_CRACK = define("egg_crack");

    // Added in 1.20.3
    public static final ParticleType GUST = define("gust");
    @Deprecated // Replaced with GUST_EMITTER_LARGE/GUST_EMITTER_SMALL in 1.20.5
    public static final ParticleType GUST_EMITTER = define("gust_emitter");
    public static final ParticleType WHITE_SMOKE = define("white_smoke");
    public static final ParticleType DUST_PLUME = define("dust_plume");
    public static final ParticleType GUST_DUST = define("gust_dust");
    public static final ParticleType TRIAL_SPAWNER_DETECTION = define("trial_spawner_detection");

    // Added in 1.20.5
    public static final ParticleType SMALL_GUST = define("small_gust");
    public static final ParticleType GUST_EMITTER_LARGE = define("gust_emitter_large");
    public static final ParticleType GUST_EMITTER_SMALL = define("gust_emitter_small");
    public static final ParticleType INFESTED = define("infested");
    public static final ParticleType ITEM_COBWEB = define("item_cobweb");
    public static final ParticleType TRIAL_SPAWNER_DETECTION_OMINOUS = define("trial_spawner_detection_ominous");
    public static final ParticleType VAULT_CONNECTION = define("vault_connection");
    public static final ParticleType DUST_PILLAR = define("dust_pillar");
    public static final ParticleType OMINOUS_SPAWNING = define("ominous_spawning");
    public static final ParticleType RAID_OMEN = define("raid_omen");
    public static final ParticleType TRIAL_OMEN = define("trial_omen");

    static {
        TYPES_BUILDER.unloadFileMappings();
    }
}
