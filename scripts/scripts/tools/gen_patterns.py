import os, math
from PIL import Image, ImageDraw

out_dir = "app/src/main/assets/patterns"
os.makedirs(out_dir, exist_ok=True)

def pattern_wave(size=256):
    img = Image.new("RGB", (size, size), "black")
    d = ImageDraw.Draw(img)
    for x in range(size):
        y = int((math.sin(x/10) * 50) + size/2)
        d.line((x, y, x, y+2), fill="cyan")
    return img

for i in range(5):
    pattern_wave().save(os.path.join(out_dir, f"pattern_{i}.png"))
print("✅ Generated 5 base pattern overlays")
