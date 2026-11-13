package dev.heartmeter

/** A 3x5 pixel font covering what the HUD needs. Unknown characters render as blanks. */
object PixelFont {
    private val BLANK = arrayOf("...", "...", "...", "...", "...")

    private val GLYPHS: Map<Char, Array<String>> = mapOf(
        'A' to arrayOf(".X.", "X.X", "XXX", "X.X", "X.X"),
        'B' to arrayOf("XX.", "X.X", "XX.", "X.X", "XX."),
        'C' to arrayOf(".XX", "X..", "X..", "X..", ".XX"),
        'D' to arrayOf("XX.", "X.X", "X.X", "X.X", "XX."),
        'E' to arrayOf("XXX", "X..", "XX.", "X..", "XXX"),
        'F' to arrayOf("XXX", "X..", "XX.", "X..", "X.."),
        'G' to arrayOf(".XX", "X..", "X.X", "X.X", ".XX"),
        'H' to arrayOf("X.X", "X.X", "XXX", "X.X", "X.X"),
        'I' to arrayOf("XXX", ".X.", ".X.", ".X.", "XXX"),
        'J' to arrayOf("..X", "..X", "..X", "X.X", ".X."),
        'K' to arrayOf("X.X", "X.X", "XX.", "X.X", "X.X"),
        'L' to arrayOf("X..", "X..", "X..", "X..", "XXX"),
        'M' to arrayOf("X.X", "XXX", "X.X", "X.X", "X.X"),
        'N' to arrayOf("XX.", "X.X", "X.X", "X.X", "X.X"),
        'O' to arrayOf(".X.", "X.X", "X.X", "X.X", ".X."),
        'P' to arrayOf("XX.", "X.X", "XX.", "X..", "X.."),
        'Q' to arrayOf(".X.", "X.X", "X.X", "XX.", ".XX"),
        'R' to arrayOf("XX.", "X.X", "XX.", "X.X", "X.X"),
        'S' to arrayOf(".XX", "X..", ".X.", "..X", "XX."),
        'T' to arrayOf("XXX", ".X.", ".X.", ".X.", ".X."),
        'U' to arrayOf("X.X", "X.X", "X.X", "X.X", "XXX"),
        'V' to arrayOf("X.X", "X.X", "X.X", "X.X", ".X."),
        'W' to arrayOf("X.X", "X.X", "X.X", "XXX", "X.X"),
        'X' to arrayOf("X.X", "X.X", ".X.", "X.X", "X.X"),
        'Y' to arrayOf("X.X", "X.X", ".X.", ".X.", ".X."),
        'Z' to arrayOf("XXX", "..X", ".X.", "X..", "XXX"),
        '0' to arrayOf("XXX", "X.X", "X.X", "X.X", "XXX"),
        '1' to arrayOf(".X.", "XX.", ".X.", ".X.", "XXX"),
        '2' to arrayOf("XX.", "..X", ".X.", "X..", "XXX"),
        '3' to arrayOf("XX.", "..X", ".X.", "..X", "XX."),
        '4' to arrayOf("X.X", "X.X", "XXX", "..X", "..X"),
        '5' to arrayOf("XXX", "X..", "XX.", "..X", "XX."),
        '6' to arrayOf(".XX", "X..", "XXX", "X.X", "XXX"),
        '7' to arrayOf("XXX", "..X", ".X.", ".X.", ".X."),
        '8' to arrayOf("XXX", "X.X", "XXX", "X.X", "XXX"),
        '9' to arrayOf("XXX", "X.X", "XXX", "..X", "XX."),
        '%' to arrayOf("X.X", "..X", ".X.", "X..", "X.X"),
        ':' to arrayOf("...", ".X.", "...", ".X.", "..."),
        '-' to arrayOf("...", "...", "XXX", "...", "..."),
        '.' to arrayOf("...", "...", "...", "...", ".X."),
        '!' to arrayOf(".X.", ".X.", ".X.", "...", ".X."),
        '?' to arrayOf("XX.", "..X", ".X.", "...", ".X."),
        '/' to arrayOf("..X", "..X", ".X.", "X..", "X.."),
    )

    fun glyph(c: Char): Array<String> = GLYPHS[c] ?: BLANK
}
