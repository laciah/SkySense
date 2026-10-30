small.ttf  = Proggy Tiny (MIT licence, see LICENSE_ProggyFonts_MIT.txt) -> small text.
             It is a pixel font, so the menu only ever uses it at whole multiples of its native size
             (small_1 = 16px, small_2 = 32px, ...). Each multiple is rendered by FreeType at exactly that size,
             so it is always crisp.
title.ttf  = DejaVu Sans Bold (free, Verdana-like), trimmed to Latin characters -> titles.
             One definition per pixel size (title_10 ... title_80), so any window size gets a clean render.

To use real Verdana for titles: copy verdanab.ttf from C:\Windows\Fonts on your own PC, rename it title.ttf
and replace this file (keep your repository PRIVATE - Verdana is Microsoft's font and can't be redistributed).
