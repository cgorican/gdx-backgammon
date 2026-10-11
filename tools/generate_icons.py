#!/usr/bin/env python3
"""Generates the application icons from vector shapes in the game's colours.

Outputs:
  desktop/icons/logo.png                 256x256, Windows executable icon (Construo)
  desktop/icons/logo.icns                macOS app bundle icon, 16..1024 px (Construo)
  desktop/src/main/resources/icons/icon-{16,32,64,128}.png   window / taskbar icons (LWJGL3)

Requires Pillow (pip install pillow).
"""
import os

from PIL import Image, ImageDraw, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
WOOD = os.path.join(ROOT, 'desktop', 'assets-raw', 'gameplay', 'background.png')

SIZE = 1024
SS = 2  # supersampling

GREEN = (82, 136, 36)
GREEN_RIM = (115, 168, 50)
GREEN_DARK = (55, 98, 22)
CREAM = (221, 211, 167)
CREAM_RIM = (238, 231, 204)
CREAM_DARK = (184, 171, 122)
FRAME = (52, 41, 30)
SPIKE_GREEN = (89, 147, 38, 150)
SPIKE_CREAM = (224, 214, 176, 150)


def checker(size, fill, rim, dark):
    s = size * SS
    img = Image.new('RGBA', (s, s), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse((0, 0, s - 1, s - 1), fill=dark)                       # edge
    inset = int(s * 0.035)
    d.ellipse((inset, inset, s - 1 - inset, s - 1 - inset), fill=rim)  # rim
    inset = int(s * 0.12)
    d.ellipse((inset, inset, s - 1 - inset, s - 1 - inset), fill=fill)
    inset = int(s * 0.30)
    d.ellipse((inset, inset, s - 1 - inset, s - 1 - inset), outline=rim, width=int(s * 0.03))
    return img.resize((size, size), Image.LANCZOS)


def icon():
    s = SIZE * SS
    radius = int(s * 0.19)
    wood = Image.open(WOOD).convert('RGB')
    side = min(wood.size)
    wood = wood.crop((0, 0, side, side)).resize((s, s), Image.LANCZOS).convert('RGBA')

    canvas = Image.new('RGBA', (s, s), (0, 0, 0, 0))
    mask = Image.new('L', (s, s), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, s - 1, s - 1), radius, fill=255)
    canvas.paste(wood, (0, 0), mask)

    # two points (triangles) along the top and bottom, like the board
    points = Image.new('RGBA', (s, s), (0, 0, 0, 0))
    d = ImageDraw.Draw(points)
    w = s // 4
    for i, x in enumerate([int(s * 0.12), int(s * 0.63)]):
        d.polygon([(x, 0), (x + w, 0), (x + w // 2, int(s * 0.46))], fill=SPIKE_GREEN if i == 0 else SPIKE_CREAM)
        d.polygon([(x, s), (x + w, s), (x + w // 2, int(s * 0.54))], fill=SPIKE_CREAM if i == 0 else SPIKE_GREEN)
    canvas = Image.alpha_composite(canvas, Image.composite(points, Image.new('RGBA', (s, s)), mask))

    # frame
    frame = Image.new('RGBA', (s, s), (0, 0, 0, 0))
    ImageDraw.Draw(frame).rounded_rectangle((0, 0, s - 1, s - 1), radius, outline=FRAME + (255,), width=int(s * 0.035))
    canvas = Image.alpha_composite(canvas, frame)

    # checkers with a soft shadow
    c = int(SIZE * 0.46) * SS
    dark = checker(c // SS, GREEN, GREEN_RIM, GREEN_DARK).resize((c, c), Image.LANCZOS)
    bright = checker(c // SS, CREAM, CREAM_RIM, CREAM_DARK).resize((c, c), Image.LANCZOS)
    positions = [(int(s * 0.17), int(s * 0.18)), (int(s * 0.37), int(s * 0.36))]
    for img, (x, y) in zip([dark, bright], positions):
        shadow = Image.new('RGBA', (s, s), (0, 0, 0, 0))
        shadow.paste((0, 0, 0, 110), (x + int(s * 0.015), y + int(s * 0.03)), img.split()[3])
        canvas = Image.alpha_composite(canvas, shadow.filter(ImageFilter.GaussianBlur(s * 0.012)))
        layer = Image.new('RGBA', (s, s), (0, 0, 0, 0))
        layer.paste(img, (x, y), img)
        canvas = Image.alpha_composite(canvas, layer)
    return canvas.resize((SIZE, SIZE), Image.LANCZOS)


def main():
    img = icon()
    icons_dir = os.path.join(ROOT, 'desktop', 'icons')
    window_dir = os.path.join(ROOT, 'desktop', 'src', 'main', 'resources', 'icons')
    os.makedirs(icons_dir, exist_ok=True)
    os.makedirs(window_dir, exist_ok=True)
    img.resize((256, 256), Image.LANCZOS).save(os.path.join(icons_dir, 'logo.png'), optimize=True)
    img.save(os.path.join(icons_dir, 'logo.icns'))
    for size in (16, 32, 64, 128):
        img.resize((size, size), Image.LANCZOS).save(os.path.join(window_dir, 'icon-%d.png' % size), optimize=True)
    print('Generated icons in', icons_dir, 'and', window_dir)


if __name__ == '__main__':
    main()
