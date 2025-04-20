/*
 * Blabber
 * Copyright (C) 2022-2025 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package org.ladysnake.blabber.impl.common.settings;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum BlabberSetting implements StringIdentifiable {
    DEBUG_ANCHORS("debug.anchors");
    public static final Codec<BlabberSetting> CODEC = StringIdentifiable.createBasicCodec(BlabberSetting::values);
    private static final Map<String, BlabberSetting> index = new HashMap<>();

    static {
        for (BlabberSetting value : values()) {
            index.put(value.id, value);
        }
    }

    public static @Nullable BlabberSetting getById(String id) {
        return index.get(id);
    }

    private final String id;

    BlabberSetting(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    @Override
    public String asString() {
        return this.id;
    }
}
