/*
 * Blabber
 * Copyright (C) 2022-2024 Ladysnake
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
package org.ladysnake.blabber.impl.common.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import org.ladysnake.blabber.impl.common.settings.BlabberSetting;

import java.util.function.Supplier;

public class SettingArgumentType extends EnumArgumentType<BlabberSetting> {
    protected SettingArgumentType(Codec<BlabberSetting> codec, Supplier<BlabberSetting[]> valuesSupplier) {
        super(codec, valuesSupplier);
    }

    public static SettingArgumentType setting() {
        return new SettingArgumentType(BlabberSetting.CODEC, BlabberSetting::values);
    }

    public static BlabberSetting getSetting(CommandContext<ServerCommandSource> context, String id) {
        return context.getArgument(id, BlabberSetting.class);
    }
}
