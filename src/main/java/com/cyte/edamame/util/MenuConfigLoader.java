/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.util;

import com.cyte.edamame.editor.MenuBarPriority;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

public class MenuConfigLoader {
    public static Map<String, MenuBarPriority> loadMenuConfig() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        try (InputStream inputStream = MenuConfigLoader.class.
                getResourceAsStream("resources/com/cyte/edamame/menuconfig/menu_config.json")) {
            if (inputStream == null) {
                throw new FileNotFoundException("menu_config.json note found");
            }
            return objectMapper.readValue(inputStream, new TypeReference<>() {
            });
        } catch (FileNotFoundException exception) {
            throw exception;
            // Need to make a popup of some sort to let the user know that the json file does not exist.
        } catch (Exception exception) {
            throw new InvalidConfigurationException("Error parsing or processing configuration JSON file.", exception);
        }
    }

    public static class InvalidConfigurationException extends Exception {
        public InvalidConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
