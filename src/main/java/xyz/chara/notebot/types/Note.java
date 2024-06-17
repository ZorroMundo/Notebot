/*
This file is part of Notebot.
Notebot is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
Notebot is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with Notebot. If not, see <https://www.gnu.org/licenses/>.
*/

package xyz.chara.notebot.types;

public class Note {

    public int pitch;
    public int NoteBlockInstrument;

    public Note(int pitch, int NoteBlockInstrument) {
        this.pitch = pitch;
        this.NoteBlockInstrument = NoteBlockInstrument;
    }

    public int getPitch() {
        return pitch;
    }

    public int getNoteBlockInstrument() {
        return NoteBlockInstrument;
    }

    @Override
    public int hashCode() {
        return pitch * 31 + NoteBlockInstrument;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Note))
            return false;

        Note other = (Note) obj;
        return NoteBlockInstrument == other.NoteBlockInstrument && pitch == other.pitch;
    }

    @Override
    public String toString() {
        return "NoteBlockInstrument:" + Integer.toString(NoteBlockInstrument) + " Pitch:" + Integer.toString(pitch);
    }
}
