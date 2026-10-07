import os
import random
import sys

from PIL import Image

random.seed(7)

W, H = 64, 80
img = Image.new('RGBA', (W, H), (0, 0, 0, 0))

SKIN = [(184, 181, 170), (174, 171, 159), (196, 193, 182), (168, 165, 154), (178, 175, 164)]
SKIN_DARK = [(162, 159, 148), (152, 149, 138), (143, 140, 130)]
CLAW = (107, 104, 96)
BLACK = (0, 0, 0)
VOID = (5, 5, 7)
RIB = (156, 152, 140)
RIB_DARK = (126, 123, 112)
TEETH = (216, 208, 176)


def fill(x, y, w, h, palette):
    for j in range(y, y + h):
        for i in range(x, x + w):
            img.putpixel((i, j), random.choice(palette) + (255,))


def rect(x, y, w, h, c):
    for j in range(y, y + h):
        for i in range(x, x + w):
            img.putpixel((i, j), c + (255,))


def box_faces(u, v, dx, dy, dz):
    return {
        'top': (u + dz, v, dx, dz),
        'bottom': (u + dz + dx, v, dx, dz),
        'right': (u, v + dz, dz, dy),
        'front': (u + dz, v + dz, dx, dy),
        'left': (u + dz + dx, v + dz, dz, dy),
        'back': (u + dz + dx + dz, v + dz, dx, dy),
    }


def paint_box(u, v, dx, dy, dz, palette, claws=False):
    faces = box_faces(u, v, dx, dy, dz)
    for name, (x, y, w, h) in faces.items():
        fill(x, y, w, h, palette)
        if claws and name not in ('top', 'bottom'):
            for k in range(h - 3, h):
                img.putpixel((x, y + k), CLAW + (255,))
                img.putpixel((x + w - 1, y + k), CLAW + (255,))
    return faces


head = paint_box(0, 0, 8, 8, 8, SKIN)
fx, fy, _, _ = head['front']
rect(fx + 1, fy + 2, 2, 3, BLACK)
rect(fx + 5, fy + 2, 2, 3, BLACK)
rect(fx + 0, fy + 5, 1, 1, BLACK)
rect(fx + 7, fy + 5, 1, 1, BLACK)
rect(fx + 0, fy + 6, 8, 1, BLACK)
rect(fx + 1, fy + 7, 6, 1, BLACK)
for tx, ty in ((1, 6), (3, 6), (4, 6), (6, 6), (2, 7), (5, 7)):
    rect(fx + tx, fy + ty, 1, 1, TEETH)

body = paint_box(0, 16, 8, 16, 4, SKIN)
bx, by, _, _ = body['front']
rect(bx + 1, by + 2, 6, 10, VOID)
for ry in (4, 7, 10):
    rect(bx + 1, by + ry, 6, 1, RIB)
rect(bx + 3, by + 2, 2, 10, RIB)
rect(bx + 2, by + 4, 1, 1, RIB_DARK)
rect(bx + 5, by + 7, 1, 1, RIB_DARK)
rect(bx + 1, by + 10, 1, 1, RIB_DARK)

paint_box(32, 0, 3, 30, 3, SKIN, claws=True)
paint_box(44, 0, 3, 30, 3, SKIN_DARK, claws=True)
paint_box(0, 40, 3, 28, 3, SKIN, claws=True)
paint_box(12, 40, 3, 28, 3, SKIN_DARK, claws=True)

out = sys.argv[1]
os.makedirs(os.path.dirname(out), exist_ok=True)
img.save(out)
print('saved', out, img.size)
