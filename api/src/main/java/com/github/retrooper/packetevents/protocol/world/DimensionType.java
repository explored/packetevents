/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2021 retrooper and contributors
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

package com.github.retrooper.packetevents.protocol.world;

import org.jetbrains.annotations.Nullable;

@Deprecated // This is a registry and cannot be hardcoded in modern versions
public enum DimensionType {
    NETHER(-1, "minecraft:the_nether"),
    OVERWORLD(0, "minecraft:overworld"),
    END(1, "minecraft:the_end"),
    CUSTOM(-999, "minecraft:custom");

    private static final DimensionType[] VALUES = values();

    private final int id;
    private final String name;

    DimensionType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static DimensionType getById(int id) {
        return VALUES[id + 1];
    }

    @Nullable
    public static DimensionType getByName(String name) {
        for (DimensionType type : VALUES) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static boolean isFlat(String levelType) {
        return WorldType.FLAT.getName().equals(levelType);
    }

    public static boolean isDebug(String levelType) {
        return WorldType.DEBUG_ALL_BLOCK_STATES.getName().equals(levelType);
    }
}
