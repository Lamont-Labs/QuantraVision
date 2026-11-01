"""Offline regressions for the evaluation's immutable source-image identity."""
import io
from PIL import Image
from verified_images import pixel_fingerprint, validate_image


def encoded(image, compression):
    out = io.BytesIO()
    image.save(out, format='PNG', compress_level=compression)
    return out.getvalue()


image = Image.new('RGBA', (32, 24), (24, 81, 172, 255))
original = encoded(image, 0)
source = dict(id='verification-fixture', width=32, height=24,
              pixel_sha256=pixel_fingerprint(original)[1])
validate_image(original, source)
recompressed = encoded(image, 9)
assert original != recompressed
validate_image(recompressed, source)
for changed in [(25, 81, 172, 255), (24, 81, 172, 0)]:
    altered = image.copy()
    altered.putpixel((11, 13), changed)
    try:
        validate_image(encoded(altered, 9), source)
    except ValueError:
        pass
    else:
        raise AssertionError('Changed source pixels were accepted')
try:
    validate_image(encoded(image.resize((33, 24)), 9), source)
except ValueError:
    pass
else:
    raise AssertionError('Changed reference dimensions were accepted')
print('PASS immutable pixels, alpha and dimensions; lossless recompression accepted')
