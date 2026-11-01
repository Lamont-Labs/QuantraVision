"""Verify source pixels even when a publisher changes lossless PNG compression."""
import hashlib
import io
import struct
from PIL import Image


def pixel_fingerprint(raw):
    with Image.open(io.BytesIO(raw)) as image:
        if image.width * image.height > 4_000_000:
            raise ValueError('Reference image exceeds the pixel limit')
        rgba = image.convert('RGBA')
        payload = struct.pack('>II', *rgba.size) + rgba.tobytes()
        return rgba.size, hashlib.sha256(payload).hexdigest()


def validate_image(raw, source):
    size, pixels = pixel_fingerprint(raw)
    if size != (source['width'], source['height']):
        raise ValueError('Reference dimensions changed: ' + source['id'])
    if pixels != source['pixel_sha256']:
        raise ValueError('Reference pixels changed: ' + source['id'])
    # The original byte hash remains recorded as acquisition provenance.
    # The dimension-bound pixel hash is the canonical lossless-image identity.
    return hashlib.sha256(raw).hexdigest()
