#!/usr/bin/env python3
"""Generates the UI skin's raw images and bitmap fonts.

Outputs:
  desktop/assets-raw/skin/<name>.png / .9.png   anti-aliased UI drawables (packed into assets/ui/uiskin.atlas)
  desktop/assets-raw/skin/poppins-*.png         bitmap font pages (packed into the same atlas)
  assets/ui/font/poppins-*.fnt                  BMFont descriptors (text format) for the pages above

Afterwards run `gradlew desktop:packAssets` to rebuild the atlases.
Requires Pillow and fontTools (pip install pillow fonttools) and the Poppins TTFs (SIL OFL 1.1),
by default from /usr/share/fonts/truetype/google-fonts; override with --font-dir.
"""
import argparse
import os

from PIL import Image, ImageDraw, ImageFilter
from fontTools.ttLib import TTFont
from PIL import ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SKIN_RAW = os.path.join(ROOT, 'desktop', 'assets-raw', 'skin')
FONT_OUT = os.path.join(ROOT, 'assets', 'ui', 'font')

SS = 4  # supersampling factor for smooth edges

GOLD = (233, 180, 76)
GOLD_DARK = (138, 90, 18)
CREAM = (243, 233, 216)

CHARS = (''.join(chr(c) for c in range(32, 127))
         + 'ÀÁÂÄÅÆÇÈÉÊËÌÍÎÏÑÒÓÔÕÖØÙÚÛÜÝßàáâãäåæçèéêëìíîïñòóôõöøùúûüýÿ'
         + 'ĆćČčĐđŠšŽžŁłŃńŚśŹźŻżĄąĘęŘřŤťŮůĚěŇň'
         + '•·–—…×°')

FONTS = [
    # name, file, pixel size
    ('poppins-ui', 'Poppins-Medium.ttf', 40),
    ('poppins-bold', 'Poppins-Bold.ttf', 42),
    ('poppins-title', 'Poppins-Bold.ttf', 76),
    ('poppins-small', 'Poppins-Medium.ttf', 30),
]


# --------------------------------------------------------------------------------------------- drawing helpers

def rounded(size, radius, fill, border=None, border_width=0, gradient=None):
    """RGBA image of a rounded rectangle, drawn supersampled and downscaled."""
    w, h = size
    img = Image.new('RGBA', (w * SS, h * SS), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    box = (0, 0, w * SS - 1, h * SS - 1)
    if border:
        d.rounded_rectangle(box, radius * SS, fill=border)
        inset = border_width * SS
        box = (inset, inset, w * SS - 1 - inset, h * SS - 1 - inset)
        inner_radius = max(0, (radius - border_width) * SS)
    else:
        inner_radius = radius * SS
    if gradient:
        top, bottom = gradient
        grad = Image.new('RGBA', img.size)
        gd = ImageDraw.Draw(grad)
        for y in range(img.size[1]):
            t = y / max(1, img.size[1] - 1)
            gd.line([(0, y), (img.size[0], y)], fill=tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(4)))
        mask = Image.new('L', img.size, 0)
        ImageDraw.Draw(mask).rounded_rectangle(box, inner_radius, fill=255)
        img.paste(grad, (0, 0), mask)
    else:
        d.rounded_rectangle(box, inner_radius, fill=fill)
    return img.resize((w, h), Image.LANCZOS)


def ninepatch(img, left, right, top, bottom, pad=None):
    """Adds the 1px .9.png guide border. left/right/top/bottom are the fixed (non-stretched) margins."""
    w, h = img.size
    out = Image.new('RGBA', (w + 2, h + 2), (0, 0, 0, 0))
    out.paste(img, (1, 1))
    px = out.load()
    black = (0, 0, 0, 255)
    for x in range(1 + left, 1 + w - right):
        px[x, 0] = black
    for y in range(1 + top, 1 + h - bottom):
        px[0, y] = black
    pl, pr, pt, pb = pad if pad else (left, right, top, bottom)
    for x in range(1 + pl, 1 + w - pr):
        px[x, h + 1] = black
    for y in range(1 + pt, 1 + h - pb):
        px[w + 1, y] = black
    return out


def save(img, name):
    img.save(os.path.join(SKIN_RAW, name))


def ring(size, width, color=(255, 255, 255, 255)):
    img = Image.new('RGBA', (size * SS, size * SS), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse((0, 0, size * SS - 1, size * SS - 1), fill=color)
    w = width * SS
    d.ellipse((w, w, size * SS - 1 - w, size * SS - 1 - w), fill=(0, 0, 0, 0))
    return img.resize((size, size), Image.LANCZOS)


def soft_glow(size, blur):
    """White radial glow, alpha fading outwards."""
    img = Image.new('L', (size, size), 0)
    inset = blur * 2
    ImageDraw.Draw(img).ellipse((inset, inset, size - 1 - inset, size - 1 - inset), fill=255)
    img = img.filter(ImageFilter.GaussianBlur(blur))
    out = Image.new('RGBA', (size, size), (255, 255, 255, 0))
    out.putalpha(img)
    return out


def point_glow(width, height):
    """Soft white triangle matching spike-bright.png (base at the bottom, tip at the top); tinted at runtime."""
    img = Image.new('L', (width * SS, height * SS), 0)
    d = ImageDraw.Draw(img)
    d.polygon([(4 * SS, height * SS), (width * SS - 4 * SS, height * SS), (width * SS / 2, 8 * SS)], fill=255)
    img = img.resize((width, height), Image.LANCZOS).filter(ImageFilter.GaussianBlur(3))
    # fade from the base (bright) to the tip (softer)
    fade = Image.linear_gradient('L').resize((width, height)).point(lambda v: 255 - int((255 - v) * 0.55))
    alpha = Image.composite(img, Image.new('L', img.size, 0), fade)
    out = Image.new('RGBA', (width, height), (255, 255, 255, 0))
    out.putalpha(alpha)
    return out


def check_box(size, checked):
    img = rounded((size, size), 10, None, border=(243, 233, 216, 255), border_width=3,
                  gradient=((30, 24, 18, 230), (30, 24, 18, 230)))
    if checked:
        inner = rounded((size - 14, size - 14), 6, None, gradient=(GOLD + (255,), (214, 154, 45, 255)))
        img.alpha_composite(inner, (7, 7))
        big = Image.new('RGBA', (size * SS, size * SS), (0, 0, 0, 0))
        d = ImageDraw.Draw(big)
        s = size * SS
        d.line([(s * 0.27, s * 0.52), (s * 0.44, s * 0.69), (s * 0.74, s * 0.33)], fill=(43, 29, 14, 255),
               width=int(4.5 * SS), joint='curve')
        img.alpha_composite(big.resize((size, size), Image.LANCZOS))
    return img


# --------------------------------------------------------------------------------------------- bitmap fonts

def build_font(name, ttf_path, size):
    font = ImageFont.truetype(ttf_path, size)
    cmap = TTFont(ttf_path).getBestCmap()
    ascent, descent = font.getmetrics()
    pad = 2
    glyphs = []
    for ch in CHARS:
        if ord(ch) not in cmap:
            continue
        x0, y0, x1, y1 = font.getbbox(ch, anchor='ls')
        advance = round(font.getlength(ch))
        w, h = max(0, x1 - x0), max(0, y1 - y0)
        glyphs.append((ch, x0, y0, w, h, advance))

    # simple shelf packing
    page_w = 512 if size < 50 else 1024
    x = y = 0
    row_h = 0
    placed = []
    for g in sorted(glyphs, key=lambda g: -g[4]):
        ch, x0, y0, w, h, adv = g
        if x + w + pad * 2 > page_w:
            x = 0
            y += row_h
            row_h = 0
        placed.append((g, x + pad, y + pad))
        x += w + pad * 2
        row_h = max(row_h, h + pad * 2)
    page_h = 1
    while page_h < y + row_h:
        page_h *= 2

    page = Image.new('RGBA', (page_w, page_h), (255, 255, 255, 0))
    draw = ImageDraw.Draw(page)
    lines = []
    for (ch, x0, y0, w, h, adv), px, py in placed:
        if w and h:
            draw.text((px - x0, py - y0), ch, font=font, anchor='ls', fill=(255, 255, 255, 255))
        lines.append('char id=%d x=%d y=%d width=%d height=%d xoffset=%d yoffset=%d xadvance=%d page=0 chnl=15'
                     % (ord(ch), px, py, w, h, x0, ascent + y0, adv))
    # Re-render alpha-only (white glyphs) to avoid fringes
    alpha = page.split()[3]
    page = Image.new('RGBA', page.size, (255, 255, 255, 0))
    page.putalpha(alpha)
    page.save(os.path.join(SKIN_RAW, name + '.png'))

    header = [
        'info face="%s" size=%d bold=0 italic=0 charset="" unicode=1 stretchH=100 smooth=1 aa=1 padding=0,0,0,0 spacing=1,1'
        % (os.path.splitext(os.path.basename(ttf_path))[0], size),
        'common lineHeight=%d base=%d scaleW=%d scaleH=%d pages=1 packed=0' % (ascent + descent, ascent, page_w, page_h),
        'page id=0 file="%s.png"' % name,
        'chars count=%d' % len(lines),
    ]
    with open(os.path.join(FONT_OUT, name + '.fnt'), 'w', newline='\n') as f:
        f.write('\n'.join(header + lines) + '\n')


# --------------------------------------------------------------------------------------------- main

def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument('--font-dir', default='/usr/share/fonts/truetype/google-fonts')
    args = parser.parse_args()
    os.makedirs(SKIN_RAW, exist_ok=True)
    os.makedirs(FONT_OUT, exist_ok=True)

    # Buttons: 9-patch, 30px corners
    def button(name, top, bottom, border, text_pad=(28, 28, 14, 16)):
        img = rounded((96, 72), 22, None, border=border, border_width=3, gradient=(top, bottom))
        save(ninepatch(img, 30, 30, 30, 30, text_pad), name + '.9.png')

    button('btn-primary', (244, 198, 99, 255), (214, 150, 40, 255), GOLD_DARK + (255,))
    button('btn-primary-over', (250, 214, 130, 255), (228, 168, 58, 255), GOLD_DARK + (255,))
    button('btn-primary-down', (200, 140, 36, 255), (226, 170, 70, 255), GOLD_DARK + (255,))
    button('btn-primary-disabled', (150, 140, 125, 255), (120, 112, 100, 255), (80, 74, 66, 255))
    button('btn-secondary', (52, 42, 32, 235), (34, 27, 20, 235), (243, 233, 216, 140))
    button('btn-secondary-over', (72, 59, 45, 240), (48, 39, 29, 240), (243, 233, 216, 200))
    button('btn-secondary-down', (28, 22, 16, 245), (44, 36, 27, 245), (233, 180, 76, 255))

    # Panels
    panel = rounded((96, 96), 26, None, border=(255, 255, 255, 34), border_width=2,
                    gradient=((28, 22, 16, 214), (18, 14, 10, 224)))
    save(ninepatch(panel, 32, 32, 32, 32, (28, 28, 24, 24)), 'panel.9.png')
    panel_active = rounded((96, 96), 26, None, border=GOLD + (255,), border_width=5,
                           gradient=((40, 31, 21, 236), (24, 18, 12, 240)))
    save(ninepatch(panel_active, 32, 32, 32, 32, (28, 28, 24, 24)), 'panel-active.9.png')
    pill = rounded((48, 48), 24, (255, 255, 255, 255))
    save(ninepatch(pill, 23, 23, 23, 23, (18, 18, 6, 6)), 'pill.9.png')
    field = rounded((64, 64), 14, None, border=(243, 233, 216, 110), border_width=2,
                    gradient=((12, 9, 6, 230), (20, 16, 12, 230)))
    save(ninepatch(field, 18, 18, 18, 18, (18, 18, 10, 10)), 'field.9.png')
    field_focus = rounded((64, 64), 14, None, border=GOLD + (255,), border_width=3,
                          gradient=((12, 9, 6, 240), (20, 16, 12, 240)))
    save(ninepatch(field_focus, 18, 18, 18, 18, (18, 18, 10, 10)), 'field-focus.9.png')
    soft_rect = Image.new('L', (96, 96), 0)
    ImageDraw.Draw(soft_rect).rounded_rectangle((10, 10, 85, 85), 18, fill=255)
    soft_rect = soft_rect.filter(ImageFilter.GaussianBlur(5))
    soft = Image.new('RGBA', (96, 96), (255, 255, 255, 0))
    soft.putalpha(soft_rect)
    save(ninepatch(soft, 40, 40, 40, 40), 'glow-rect.9.png')

    # Checkboxes
    save(check_box(46, False), 'check-off-lg.png')
    save(check_box(46, True), 'check-on-lg.png')

    # Board overlays (white, tinted in code)
    save(ring(124, 7), 'ring.png')
    save(soft_glow(180, 14), 'glow.png')
    save(point_glow(100, 500), 'point-glow.png')

    fonts = os.path.join(args.font_dir)
    for name, ttf, size in FONTS:
        build_font(name, os.path.join(fonts, ttf), size)
    print('Generated UI assets in', SKIN_RAW, 'and', FONT_OUT)


if __name__ == '__main__':
    main()
