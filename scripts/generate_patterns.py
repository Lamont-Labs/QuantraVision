#!/usr/bin/env python3
# QuantraVision: Pattern Art Generator (offline, deterministic)
# Creates Android-ready PNGs for every chart pattern template.
# Default in/out:
#   IN : app/src/main/assets/pattern_templates/*.json
#   OUT: app/src/main/res/drawable[-*]/pattern_<name>.png  (mdpi..xxxhdpi)
#
# Usage:
#   python3 scripts/generate_patterns.py \
#     --templates app/src/main/assets/pattern_templates \
#     --out app/src/main/res \
#     --sizes mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi \
#     --theme neon
#
# Template JSON schema (example: bull_flag.json):
# {
#   "name": "bull_flag",
#   "render": {
#     "series_style": "candles|line",
#     "series_points": 120,
#     "overlay": [
#       {"type":"line","pts":[[10,70],[45,30]],"width":4},
#       {"type":"line","pts":[[10,80],[45,40]],"width":4},
#       {"type":"channel","pts":[[55,30],[90,45]],"width":3}
#     ],
#     "label": "Bull Flag",
#     "confidence": 0.87
#   }
# }
#
# Determinism:
#   Seed = stable hash of pattern name. No RNG outside seeded instance.

import argparse
import json
import math
import os
import pathlib
import random
import sys
from typing import List, Tuple, Dict

try:
    from PIL import Image, ImageDraw, ImageFont, ImageFilter
except ImportError:
    print("[generate_patterns] Pillow not found. Install via: pip install pillow", file=sys.stderr)
    raise

# ---------- Config ----------

DENSITIES = {
    "mdpi":   1.0,
    "hdpi":   1.5,
    "xhdpi":  2.0,
    "xxhdpi": 3.0,
    "xxxhdpi":4.0,
}

BASE_W, BASE_H = 320, 240  # mdpi baseline canvas
THEMES = {
    "neon": {
        "bg1": (6, 18, 28),
        "bg2": (10, 30, 46),
        "grid": (18, 54, 82, 120),
        "bull": (0, 200, 255),
        "bear": (255, 84, 84),
        "accent": (0, 180, 255),
        "text": (180, 220, 255),
        "shadow": (0, 0, 0, 140),
        "candle_up": (0, 220, 170),
        "candle_dn": (230, 72, 72),
    },
    "mono": {
        "bg1": (15, 15, 18),
        "bg2": (25, 25, 28),
        "grid": (120, 120, 140, 110),
        "bull": (210, 210, 210),
        "bear": (180, 180, 180),
        "accent": (230, 230, 230),
        "text": (235, 235, 240),
        "shadow": (0, 0, 0, 150),
        "candle_up": (220, 220, 220),
        "candle_dn": (160, 160, 160),
    }
}

# Fallback fonts (Replit/Ubuntu images usually have DejaVu)
FONT_CANDIDATES = [
    "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
    "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
    "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
]

# ---------- Helpers ----------

def stable_seed(s: str) -> int:
    h = 1469598103934665603  # FNV-1a 64-bit offset
    for b in s.encode("utf-8"):
        h ^= b
        h *= 1099511628211
        h &= 0xFFFFFFFFFFFFFFFF
    return h

def pick_font(size: int) -> ImageFont.FreeTypeFont:
    for p in FONT_CANDIDATES:
        if os.path.exists(p):
            try:
                return ImageFont.truetype(p, size=size)
            except Exception:
                continue
    return ImageFont.load_default()

def ensure_dir(p: str) -> None:
    pathlib.Path(p).mkdir(parents=True, exist_ok=True)

def lerp(a, b, t): return a + (b - a) * t

def draw_background(img: Image.Image, theme: Dict):
    w, h = img.size
    g = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(g)
    # gradient bg
    for y in range(h):
        t = y / max(1, h - 1)
        r = int(lerp(theme["bg1"][0], theme["bg2"][0], t))
        gcol = int(lerp(theme["bg1"][1], theme["bg2"][1], t))
        b = int(lerp(theme["bg1"][2], theme["bg2"][2], t))
        d.line([(0, y), (w, y)], fill=(r, gcol, b, 255))
    # grid
    grid = theme["grid"]
    gx = int(w / 16)
    gy = int(h / 12)
    for x in range(0, w, gx):
        d.line([(x, 0), (x, h)], fill=grid, width=1)
    for y in range(0, h, gy):
        d.line([(0, y), (w, y)], fill=grid, width=1)
    img.alpha_composite(g)

def synth_series(rng: random.Random, n: int, h: int, bull_bias=0.0) -> List[float]:
    """OU-like synthetic series for a subtle background trend."""
    x, mu, theta, sigma = 0.0, 0.0 + bull_bias, 0.1, 0.22
    out = []
    for _ in range(n):
        dx = theta * (mu - x) + sigma * rng.gauss(0, 1)
        x += dx
        out.append(x)
    # normalize to [0.15h, 0.85h]
    mn, mx = min(out), max(out)
    eps = 1e-6 if mx - mn == 0 else (mx - mn)
    norm = [(0.15 + 0.7 * (v - mn) / eps) * h for v in out]
    return norm

def draw_line_series(d: ImageDraw.ImageDraw, series: List[float], w: int, color, width=2):
    n = len(series)
    for i in range(1, n):
        x1 = int((i - 1) * w / (n - 1))
        x2 = int(i * w / (n - 1))
        d.line([(x1, series[i - 1]), (x2, series[i])], fill=color, width=width)

def draw_candles(d: ImageDraw.ImageDraw, rng: random.Random, series: List[float], w: int, theme: Dict):
    n = len(series)
    cw = max(2, int(w / (n * 1.2)))
    last = series[0]
    for i, y in enumerate(series):
        x = int(i * w / (n - 1))
        # candle body
        change = y - last
        last = y
        up = change >= 0
        col = theme["candle_up"] if up else theme["candle_dn"]
        # wick
        wick_top = y - abs(change) * 1.2 - rng.uniform(2, 5)
        wick_bot = y + abs(change) * 1.2 + rng.uniform(2, 5)
        d.line([(x, wick_top), (x, wick_bot)], fill=(*col[:3], 200), width=1)
        # body rect
        body_top = min(y, y - change * 0.8)
        body_bot = max(y, y - change * 0.8)
        d.rectangle([x - cw // 2, body_top, x + cw // 2, body_bot], fill=(*col[:3], 220), outline=None)

def glow_line(base: Image.Image, pts: List[Tuple[int, int]], color: Tuple[int, int, int], width: int):
    glow = Image.new("RGBA", base.size, (0, 0, 0, 0))
    g = ImageDraw.Draw(glow)
    g.line(pts, fill=(*color, 255), width=width)
    for r in (8, 4, 2):
        blur = glow.filter(ImageFilter.GaussianBlur(radius=r))
        base.alpha_composite(Image.new("RGBA", base.size, (0, 0, 0, 0)), (0, 0), blur)
        base.alpha_composite(blur)
    base.alpha_composite(glow)

def draw_overlay(img: Image.Image, overlay: List[dict], theme: Dict):
    d = ImageDraw.Draw(img, "RGBA")
    for item in overlay:
        t = item.get("type", "line")
        width = int(item.get("width", 3))
        color = theme["accent"]
        if t == "line":
            pts = [(int(x), int(y)) for x, y in item["pts"]]
            glow_line(img, pts, color, width)
        elif t == "channel":
            p1, p2 = item["pts"]
            p1, p2 = (int(p1[0]), int(p1[1])), (int(p2[0]), int(p2[1]))
            # draw two parallel lines
            dx, dy = p2[0] - p1[0], p2[1] - p1[1]
            nx, ny = -dy, dx
            norm = math.hypot(nx, ny) or 1.0
            nx, ny = nx / norm, ny / norm
            offset = item.get("offset", 8)
            p1a = (int(p1[0] + nx * offset), int(p1[1] + ny * offset))
            p2a = (int(p2[0] + nx * offset), int(p2[1] + ny * offset))
            p1b = (int(p1[0] - nx * offset), int(p1[1] - ny * offset))
            p2b = (int(p2[0] - nx * offset), int(p2[1] - ny * offset))
            glow_line(img, [p1a, p2a], color, width)
            glow_line(img, [p1b, p2b], color, width)
        elif t == "polygon":
            pts = [(int(x), int(y)) for x, y in item["pts"]]
            d.polygon(pts, outline=(*theme["accent"], 220), fill=(theme["accent"][0], theme["accent"][1], theme["accent"][2], 40))
        # more types can be added here

def badge(img: Image.Image, label: str, confidence: float, theme: Dict):
    d = ImageDraw.Draw(img, "RGBA")
    w, h = img.size
    pad = int(min(w, h) * 0.04)
    r = int(min(w, h) * 0.08)
    rect = [pad, h - pad - r*2, pad + r*5, h - pad]
    # shadow
    d.rounded_rectangle([rect[0]+2, rect[1]+2, rect[2]+2, rect[3]+2], radius=r, fill=theme["shadow"])
    d.rounded_rectangle(rect, radius=r, outline=None, fill=(20, 48, 70, 200))
    font1 = pick_font(int(r*0.8))
    font2 = pick_font(int(r*0.9))
    d.text((rect[0] + r*0.6, rect[1] + r*0.35), label, fill=theme["text"], font=font1)
    conf_txt = f"{int(confidence*100)}% conf."
    d.text((rect[0] + r*0.6, rect[1] + r*1.15), conf_txt, fill=theme["accent"], font=font2)

def render_one(name: str, spec: dict, dpi_scale: float, out_file: str, theme_key: str):
    theme = THEMES.get(theme_key, THEMES["neon"])
    rng = random.Random(stable_seed(name))

    w, h = int(BASE_W * dpi_scale), int(BASE_H * dpi_scale)
    img = Image.new("RGBA", (w, h), (0, 0, 0, 255))
    draw_background(img, theme)

    # series
    render = spec.get("render", {})
    n = int(render.get("series_points", 120))
    style = render.get("series_style", "candles")
    # introduce small bull bias for bullish-named patterns
    bias = 0.15 if ("bull" in name or "ascending" in name or "cup" in name) else (-0.05 if "bear" in name or "descending" in name else 0.0)
    series = synth_series(rng, n, h=int(h*0.76), bull_bias=bias)
    series = [y + h*0.12 for y in series]  # center
    d = ImageDraw.Draw(img, "RGBA")
    if style == "line":
        draw_line_series(d, series, w, color=(*THEMES[theme_key]["accent"], 180), width=max(2, int(2*dpi_scale)))
    else:
        draw_candles(d, rng, series, w, THEMES[theme_key])

    # overlay
    overlay = render.get("overlay", [])
    draw_overlay(img, overlay, THEMES[theme_key])

    # badge
    conf = float(render.get("confidence", 0.85))
    badge(img, render.get("label", name.replace("_", " ").title()), conf, THEMES[theme_key])

    img = img.convert("RGB")  # Android drawables are RGB
    ensure_dir(os.path.dirname(out_file))
    img.save(out_file, format="PNG", optimize=True)
    print(f"[generate_patterns] wrote {out_file}")

def load_templates(dir_path: str) -> Dict[str, dict]:
    mapping = {}
    if not os.path.isdir(dir_path):
        print(f"[generate_patterns] Template dir missing: {dir_path}", file=sys.stderr)
        return mapping
    for p in sorted(pathlib.Path(dir_path).glob("*.json")):
        with open(p, "r", encoding="utf-8") as f:
            try:
                data = json.load(f)
                name = data.get("name") or p.stem
                mapping[name] = data
            except Exception as e:
                print(f"[generate_patterns] bad template {p.name}: {e}", file=sys.stderr)
    return mapping

# Minimal fallbacks if no templates exist
FALLBACKS: Dict[str, dict] = {
    "head_shoulders": {
        "render": {
            "series_style": "line",
            "series_points": 140,
            "overlay": [
                {"type":"line","pts":[[30,130],[70,60]],"width":4},
                {"type":"line","pts":[[70,60],[110,130]],"width":4},
                {"type":"line","pts":[[110,130],[150,80]],"width":4},
                {"type":"line","pts":[[150,80],[190,130]],"width":4},
                {"type":"line","pts":[[30,150],[190,150]],"width":2}
            ],
            "label":"Head & Shoulders",
            "confidence": 0.88
        }
    },
    "bull_flag": {
        "render": {
            "series_style": "candles",
            "series_points": 110,
            "overlay": [
                {"type":"line","pts":[[45,60],[95,35]],"width":4},
                {"type":"line","pts":[[45,70],[95,45]],"width":4},
                {"type":"channel","pts":[[95,35],[160,60]],"width":3,"offset":10}
            ],
            "label":"Bull Flag",
            "confidence": 0.91
        }
    },
    "descending_triangle": {
        "render": {
            "series_style": "candles",
            "series_points": 120,
            "overlay": [
                {"type":"line","pts":[[40,150],[180,150]],"width":3},
                {"type":"line","pts":[[40,70],[180,150]],"width":3}
            ],
            "label":"Descending Triangle",
            "confidence": 0.83
        }
    }
}

def density_path(res_root: str, density: str) -> str:
    sub = "drawable" if density == "mdpi" else f"drawable-{density}"
    return os.path.join(res_root, sub)

def main():
    ap = argparse.ArgumentParser(description="Generate Android drawable PNGs for chart patterns.")
    ap.add_argument("--templates", default="app/src/main/assets/pattern_templates", help="Directory of *.json templates")
    ap.add_argument("--out", default="app/src/main/res", help="Android res root (will create drawable-* folders)")
    ap.add_argument("--sizes", default="mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi", help="Comma list of densities")
    ap.add_argument("--theme", default="neon", choices=list(THEMES.keys()))
    args = ap.parse_args()

    sizes = [s.strip() for s in args.sizes.split(",") if s.strip()]
    for s in sizes:
        if s not in DENSITIES:
            print(f"[generate_patterns] unknown density: {s}", file=sys.stderr)
            sys.exit(2)

    templates = load_templates(args.templates)
    if not templates:
        print("[generate_patterns] no templates found → using built-in fallbacks", file=sys.stderr)
        templates = FALLBACKS

    for name, spec in templates.items():
        for dens in sizes:
            dpi = DENSITIES[dens]
            out_dir = density_path(args.out, dens)
            out_file = os.path.join(out_dir, f"pattern_{name}.png")
            render_one(name, spec, dpi, out_file, args.theme)

if __name__ == "__main__":
    main()
```0
