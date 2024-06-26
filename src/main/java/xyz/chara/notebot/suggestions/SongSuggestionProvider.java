/*
This file is part of Notebot.
Notebot is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
Notebot is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with Notebot. If not, see <https://www.gnu.org/licenses/>.
*/

package xyz.chara.notebot.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import xyz.chara.notebot.utils.NotebotFileManager;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

// Old Todo: Song caching to make it faster
// I don't think it's really necessary.
public class SongSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context,
            SuggestionsBuilder builder) throws CommandSyntaxException {
        String keyword = builder.getInput().substring(builder.getStart());

        for (String f : getSongs(keyword.toLowerCase())) {
            builder.suggest(f);
        }

        return builder.buildFuture();
    }

    public static Set<String> getSongs(String keyword) {
        Set<String> files = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        if (!files.isEmpty()) {
            files.clear();
        }

        NotebotFileManager.getDir().resolve("songs/").toFile().mkdirs();

        if (keyword.isEmpty()) {
            files.addAll(Arrays.asList(NotebotFileManager.getDir().resolve("songs/").toFile().list()));
            return files;
        }

        for (String f : NotebotFileManager.getDir().resolve("songs/").toFile().list()) {
            if (f.toLowerCase().contains(keyword)) {
                files.add(f);
            }
        }

        return files;
    }
}
