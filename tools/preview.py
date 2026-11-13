"""Renders the widget HUD with Pillow, mirroring HudRenderer.kt.

Used to generate app/src/main/res/drawable-nodpi/widget_preview.png and to
eyeball design tweaks without an Android device.
  python3 tools/preview.py OUT.png [session_remaining] [weekly_remaining] [scale]
"""
import math
import re
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "app/src/main/java/dev/heartmeter"
W, H = 88, 44
C = dict(BG=0x0B0B14, FRAME=0xC8A040, DANGER=0xE03030, TEXT=0xF0F0E0, DIM=0x8A8A9A,
         HEART=0xE83838, SHINE=0xFFD0D0, HEMPTY=0x3A1820, MAGIC=0x30C048,
         MSHINE=0x90F0A0, MEMPTY=0x10301A)
HEART = [".XX.XX.", "XXXXXXX", "XXXXXXX", ".XXXXX.", "..XXX..", "...X..."]

# Reuse the glyph table straight from the Kotlin source so the two never drift.
FONT = {m[0]: m[1:] for m in re.findall(
    r"'(.)' to arrayOf\(\"(.{3})\", \"(.{3})\", \"(.{3})\", \"(.{3})\", \"(.{3})\"\)",
    (SRC / "PixelFont.kt").read_text())}


def render(session, week, s_reset="2H14M", w_reset="3D4H", footer="UPD 14:05", scale=8):
    g = [[C["BG"]] * W for _ in range(H)]

    def fill(x, y, w, h, c):
        for yy in range(y, y + h):
            for xx in range(x, x + w):
                g[yy][xx] = c

    def text(x, y, s, c):
        for i, ch in enumerate(s):
            gl = FONT.get(ch, ("...",) * 5)
            for r in range(5):
                for col in range(3):
                    if gl[r][col] == "X":
                        g[y + r][x + i * 4 + col] = c

    def rtext(right, y, s, c):
        text(right - (len(s) * 4 - 1), y, s, c)

    danger = session <= 20 or week <= 10
    fc = C["DANGER"] if danger else C["FRAME"]
    fill(2, 1, W - 4, 1, fc); fill(2, H - 2, W - 4, 1, fc)
    fill(1, 2, 1, H - 4, fc); fill(W - 2, 2, 1, H - 4, fc)

    text(4, 4, f"SESSION {int(session)}%", C["TEXT"]); rtext(W - 4, 4, s_reset, C["DIM"])
    for i in range(10):
        f = min(max((session - i * 10) / 10, 0), 1)
        f = math.floor(f * 4) / 4
        cols = 7 if f >= 1 else int(math.floor(f * 7 + 0.5))
        for r, row in enumerate(HEART):
            for col, ch in enumerate(row):
                if ch == "X":
                    c = (C["SHINE"] if (r, col) == (1, 1) else C["HEART"]) if col < cols else C["HEMPTY"]
                    g[11 + r][4 + i * 8 + col] = c

    text(4, 20, f"WEEKLY {int(week)}%", C["TEXT"]); rtext(W - 4, 20, w_reset, C["DIM"])
    fill(4, 27, 80, 6, C["TEXT"]); fill(5, 28, 78, 4, C["MEMPTY"])
    n = round(78 * week / 100)
    if n:
        fill(5, 28, n, 4, C["MAGIC"]); fill(5, 28, n, 1, C["MSHINE"])
    text(4, 35, footer, C["DIM"])

    img = Image.new("RGB", (W, H))
    img.putdata([((c >> 16) & 255, (c >> 8) & 255, c & 255) for row in g for c in row])
    return img.resize((W * scale, H * scale), Image.NEAREST)


if __name__ == "__main__":
    out = sys.argv[1]
    s = float(sys.argv[2]) if len(sys.argv) > 2 else 66
    w = float(sys.argv[3]) if len(sys.argv) > 3 else 61
    sc = int(sys.argv[4]) if len(sys.argv) > 4 else 8
    render(s, w, scale=sc).save(out)
