from PIL import Image
import sys

img = Image.open(sys.argv[1]).convert('RGBA')
data = img.getdata()
new_data = []

for r, g, b, a in data:
    # Use luminosity as alpha
    lum = int(r * 0.299 + g * 0.587 + b * 0.114)
    # The user wanted a cyan shockwave
    new_data.append((r, g, b, lum))

img.putdata(new_data)
img.save(sys.argv[2], 'PNG')
