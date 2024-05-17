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

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.blabber.impl.common.settings.BlabberSetting;
import org.ladysnake.blabber.impl.common.settings.BlabberSettingsComponent;

import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class SettingsSubCommand {
    public static final String SETTINGS_SUBCOMMAND = "settings";
    public static final String SETTINGS_SET_SUBCOMMAND = "set";
    public static final @NotNull Predicate<ServerCommandSource> ALLOW_DEBUG = Permissions.require("dialogue.debug", 2);

    static LiteralArgumentBuilder<ServerCommandSource> settingsSubtree() {
        return literal(SETTINGS_SUBCOMMAND)
                .requires(ALLOW_DEBUG)
                .then(literal(SETTINGS_SET_SUBCOMMAND).then(
                        argument("setting", SettingArgumentType.setting())
                                .then(argument("value", BoolArgumentType.bool())
                                        .executes(context -> setEnabled(context.getSource(), context.getSource().getPlayerOrThrow(), SettingArgumentType.getSetting(context, "setting"), BoolArgumentType.getBool(context, "value")))
                                )
                ));
    }

    private static int setEnabled(ServerCommandSource source, ServerPlayerEntity player, BlabberSetting setting, boolean enabled) {
        BlabberSettingsComponent.get(player).setEnabled(setting, enabled);
        source.sendFeedback(() -> Text.translatable(enabled ? "blabber:commands.setting.enabled" : "blabber:commands.setting.disabled", setting.id()), false);
        return 1;
    }
}
