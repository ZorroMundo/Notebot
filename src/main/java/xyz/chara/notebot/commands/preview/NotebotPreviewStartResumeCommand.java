/*
This file is part of Notebot.
Notebot is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
Notebot is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with Notebot. If not, see <https://www.gnu.org/licenses/>.
*/

package xyz.chara.notebot.commands.preview;

import static xyz.chara.notebot.Notebot.mc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

public class NotebotPreviewStartResumeCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> clientCommandSourceCommandDispatcher,
            CommandRegistryAccess commandRegistryAccess) {
        clientCommandSourceCommandDispatcher.register(
                ClientCommandManager.literal("notebot")
                        .then(ClientCommandManager.literal("preview")
                                .then(ClientCommandManager.literal("play")
                                        .executes(NotebotPreviewStartResumeCommand::run))));
    }

    private static int run(CommandContext<FabricClientCommandSource> context) {
        if (NotebotPreviewStartCommand.song == null) {
            mc.player.sendMessage(
                    Text.literal("§6There's no preview songs currently playing."));
        } else {
            if (!NotebotPreviewStartCommand.playing) {
                mc.player.sendMessage(
                        Text.literal("§6Resumed the preview of §a" + NotebotPreviewStartCommand.song.filename + "§6."));
                NotebotPreviewStartCommand.playing = true;
            }
        }
        
        return 1;
    }
}
