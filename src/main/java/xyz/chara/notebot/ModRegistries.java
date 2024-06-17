/*
This file is part of Notebot.
Notebot is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
Notebot is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with Notebot. If not, see <https://www.gnu.org/licenses/>.
*/

package xyz.chara.notebot;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import xyz.chara.notebot.commands.NotebotPauseCommand;
import xyz.chara.notebot.commands.NotebotSeekCommand;
import xyz.chara.notebot.commands.NotebotStopCommand;
import xyz.chara.notebot.commands.info.NotebotInfoCommand;
import xyz.chara.notebot.commands.info.NotebotInfoSongCommand;
import xyz.chara.notebot.commands.loop.NotebotLoopCommand;
import xyz.chara.notebot.commands.loop.NotebotLoopDisableCommand;
import xyz.chara.notebot.commands.loop.NotebotLoopEnableCommand;
import xyz.chara.notebot.commands.play.NotebotPlayCommand;
import xyz.chara.notebot.commands.play.NotebotPlaySelectCommand;
import xyz.chara.notebot.commands.preview.NotebotPreviewPauseCommand;
import xyz.chara.notebot.commands.preview.NotebotPreviewStartCommand;
import xyz.chara.notebot.commands.preview.NotebotPreviewStartResumeCommand;
import xyz.chara.notebot.commands.preview.NotebotPreviewStopCommand;
import xyz.chara.notebot.commands.queue.NotebotQueueAddCommand;
import xyz.chara.notebot.commands.queue.NotebotQueueCleanCommand;
import xyz.chara.notebot.commands.queue.NotebotQueueCommand;
import xyz.chara.notebot.commands.queue.NotebotQueueRemoveCommand;
import xyz.chara.notebot.commands.tune.NotebotTuneStartCommand;
import xyz.chara.notebot.commands.tune.NotebotTuneStopCommand;

public class ModRegistries {
    public static void registerModStuff() {
        ModRegistries.registerCommands();
        ModRegistries.registerEvents();
    }

    private static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register(NotebotPauseCommand::register);
        ClientCommandRegistrationCallback.EVENT.register(NotebotStopCommand::register);
        ClientCommandRegistrationCallback.EVENT.register(NotebotSeekCommand::register);
        
        ClientCommandRegistrationCallback.EVENT.register(NotebotInfoCommand::register);
        ClientCommandRegistrationCallback.EVENT.register(NotebotInfoSongCommand::register);

        ClientCommandRegistrationCallback.EVENT.register(NotebotPlayCommand::register);
        ClientCommandRegistrationCallback.EVENT.register(NotebotPlaySelectCommand::register);

        ClientCommandRegistrationCallback.EVENT.register(NotebotTuneStartCommand::register);
        ClientCommandRegistrationCallback.EVENT.register(NotebotTuneStopCommand::register);

        ClientCommandRegistrationCallback.EVENT.register(NotebotPreviewStartCommand::register);
        ClientCommandRegistrationCallback.EVENT.register(NotebotPreviewStartResumeCommand::register);
        ClientCommandRegistrationCallback.EVENT.register(NotebotPreviewPauseCommand::register);
        ClientCommandRegistrationCallback.EVENT.register(NotebotPreviewStopCommand::register);

        ClientCommandRegistrationCallback.EVENT.register(NotebotQueueCleanCommand::register);
        ClientCommandRegistrationCallback.EVENT.register(NotebotQueueAddCommand::register);
        ClientCommandRegistrationCallback.EVENT.register(NotebotQueueRemoveCommand::register);
        ClientCommandRegistrationCallback.EVENT.register(NotebotQueueCommand::register);

        ClientCommandRegistrationCallback.EVENT.register(NotebotLoopCommand::register);
        ClientCommandRegistrationCallback.EVENT.register(NotebotLoopEnableCommand::register);
        ClientCommandRegistrationCallback.EVENT.register(NotebotLoopDisableCommand::register);
    }

    private static void registerEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(NotebotPlayer::onTick);
        ClientTickEvents.END_CLIENT_TICK.register(NotebotTuneStartCommand::onTick);
        ClientTickEvents.END_CLIENT_TICK.register(NotebotPreviewStartCommand::onTick);
    }
}
