#!/usr/bin/env bash
# Generates Android launcher icons from a source PNG.
#
# Usage:
#   scripts/install-launcher-icon.sh path/to/source.png [--trim] [--no-square]
#
# Flags:
#   --trim        auto-trim any near-white border (use when the source has a
#                 white frame around the actual artwork)
#   --no-square   skip the center-crop-to-square step (use when source is
#                 already square)
#
# Requires: python3 + Pillow (already present on this machine).

set -euo pipefail

SRC=""
TRIM=0
SQUARE=1
for arg in "$@"; do
  case "$arg" in
    --trim) TRIM=1 ;;
    --no-square) SQUARE=0 ;;
    *) SRC="$arg" ;;
  esac
done

if [[ -z "${SRC}" ]]; then
  echo "usage: $0 path/to/source.png [--trim] [--no-square]" >&2
  exit 1
fi
if [[ ! -f "$SRC" ]]; then
  echo "source not found: $SRC" >&2
  exit 1
fi

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
RES="$PROJECT_ROOT/app/src/main/res"

python3 - "$SRC" "$RES" "$TRIM" "$SQUARE" <<'PY'
import os, sys
from PIL import Image, ImageChops

src_path, res_root, trim_flag, square_flag = sys.argv[1], sys.argv[2], sys.argv[3] == "1", sys.argv[4] == "1"

img = Image.open(src_path).convert("RGBA")

if trim_flag:
    # Find the bounding box of pixels that are NOT near-white.
    rgb = img.convert("RGB")
    bg = Image.new("RGB", rgb.size, (255, 255, 255))
    diff = ImageChops.difference(rgb, bg)
    # Tolerance: anything within 12 of pure white is treated as background.
    bbox = diff.point(lambda v: 255 if v > 12 else 0).getbbox()
    if bbox:
        img = img.crop(bbox)
        print(f"trimmed to {img.size}")

if square_flag:
    w, h = img.size
    side = min(w, h)
    left = (w - side) // 2
    top = (h - side) // 2
    img = img.crop((left, top, left + side, top + side))
    print(f"squared to {img.size}")

densities = [("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)]
for d, s in densities:
    out_dir = os.path.join(res_root, f"mipmap-{d}")
    os.makedirs(out_dir, exist_ok=True)
    resized = img.resize((s, s), Image.LANCZOS)
    resized.save(os.path.join(out_dir, "ic_launcher.png"))
    resized.save(os.path.join(out_dir, "ic_launcher_round.png"))
    print(f"wrote mipmap-{d}: {s}x{s}")
PY

echo "done."
