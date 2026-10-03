from PIL import Image, ImageDraw, ImageChops, ImageFilter
import os

K = 4
U = 512
S = U * K
OUT = "D:/WithSadam/Pose-Camera/design/icons"

NAVY_TOP = (20, 26, 61)
NAVY_MID = (11, 14, 36)
NAVY_BOT = (6, 8, 20)
INDIGO = (99, 102, 241)
VIOLET = (139, 92, 246)
PINK = (236, 72, 153)
CYAN = (6, 182, 212)
GREEN = (16, 185, 129)
DEEP = (9, 12, 32)
WHITE = (255, 255, 255)


def u(v):
    return int(round(v * K))


def multi_lin(size, stops, angle=0):
    big = int(max(size) * 2)
    g = Image.linear_gradient("L").resize((big, big), Image.BILINEAR)
    if angle:
        g = g.rotate(-angle, resample=Image.BICUBIC)
    l = (big - size[0]) // 2
    t = (big - size[1]) // 2
    g = g.crop((l, t, l + size[0], t + size[1]))
    luts = []
    for ch in range(3):
        lut = []
        for i in range(256):
            p = i / 255.0
            for j in range(len(stops) - 1):
                p0, c0 = stops[j]
                p1, c1 = stops[j + 1]
                if p <= p1 or j == len(stops) - 2:
                    f = 0.0 if p1 == p0 else (p - p0) / (p1 - p0)
                    f = max(0.0, min(1.0, f))
                    lut.append(int(round(c0[ch] + (c1[ch] - c0[ch]) * f)))
                    break
        luts.append(lut)
    return Image.merge("RGB", [g.point(luts[c]) for c in range(3)])


def glow(base, cx, cy, r, color, alpha, falloff=1.7):
    d = Image.radial_gradient("L").resize((2 * r, 2 * r), Image.BILINEAR)
    m = ImageChops.invert(d).point(lambda v: int(((v / 255.0) ** falloff) * alpha))
    base.paste(Image.new("RGB", (2 * r, 2 * r), color), (cx - r, cy - r), m)


def layer():
    return Image.new("RGBA", (S, S), (0, 0, 0, 0))


def over(base, lay):
    return Image.alpha_composite(base.convert("RGBA"), lay)


POSE = {
    "head": (0.500, 0.110),
    "neck": (0.500, 0.250),
    "lsh": (0.352, 0.280), "rsh": (0.648, 0.280),
    "lel": (0.218, 0.425), "lwr": (0.300, 0.572),
    "rel": (0.802, 0.172), "rwr": (0.878, 0.022),
    "hip": (0.500, 0.556),
    "lhp": (0.402, 0.546), "rhp": (0.598, 0.546),
    "lkn": (0.372, 0.748), "lan": (0.328, 0.952),
    "rkn": (0.628, 0.748), "ran": (0.704, 0.952),
}
BONES = [
    ("neck", "hip"), ("neck", "lsh"), ("neck", "rsh"),
    ("lsh", "lel"), ("lel", "lwr"), ("rsh", "rel"), ("rel", "rwr"),
    ("hip", "lhp"), ("hip", "rhp"),
    ("lhp", "lkn"), ("lkn", "lan"), ("rhp", "rkn"), ("rkn", "ran"),
]


def draw_figure(lay, box, stroke, color, joint_color=None, joint_r=0, head_r_f=0.097):
    left, top, w, h = box
    d = ImageDraw.Draw(lay)
    P = {k: (left + nx * w, top + ny * h) for k, (nx, ny) in POSE.items()}
    sw = stroke
    if sw > 0:
        for a, b in BONES:
            d.line([P[a], P[b]], fill=color, width=sw)
        for k, p in P.items():
            if k == "head":
                continue
            d.ellipse([p[0] - sw / 2, p[1] - sw / 2, p[0] + sw / 2, p[1] + sw / 2], fill=color)
        hr = head_r_f * h
        hx, hy = P["head"]
        d.line([P["head"], P["neck"]], fill=color, width=sw)
        d.ellipse([hx - hr, hy - hr, hx + hr, hy + hr], fill=color)
    if joint_color and joint_r:
        for k in ("lsh", "rsh", "lel", "rel", "lwr", "rwr", "hip", "lkn", "rkn", "lan", "ran"):
            p = P[k]
            d.ellipse([p[0] - joint_r, p[1] - joint_r, p[0] + joint_r, p[1] + joint_r], fill=joint_color)


def lerp(c0, c1, f):
    return tuple(int(round(c0[i] + (c1[i] - c0[i]) * f)) for i in range(3))


def grad_arc(lay, cx, cy, r, width, start, sweep, stops, steps=180):
    d = ImageDraw.Draw(lay)
    for i in range(steps):
        f = i / (steps - 1.0)
        seg = sweep / steps
        a0 = start + i * seg
        col = stops[-1][1]
        for j in range(len(stops) - 1):
            p0, c0 = stops[j]
            p1, c1 = stops[j + 1]
            if f <= p1 or j == len(stops) - 2:
                t = 0.0 if p1 == p0 else (f - p0) / (p1 - p0)
                col = lerp(c0, c1, max(0.0, min(1.0, t)))
                break
        d.arc([cx - r, cy - r, cx + r, cy + r], a0 - 0.6, a0 + seg + 0.6,
              fill=col + (255,), width=width)


def smooth(base):
    return base.filter(ImageFilter.GaussianBlur(u(5)))


def top_gloss(base, h, alpha):
    g = Image.linear_gradient("L").resize((S, h), Image.BILINEAR)
    m = ImageChops.invert(g).point(lambda v: int(((v / 255.0) ** 1.8) * alpha))
    base.paste(Image.new("RGB", (S, h), WHITE), (0, 0), m)


def variant_a():
    base = multi_lin((S, S), [(0.0, NAVY_TOP), (0.55, NAVY_MID), (1.0, NAVY_BOT)], angle=18)
    glow(base, u(118), u(104), u(236), INDIGO, 108)
    glow(base, u(406), u(428), u(264), VIOLET, 96)
    glow(base, u(428), u(118), u(152), CYAN, 56)
    glow(base, u(120), u(430), u(170), PINK, 46)
    base = smooth(base)
    top_gloss(base, u(190), 16)

    lay = layer()
    d = ImageDraw.Draw(lay)
    cx, cy, r, w = u(256), u(254), u(168), u(30)
    d.arc([cx - r, cy - r, cx + r, cy + r], 0, 360, fill=(255, 255, 255, 30), width=w)
    base = over(base, lay)

    lay = layer()
    grad_arc(lay, cx, cy, r, w, 132, 278, [(0.0, CYAN), (0.42, INDIGO), (0.78, VIOLET), (1.0, PINK)])
    halo = lay.filter(ImageFilter.GaussianBlur(u(9)))
    halo.putalpha(halo.getchannel("A").point(lambda v: int(v * 0.55)))
    base = over(base, halo)
    base = over(base, lay)

    box = (u(193.5), u(156.6), u(114), u(200))
    lay = layer()
    draw_figure(lay, box, u(18), WHITE + (255,))
    base = over(base, lay)
    lay = layer()
    draw_figure(lay, box, 0, None, joint_color=CYAN + (255,), joint_r=u(5.4))
    base = over(base, lay)
    return base.convert("RGBA")


def variant_b():
    base = multi_lin((S, S), [(0.0, INDIGO), (0.5, VIOLET), (1.0, PINK)], angle=135)
    glow(base, u(140), u(96), u(230), (165, 180, 255), 70)
    glow(base, u(430), u(452), u(250), (255, 120, 190), 60)
    base = smooth(base)
    top_gloss(base, u(210), 30)

    cx, cy = u(256), u(248)
    lay = layer()
    d = ImageDraw.Draw(lay)
    ro = u(184)
    d.arc([cx - ro, cy - ro, cx + ro, cy + ro], 0, 360, fill=(255, 255, 255, 110), width=u(13))
    base = over(base, lay)

    sh = layer()
    ds = ImageDraw.Draw(sh)
    rd = u(148)
    ds.ellipse([cx - rd, cy - rd + u(14), cx + rd, cy + rd + u(14)], fill=(20, 8, 40, 120))
    sh = sh.filter(ImageFilter.GaussianBlur(u(16)))
    base = over(base, sh)

    lay = layer()
    d = ImageDraw.Draw(lay)
    d.ellipse([cx - rd, cy - rd, cx + rd, cy + rd], fill=(255, 255, 255, 255))
    base = over(base, lay)

    lay = layer()
    draw_figure(lay, (u(191.3), u(147.7), u(118), u(206)), u(18), DEEP + (255,))
    base = over(base, lay)

    hw = u(170)
    g = Image.radial_gradient("L").resize((hw, hw), Image.BILINEAR)
    m = ImageChops.invert(g).point(lambda v: int(((v / 255.0) ** 2.0) * 110))
    hl = Image.new("RGBA", (hw, hw), (255, 255, 255, 255))
    hl.putalpha(m)
    lay = layer()
    lay.paste(hl, (u(150), u(120)), hl)
    mask = Image.new("L", (S, S), 0)
    ImageDraw.Draw(mask).ellipse([cx - rd, cy - rd, cx + rd, cy + rd], fill=255)
    lay.putalpha(ImageChops.multiply(lay.getchannel("A"), mask))
    base = over(base, lay)
    return base.convert("RGBA")


def variant_c():
    base = multi_lin((S, S), [(0.0, (24, 29, 72)), (0.5, NAVY_MID), (1.0, (4, 6, 16))], angle=150)
    glow(base, u(96), u(126), u(240), VIOLET, 96)
    glow(base, u(420), u(400), u(240), INDIGO, 90)
    glow(base, u(404), u(96), u(140), CYAN, 44)
    base = smooth(base)
    top_gloss(base, u(190), 16)

    lay = layer()
    d = ImageDraw.Draw(lay)
    inset, ln, bw = u(52), u(56), u(13)
    col = (255, 255, 255, 64)
    for sx, sy in ((1, 1), (-1, 1), (1, -1), (-1, -1)):
        x = inset if sx > 0 else S - inset
        y = inset if sy > 0 else S - inset
        d.line([(x, y), (x + sx * ln, y)], fill=col, width=bw)
        d.line([(x, y), (x, y + sy * ln)], fill=col, width=bw)
        d.ellipse([x - bw / 2, y - bw / 2, x + bw / 2, y + bw / 2], fill=col)
    base = over(base, lay)

    box_g = (u(159), u(129), u(140), u(242))
    lay = layer()
    draw_figure(lay, box_g, u(21), CYAN + (255,))
    ghost = lay.filter(ImageFilter.GaussianBlur(u(2)))
    ghost.putalpha(ghost.getchannel("A").point(lambda v: int(v * 0.62)))
    halo = lay.filter(ImageFilter.GaussianBlur(u(12)))
    halo.putalpha(halo.getchannel("A").point(lambda v: int(v * 0.40)))
    base = over(base, halo)
    base = over(base, ghost)

    box_s = (u(199), u(147), u(140), u(242))
    lay = layer()
    draw_figure(lay, box_s, u(22), WHITE + (255,))
    base = over(base, lay)

    return base.convert("RGBA")


names = [
    ("ic_playstore_v1_pose_match", variant_a),
    ("ic_playstore_v2_lens", variant_b),
    ("ic_playstore_v3_overlay", variant_c),
]
icons = []
for name, fn in names:
    img = fn().resize((U, U), Image.LANCZOS)
    p = os.path.join(OUT, name + ".png")
    img.save(p, "PNG")
    icons.append((name, img))
    print(name, img.size, img.mode, os.path.getsize(p))

pad, gap, small = 36, 32, 48
sheet_w = pad * 2 + 3 * 240 + 2 * gap
sheet_h = pad * 2 + 240 + 28 + small
sheet = Image.new("RGB", (sheet_w, sheet_h), (242, 243, 247))
for i, (name, img) in enumerate(icons):
    x = pad + i * (240 + gap)
    big = img.resize((240, 240), Image.LANCZOS)
    mask = Image.new("L", (240, 240), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, 239, 239], radius=54, fill=255)
    sheet.paste(big, (x, pad), mask)
    sm = img.resize((small, small), Image.LANCZOS)
    smask = Image.new("L", (small, small), 0)
    ImageDraw.Draw(smask).rounded_rectangle([0, 0, small - 1, small - 1], radius=11, fill=255)
    sheet.paste(sm, (x + 240 // 2 - small // 2, pad + 240 + 28), smask)
sheet.save(os.path.join(OUT, "_preview.png"), "PNG")
print("preview", sheet.size)
