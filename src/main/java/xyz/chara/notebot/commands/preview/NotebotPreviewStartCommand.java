/*
This file is part of Notebot.
Notebot is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
Notebot is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with Notebot. If not, see <https://www.gnu.org/licenses/>.
*/

package xyz.chara.notebot.commands.preview;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import xyz.chara.notebot.NotebotPlayer;
import xyz.chara.notebot.suggestions.SongSuggestionProvider;
import xyz.chara.notebot.types.Note;
import xyz.chara.notebot.types.Song;
import xyz.chara.notebot.utils.NotebotFileManager;
import xyz.chara.notebot.utils.NotebotUtils;

import static xyz.chara.notebot.Notebot.mc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotebotPreviewStartCommand {
    /* Status */
    public static boolean playing = false;

    /* Some settings */
    public static boolean loop = false;

    /* The loaded song */
    public static Song song;
    public static List<String> trail = new ArrayList<>();
    public static List<String> queue = new ArrayList<>();

    /* Map of noteblocks and their pitch around the player [blockpos:pitch] */
    private static Map<BlockPos, Integer> blockPitches = new HashMap<>();
    private static int timer = -10;

    public static void register(CommandDispatcher<FabricClientCommandSource> clientCommandSourceCommandDispatcher,
            CommandRegistryAccess commandRegistryAccess) {
        clientCommandSourceCommandDispatcher.register(
                ClientCommandManager.literal("notebot")
                        .then(ClientCommandManager.literal("preview")
                                .then(ClientCommandManager.literal("play")
                                        .then(ClientCommandManager.argument("song", StringArgumentType.greedyString())
                                                .suggests(new SongSuggestionProvider())
                                                .executes(NotebotPreviewStartCommand::run)))));
    }

    private static int run(CommandContext<FabricClientCommandSource> context) {
        NotebotPlayer.stop();
        song = NotebotUtils.parse(
                NotebotFileManager.getDir().resolve(
                        "songs/" + context.getArgument("song", String.class)));
        playing = true;
        mc.player.sendMessage(
                Text.literal("§6Started previewing the song §a" + context.getArgument("song", String.class) + "§6."));
        return 1;
    }

    public static int getNote(BlockPos pos) {
        if (!isNoteblock(pos))
            return -1;

        return mc.world.getBlockState(pos).get(NoteBlock.NOTE);
    }

    @SuppressWarnings("resource")
    public static void playBlock(NoteBlockInstrument NoteBlockInstrument, int pitch) {
        // Normalize, code taken from:
        // https://github.com/LCLPYT/notica/blob/91a6edfba721c54a71a523db7481d85bd9dc6592/src/main/java/work/lclpnet/notica/util/NoteHelper.java#L56
        // var pitch = (short) _pitch;
        // pitch -= 33;
        mc.world.playSoundFromEntity(MinecraftClient.getInstance().player,
                MinecraftClient
                        .getInstance().player,
                NoteBlockInstrumentToMusic(
                        NoteBlockInstrument),
                SoundCategory.RECORDS,
                1f, (float) Math.pow(2, (double) (pitch - 12) / 12), 0);
    }

    public static Reference<SoundEvent> NoteBlockInstrumentToMusic(NoteBlockInstrument NoteBlockInstrument) {

        switch (NoteBlockInstrument) {
            case FLUTE:
                return SoundEvents.BLOCK_NOTE_BLOCK_FLUTE;
            case BELL:
                return SoundEvents.BLOCK_NOTE_BLOCK_BELL;
            case CHIME:
                return SoundEvents.BLOCK_NOTE_BLOCK_CHIME;
            case XYLOPHONE:
                return SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE;
            case IRON_XYLOPHONE:
                return SoundEvents.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE;
            case COW_BELL:
                return SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL;
            case DIDGERIDOO:
                return SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO;
            case BIT:
                return SoundEvents.BLOCK_NOTE_BLOCK_BIT;
            case BANJO:
                return SoundEvents.BLOCK_NOTE_BLOCK_BANJO;
            case PLING:
                return SoundEvents.BLOCK_NOTE_BLOCK_PLING;
            case SNARE:
                return SoundEvents.BLOCK_NOTE_BLOCK_SNARE;
            case BASEDRUM:
                return SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM;
            case HAT:
                return SoundEvents.BLOCK_NOTE_BLOCK_HAT;
            case BASS:
                return SoundEvents.BLOCK_NOTE_BLOCK_BASS;
            case GUITAR:
                return SoundEvents.BLOCK_NOTE_BLOCK_GUITAR;
            default: // HARP also goes here.
                return SoundEvents.BLOCK_NOTE_BLOCK_HARP;
        }
    }

    public static boolean isNoteblock(BlockPos pos) {
        // Checks if this block is a noteblock and the noteblock can be played
        return mc.world.getBlockState(pos).getBlock() instanceof NoteBlock && mc.world.getBlockState(pos.up()).isAir();
    }

    public static void stop() {
        playing = false;
        song = null;
        blockPitches.clear();
        playing = false;
        timer = 0;
    }

    public static void onTick(MinecraftClient client) {
        if (mc.world == null || mc.interactionManager == null || !playing)
            return;

        if (song == null) {
            if (queue.isEmpty()) {
                mc.player.sendMessage(Text.literal("§cThere's no songs to preview!"));
                stop();
                return;
            }
        }

        if (timer == -10) {
            mc.player.sendMessage(Text.literal("§6Now Playing: §a" + song.filename));
        }

        timer++;

        Collection<Note> curNotes = song.notes.get(timer);

        if (timer >= song.length) {
            mc.player.sendMessage(Text.literal("§6The song preview §a" + song.filename + "§6 finished."));
            stop();
            return;
        }
        if (curNotes.isEmpty()) {
            return;
        }

        for (Note i : curNotes) {
            playBlock(NoteBlockInstrument.values()[i.NoteBlockInstrument], i.pitch);
        }
    }
}
