# PDF fonts

Place `DejaVuSans.ttf` (or another Unicode TrueType font) in this directory when deploying in an environment without system fonts.

The application first tries `classpath:fonts/DejaVuSans.ttf`, then common Linux system font paths such as `/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf`.
