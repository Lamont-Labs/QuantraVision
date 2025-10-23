#!/usr/bin/env python3
# QuantraVision — Pattern Image Generator
# Offline-first. Generates missing reference PNGs for YAML pattern templates.
# Default "builtin" renderer uses deterministic Matplotlib drawings.
# Optional providers: "openai" (requires OPENAI_API_KEY) — use only if you accept network use.
#
# Usage examples:
#   python scripts/generate_pattern_images.py --yaml-dir pattern_templates --out-dir app/src/main/assets/pattern_templates
#   python scripts/generate_pattern_images.py --yaml-dir pattern_templates --out-dir app/src/main/assets/pattern_templates --provider openai

import argparse, os, re, json, hashlib, sys
from pathlib import Path

# ---------- helpers ----------

def sha256_bytes(b: bytes) -> str:
    return hashlib.sha256(b).hexdigest()

def read_yaml_name(yaml_text: str) -> str | None:
    m = re.search(r'^\s*name\s*:\s*["\']?(.+?)["\']?\s*$', yaml_text, re.M)
    return m.group(1).strip() if m else None

def set_yaml_image(yaml_text: str, rel_path: str) -> str:
    if re.search(r'^\s*image\s*:', yaml_text, re.M):
        return re.sub(r'(^\s*image\s*:\s*)([^\n#]+)', r'\1"' + rel_path + '"', yaml_text, flags=re.M)
    # append under end
    sep = "" if yaml_text.endswith("\n") else "\n"
    return yaml_text + f'{sep}image: "{rel_path}"\n'

# ---------- providers ----------

def provider_builtin(pattern_name: str, out_path: Path) -> bytes:
    # Deterministic 256x256 PNG with stylized lines using matplotlib
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
    import numpy as np

    W, H = 256, 256
    fig = plt.figure(figsize=(W/96, H/96), dpi=96)
    ax = fig.add_axes([0,0,1,1])
    ax.set_axis_off()
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)

    # background
    ax.plot([0,100],[50,50], lw=0.6, alpha=0.4)

    name = pattern_name.lower()

    def line(x0,y0,x1,y1, lw=2.0): ax.plot([x0,x1],[y0,y1], lw=lw)
    def dots(xs, ys): ax.plot(xs, ys, "o", ms=3)

    if "double top" in name:
        line(5,30,30,70); line(30,70,45,65); line(45,65,60,70); line(60,70,95,35)
        dots([30,60],[70,70])
    elif "double bottom" in name:
        line(5,70,30,30); line(30,30,45,35); line(45,35,60,30); line(60,30,95,65)
        dots([30,60],[30,30])
    elif "head & shoulders" in name or "head and shoulders" in name:
        line(5,35,20,65); line(20,65,35,45); line(35,45,50,80); line(50,80,65,45); line(65,45,95,35)
        ax.plot([10,90],[40,40], lw=1.0, alpha=0.6)
    elif "triangle" in name:
        line(10,30,90,70); line(10,30,10,70); line(10,70,90,70)
    elif "wedge" in name:
        line(10,30,90,60); line(10,40,90,70)
    elif "flag" in name:
        line(10,20,10,80, lw=1.5); line(10,70,45,60); line(45,60,10,50)
    elif "cup" in name and "handle" in name:
        t = np.linspace(-1,1,60); y = 15*(t**2)+30
        ax.plot(20+60*(t+1)/2, y, lw=2.0); line(70,45,90,40)
    elif "rsi" in name:
        # oscillator band
        ax.plot([5,95],[70,70], lw=1); ax.plot([5,95],[30,30], lw=1)
        line(5,50,20,65); line(20,65,40,35); line(40,35,60,60); line(60,60,95,40)
    else:
        # generic trend with consolidation
        line(5,20,60,75); line(60,75,95,60); line(40,55,80,55, lw=1.0)

    fig.canvas.draw()
    from io import BytesIO
    buf = BytesIO()
    fig.savefig(buf, format="png", dpi=96, transparent=False)
    plt.close(fig)
    data = buf.getvalue()
    return data

def provider_openai(pattern_name: str, out_path: Path) -> bytes:
    # Optional network provider; requires OPENAI_API_KEY env and openai>=1.0
    import os
    try:
        from openai import OpenAI
    except Exception as e:
        raise RuntimeError("openai package not installed") from e
    key = os.environ.get("OPENAI_API_KEY")
    if not key: raise RuntimeError("OPENAI_API_KEY env var missing")

    client = OpenAI(api_key=key)
    prompt = f"Clean, high-contrast monochrome stock chart rendering of the technical pattern: {pattern_name}. Minimal grid. No labels. 256x256."
    img = client.images.generate(model="gpt-image-1", prompt=prompt, size="256x256")
    b64 = img.data[0].b64_json
    import base64
    return base64.b64decode(b64)

PROVIDERS = {
    "builtin": provider_builtin,
    "openai": provider_openai,
}

# ---------- main ----------

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--yaml-dir", required=True, help="Directory containing *.yaml pattern templates")
    ap.add_argument("--out-dir",  required=True, help="Directory to write PNGs (e.g., app/src/main/assets/pattern_templates)")
    ap.add_argument("--provider", default="builtin", choices=PROVIDERS.keys(), help="Image generator to use")
    ap.add_argument("--prompts",  default="scripts/pattern_prompts.yaml", help="Optional prompts map (not required for builtin)")
    args = ap.parse_args()

    yaml_dir = Path(args.yaml_dir)
    out_dir  = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    gen = PROVIDERS[args.provider]

    updated = 0
    created = 0

    for yf in sorted(yaml_dir.glob("*.yaml")):
        ytxt = yf.read_text(encoding="utf-8")
        pattern_name = read_yaml_name(ytxt) or yf.stem.replace("_", " ").title()
        png_name = f"{yf.stem}_ref.png"
        png_rel  = png_name  # relative within out_dir for Android assets
        png_path = out_dir / png_name

        if not png_path.exists():
            data = gen(pattern_name, png_path)
            png_path.write_bytes(data)
            created += 1

        # ensure YAML has correct image path
        new_yaml = set_yaml_image(ytxt, png_rel)
        if new_yaml != ytxt:
            yf.write_text(new_yaml, encoding="utf-8")
            updated += 1

    # summary
    print(json.dumps({"created_png": created, "updated_yaml": updated, "output_dir": str(out_dir)}, indent=2))

if __name__ == "__main__":
    sys.exit(main())
