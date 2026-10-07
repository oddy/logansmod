import sys
from PIL import Image

# 16x16 spawn egg: pale shell with black speckles and a thin grin.
rows = [
    "................",
    "......####......",
    ".....#PPPP#.....",
    "....#PPPPPP#....",
    "....#PKPPPP#....",
    "...#PPPPPKPP#...",
    "...#PPKPPPPP#...",
    "...#PPPPPPPP#...",
    "..#PKPPPPPKPP#..",
    "..#PPPPPPPPPP#..",
    "..#PKKKKKKKKP#..",
    "..#PPKTKTKTPP#..",
    "...#PPPPPPPP#...",
    "...#SPPPPPPS#...",
    "....##SSSS##....",
    "......####......",
]
pal = {"#": (40, 40, 44, 255), "P": (190, 187, 176, 255), "S": (150, 147, 136, 255),
       "K": (5, 5, 7, 255), "T": (216, 208, 176, 255), ".": (0, 0, 0, 0)}
img = Image.new("RGBA", (16, 16))
for y, row in enumerate(rows):
    for x, ch in enumerate(row):
        img.putpixel((x, y), pal[ch])
img.save(sys.argv[1])
print("saved", sys.argv[1])
