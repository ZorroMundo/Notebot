/*
This file is part of Notebot.
Notebot is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
Notebot is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with Notebot. If not, see <https://www.gnu.org/licenses/>.
*/

package xyz.chara.notebot.commands.info;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import xyz.chara.notebot.NotebotPlayer;
import xyz.chara.notebot.types.Song;
import xyz.chara.notebot.utils.NotebotUtils;

import static xyz.chara.notebot.Notebot.mc;

import java.util.Map;

public class NotebotInfoCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> clientCommandSourceCommandDispatcher,
            CommandRegistryAccess commandRegistryAccess) {
        clientCommandSourceCommandDispatcher.register(
                ClientCommandManager.literal("notebot")
                        .then(ClientCommandManager.literal("info")
                                .executes(NotebotInfoCommand::run)));
    }

    public static String listRequirements(Song song) {
        StringBuilder result = new StringBuilder();

        result.append("§6Song: §e").append(song.filename);

        for (Map.Entry<NoteBlockInstrument, ItemStack> e : NotebotUtils.NoteBlockInstrument_TO_ITEM
                .entrySet()) {
            int count = (int) song.requirements.stream().filter(n -> n.NoteBlockInstrument == e.getKey().ordinal())
                    .count();

            if (count != 0) {
                result.append("\n§6- §e").append(e.getValue().getName().getString()).append(": §a").append(count);
            }
        }

        return result.toString();
    }

    private static int run(CommandContext<FabricClientCommandSource> context) {
        StringBuilder result = new StringBuilder();

        if (NotebotPlayer.song == null) {
            result.append("§6There's no song currently playing.");
        } else {
            result.append("§6Song: §a").append(NotebotPlayer.song.filename);

            result.append("\n§6Time elapsed: §a")
                    .append(Math.max(NotebotPlayer.timer, 0))
                    .append("§6 / §a")
                    .append(NotebotPlayer.song.length).append("§6 ticks.");
        }

        mc.player.sendMessage(Text.literal(result.toString()));

        return 1;
    }
}
