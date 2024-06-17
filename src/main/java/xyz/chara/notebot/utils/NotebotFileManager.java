/*
This file is part of Notebot.
Notebot is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
Notebot is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with Notebot. If not, see <https://www.gnu.org/licenses/>.
*/

package xyz.chara.notebot.utils;

import org.slf4j.Logger;

import xyz.chara.notebot.Notebot;

import java.nio.file.Files;
import java.nio.file.Path;

public class NotebotFileManager {

    private static Path dir;
    private static Logger logger = Notebot.LOGGER;

    public static void init() {
        dir = Path.of("notebot/");
        var _dir = Path.of("notebot/songs");
        if (!_dir.toFile().exists()) {
            _dir.toFile().mkdirs();
        }
    }

    public static Path getDir() {
        return dir;
    }

    public static void createFile(String path) {
        try {
            if (!fileExists(path)) {
                getDir().resolve(path).getParent().toFile().mkdirs();
                Files.createFile(getDir().resolve(path));
            }
        } catch (Exception e) {
            logger.error("Error Creating File: " + path, e);
        }
    }

    public static boolean fileExists(String path) {
        try {
            return getDir().resolve(path).toFile().exists();
        } catch (Exception e) {
            return false;
        }
    }
}