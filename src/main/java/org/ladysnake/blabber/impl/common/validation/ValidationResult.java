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
package org.ladysnake.blabber.impl.common.validation;

import java.util.List;
import java.util.stream.Collectors;

public sealed interface ValidationResult {
    static Success success() {
        return Success.INSTANCE;
    }

    final class Success implements ValidationResult {
        public static final Success INSTANCE = new Success();
    }

    record Warnings(List<Warning> warnings) implements ValidationResult {
        public String message() {
            return warnings().stream().map(Warning::message).collect(Collectors.joining(", "));
        }
    }

    sealed interface Warning {
        String state();

        String message();

        record Unreachable(String state) implements Warning {
            @Override
            public String message() {
                return state() + " is unreachable";
            }
        }

        record ConditionalSoftLock(String state) implements Warning {
            @Override
            public String message() {
                return state() + " only has conditional paths to the end of the dialogue";
            }
        }
    }

    sealed interface Error extends ValidationResult {
        String state();

        String message();

        record NoChoice(String state) implements Error {
            @Override
            public String message() {
                return state() + " has no available choices but is not an end state";
            }
        }

        record SoftLock(String state) implements Error {
            @Override
            public String message() {
                return state() + " does not have any path to the end of the dialogue";
            }
        }

        record NonexistentIllustration(String state, String illustration) implements Error {
            @Override
            public String message() {
                return state() + " references non-existent illustration " + illustration();
            }
        }
    }
}
