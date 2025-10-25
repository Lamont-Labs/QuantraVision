from PIL import Image, ImageDraw, ImageFont, ImageFilter
import os, math

out_dir = "dist/playstore"
os.makedirs(out_dir, exist_ok=True)

W, H = 1080, 1920
font = ImageFont.load_default()

def base_gradient():
    img = Image.new("RGB", (W, H), "#0A0F14")
    d = ImageDraw.Draw(img)
    for y in range(H):
        c = int(10 + (y / H) * 50)
        d.line([(0, y), (W, y)], fill=(c, c+10, c+20))
    return img

def overlay_pattern(img, label, conf, tradeable):
    d = ImageDraw.Draw(img, "RGBA")
    x0, y0 = 200, 700
    x1, y1 = 880, 1100
    d.rectangle([x0, y0, x1, y1], outline=(0, 229, 255, 255), width=6)
    d.rectangle([x0, y0, x1, y1], fill=(0, 229, 255, 30))
    tag = f"{label}  {int(conf*100)}% • {tradeable}"
    tw, th = d.textsize(tag, font=font)
    d.rectangle([x0, y0-50, x0+tw+16, y0-10], fill=(0,0,0,180))
    d.text((x0+8, y0-42), tag, fill="white", font=font)
    return img

def add_header(img, text):
    d = ImageDraw.Draw(img)
    d.text((40,60), text, fill=(0,229,255), font=font)
    d.text((40,100), "Lamont Labs • QuantraVision Overlay", fill=(180,180,180), font=font)
    return img

screens = [
    ("See patterns your platform can't.", "Head & Shoulders", 0.88, "Viable"),
    ("Offline AI chart detection.", "Bull Flag", 0.92, "Caution"),
    ("Your overlay. Your control.", "Triangle Breakout", 0.79, "Not Viable")
]

for i,(title,label,conf,tag) in enumerate(screens,1):
    img = base_gradient()
    img = overlay_pattern(img,label,conf,tag)
    img = add_header(img,title)
    img = img.filter(ImageFilter.SMOOTH_MORE)
    path = os.path.join(out_dir,f"screenshot_{i}.png")
    img.save(path)

print("✅ Generated sample Play Store screenshots:")
for f in os.listdir(out_dir):
    print("  -",f)
