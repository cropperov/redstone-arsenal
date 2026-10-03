#!/usr/bin/env python3
"""Generates every PNG texture for the mod. Run from the project root:  python3 tools/gen_textures.py
The cube UV layouts here must match MissileModel.java / TankModel.java."""
import math, os, random
from PIL import Image

OUT = "src/main/resources/assets/redstonearsenal/textures"
for sub in ("item", "block", "entity"):
    os.makedirs(f"{OUT}/{sub}", exist_ok=True)


def clamp(v):
    return max(0, min(255, int(v)))


def paint_cube(img, ox, oy, dx, dy, dz, fn, seed=0):
    """Paint the standard Minecraft box UV net starting at (ox, oy)."""
    rnd = random.Random(seed * 1000 + ox * 7 + oy)
    faces = {
        "top": (ox + dz, oy, dx, dz), "bottom": (ox + dz + dx, oy, dx, dz),
        "left": (ox, oy + dz, dz, dy), "front": (ox + dz, oy + dz, dx, dy),
        "right": (ox + dz + dx, oy + dz, dz, dy), "back": (ox + 2 * dz + dx, oy + dz, dx, dy),
    }
    shade = {"top": 1.15, "bottom": 0.65, "left": 0.85, "front": 1.0, "right": 0.8, "back": 0.9}
    for name, (x, y, w, h) in faces.items():
        for j in range(h):
            for i in range(w):
                c = fn(name, i, j, w, h)
                f = shade[name] * (0.86 if (i in (0, w - 1) or j in (0, h - 1)) else 1.0)
                n = rnd.uniform(-6, 6)
                img.putpixel((x + i, y + j), tuple(clamp(ch * f + n) for ch in c) + (255,))


def hash2(a, b, s=0):
    return random.Random(a * 73856093 ^ b * 19349663 ^ s).random()


# ------------------------------------------------------------------ missiles
PALETTES = {
    "explosive":   dict(body=(150, 154, 160), nose=(190, 35, 35), stripe=(200, 40, 40), band=(60, 62, 68), fin=(70, 74, 82)),
    "cluster":     dict(body=(70, 74, 60), nose=(230, 140, 20), stripe=(240, 200, 30), band=(30, 30, 30), fin=(50, 52, 44)),
    "terraformer": dict(body=(225, 228, 220), nose=(60, 170, 70), stripe=(70, 180, 80), band=(120, 90, 50), fin=(55, 130, 60)),
    "shell":       dict(body=(176, 138, 60), nose=(120, 90, 35), stripe=(140, 105, 40), band=(90, 70, 30), fin=(90, 70, 30)),
}


def missile_texture(name, pal):
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))

    def body(face, i, j, w, h):
        if face == "bottom":  # exhaust
            return (25, 25, 28) if (i + j) % 2 else (70, 40, 20)
        if face == "top":
            return pal["nose"]
        if j in (3, 4):
            return pal["stripe"]
        if 16 <= j <= 18:
            return pal["band"]
        if j == 10 and i in (1, 2):
            return (30, 30, 30)  # little hatch
        return pal["body"]

    def nose(face, i, j, w, h):
        k = j / max(1, h - 1)
        base = pal["nose"]
        return tuple(clamp(c * (0.8 + 0.35 * (1 - k))) for c in base)

    def fin(face, i, j, w, h):
        return pal["fin"] if (i + j) % 7 else tuple(clamp(c * 0.7) for c in pal["fin"])

    paint_cube(img, 0, 0, 4, 20, 4, body, 1)
    paint_cube(img, 16, 0, 3, 6, 3, nose, 2)
    paint_cube(img, 32, 0, 12, 5, 1, fin, 3)
    paint_cube(img, 32, 8, 1, 5, 12, fin, 4)
    img.save(f"{OUT}/entity/missile_{name}.png")


for n, p in PALETTES.items():
    missile_texture(n, p)

# ------------------------------------------------------------------ tank
GREENS = [(74, 92, 54), (58, 74, 44), (98, 102, 64), (46, 58, 38)]


def camo(i, j, s):
    return GREENS[int(hash2(i // 3, j // 3, s) * len(GREENS))]


def tank_texture():
    img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))

    def track(face, i, j, w, h):
        if face in ("left", "right"):  # i runs along the track
            for wx in range(4, w, 8):
                if (i - wx) ** 2 + (j - h // 2) ** 2 <= 5:
                    return (95, 95, 100)
            return (30, 30, 34) if i % 3 == 0 else (46, 46, 50)
        if face in ("top", "bottom"):
            return (28, 28, 32) if j % 2 == 0 else (50, 50, 54)
        return (40, 40, 44)

    def hull(face, i, j, w, h):
        c = camo(i, j, 7)
        if face == "top" and j > h - 9 and 3 < i < w - 4:  # engine grille
            return (30, 32, 30) if j % 2 == 0 else (60, 64, 56)
        if (i in (1, w - 2)) and (j in (1, h - 2)):
            return (30, 30, 28)  # rivets
        if face == "front" and h > 3 and j == h // 2:
            return (35, 40, 30)
        return c

    def turret(face, i, j, w, h):
        c = camo(i + 5, j + 2, 11)
        if face in ("left", "right"):
            cx, cy = w // 2, h // 2
            if abs(i - cx) + abs(j - cy) <= 1 or (i == cx and abs(j - cy) <= 2):
                return (228, 228, 228)  # star-ish marking
        if face == "top":
            if (i - w // 2) ** 2 + (j - h // 2 + 2) ** 2 <= 5:
                return (24, 26, 22)
        return c

    def hatch(face, i, j, w, h):
        return (40, 44, 36) if face != "top" else (24, 26, 22) if (i + j) % 2 else (50, 54, 44)

    def barrel(face, i, j, w, h):
        base = (62, 66, 72)
        if face in ("front", "back"):
            return (14, 14, 16) if face == "front" else base
        idx = j if face in ("top", "bottom") else i
        if idx in (2, 3) or idx in (11, 12):
            return (38, 40, 46)
        if idx >= 14:
            return (30, 32, 36)
        return base

    paint_cube(img, 0, 0, 6, 8, 32, track, 1)
    paint_cube(img, 0, 40, 16, 10, 30, hull, 2)
    paint_cube(img, 0, 80, 12, 6, 14, turret, 3)
    paint_cube(img, 0, 100, 5, 2, 5, hatch, 4)
    paint_cube(img, 60, 80, 3, 3, 16, barrel, 5)
    img.save(f"{OUT}/entity/tank.png")


tank_texture()

# ------------------------------------------------------------------ item icons (16x16, procedural + outline)


def outline(mask_img):
    w, h = mask_img.size
    px = mask_img.load()
    out = mask_img.copy()
    opx = out.load()
    for y in range(h):
        for x in range(w):
            if px[x, y][3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < w and 0 <= ny < h and px[nx, ny][3] > 0:
                        opx[x, y] = (18, 18, 24, 255)
                        break
    return out


def missile_icon(name, pal):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    s2 = math.sqrt(2)
    for y in range(16):
        for x in range(16):
            a = ((x - 3) + (12 - y)) / s2   # along the missile, tail -> nose
            p = ((x - 3) - (12 - y)) / s2   # across
            c = None
            if 0 <= a <= 8.5 and abs(p) <= 1.7:
                c = pal["body"]
                if 2.5 <= a <= 3.8:
                    c = pal["stripe"]
                if p > 0.6:
                    c = tuple(clamp(v * 0.75) for v in c)
                elif p < -0.6:
                    c = tuple(clamp(v * 1.12) for v in c)
            elif 8.5 < a <= 12.2 and abs(p) <= 1.7 * (12.4 - a) / 3.9 + 0.35:
                c = pal["nose"]
                if p < -0.3:
                    c = tuple(clamp(v * 1.2) for v in c)
            elif 0 <= a <= 3.2 and 1.7 < abs(p) <= 3.4 - a * 0.3:
                c = pal["fin"]
            elif -3.2 <= a < 0 and abs(p) <= 1.0 + a * 0.15 + 0.4:
                c = (255, 200, 40) if a > -1.6 else (240, 90, 20)
            if c:
                img.putpixel((x, y), tuple(c) + (255,))
    outline(img).save(f"{OUT}/item/missile_{name}.png")


for n in ("explosive", "cluster", "terraformer"):
    missile_icon(n, PALETTES[n])


def rect(img, x0, y0, x1, y1, c):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            img.putpixel((x, y), c + (255,))


def tank_icon():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rect(img, 2, 10, 13, 13, (48, 48, 54))        # tracks
    for x in (4, 7, 10):
        rect(img, x, 11, x, 12, (110, 110, 118))
    rect(img, 3, 8, 12, 9, (74, 92, 54))          # hull
    rect(img, 5, 5, 10, 7, (98, 106, 66))         # turret
    rect(img, 6, 5, 8, 5, (122, 130, 86))
    rect(img, 11, 6, 15, 6, (70, 74, 82))         # barrel
    rect(img, 15, 6, 15, 6, (30, 32, 36))
    rect(img, 7, 4, 8, 4, (40, 44, 36))           # hatch
    outline(img).save(f"{OUT}/item/tank.png")


tank_icon()


def designator_icon():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for t in range(0, 9):  # handle from (2,14) to (9,7)
        x, y = 2 + t * 0.9, 14 - t * 0.9
        for dx, dy in ((0, 0), (1, 0)):
            img.putpixel((int(x) + dx, int(y) + dy), (120, 124, 132, 255))
        img.putpixel((int(x), int(y) + 1), (70, 72, 80, 255))
    cx, cy = 11, 4
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - cx, y - cy)
            if 2.6 <= d <= 3.6:
                img.putpixel((x, y), (235, 40, 40, 255))
            elif d < 1.2:
                img.putpixel((x, y), (255, 230, 120, 255))
    for k in (-4, 4):
        for yy, xx in ((cy, cx + k), (cy + k, cx)):
            if 0 <= xx < 16 and 0 <= yy < 16:
                img.putpixel((xx, yy), (235, 40, 40, 255))
    outline(img).save(f"{OUT}/item/strike_designator.png")


designator_icon()

# ------------------------------------------------------------------ block textures


def cannon_textures():
    rnd = random.Random(42)
    metal = (58, 62, 70)

    def base(x, y):
        n = rnd.uniform(-5, 5)
        c = metal
        edge = x in (0, 15) or y in (0, 15)
        if edge:
            c = (36, 38, 44)
        elif x in (1, 14) or y in (1, 14):
            c = (82, 86, 96)
        if (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)):
            c = (150, 154, 162)  # rivets
        return tuple(clamp(v + n) for v in c)

    for active in (False, True):
        img = Image.new("RGBA", (16, 16))
        for y in range(16):
            for x in range(16):
                c = base(x, y)
                d = math.hypot(x - 7.5, y - 7.5)
                if 3.0 <= d < 4.4:
                    c = (24, 26, 32)
                elif d < 3.0:
                    if active:
                        t = d / 3.0
                        c = (255, clamp(240 - 140 * t), clamp(200 - 160 * t))
                    else:
                        t = d / 3.0
                        c = (clamp(30 + 20 * t), clamp(120 + 60 * t), clamp(170 + 40 * t))
                img.putpixel((x, y), c + (255,))
        img.save(f"{OUT}/block/orbital_cannon_top{'_on' if active else ''}.png")

    side = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            c = base(x, y)
            if y >= 12:  # hazard stripe
                c = (230, 190, 30) if ((x + y) // 2) % 2 else (30, 30, 30)
                if y == 15:
                    c = (24, 24, 28)
            elif 6 <= y <= 9 and 3 <= x <= 12:
                c = (30, 32, 38)
                if y in (7, 8) and 4 <= x <= 11 and x % 2 == 0:
                    c = (60, 190, 230)  # status lights
            side.putpixel((x, y), c + (255,))
    side.save(f"{OUT}/block/orbital_cannon_side.png")

    bottom = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            c = tuple(clamp(v * 0.7) for v in base(x, y))
            bottom.putpixel((x, y), c + (255,))
    bottom.save(f"{OUT}/block/orbital_cannon_bottom.png")


cannon_textures()
print("textures generated")
