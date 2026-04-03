import os
from PIL import Image

output_dir = r"E:\spore\WinterCoreAddon\src\main\resources\assets\wintercore\textures\item"

# Colors
TRANS = (0, 0, 0, 0)
B = (45, 45, 45, 255)     # Dark border
L = (100, 100, 100, 255)  # Light edge
D = (60, 60, 60, 255)     # Base background
M = (80, 80, 80, 255)     # Mid background 

def generate_chip(name, symbol_pixels, color_map):
    img = Image.new("RGBA", (16, 16), TRANS)
    pixels = img.load()
    
    # Base unified tile layout
    base_layout = [
        "                ",
        "  BBBBBBBBBBBB  ",
        " BLLLLLLLLLLLDB ",
        " BLDDDDDDDDDDDB ",
        " BLDMMMMMMMMDDB ",
        " BLDMMMMMMMMDDB ",
        " BLDMMMMMMMMDDB ",
        " BLDMMMMMMMMDDB ",
        " BLDMMMMMMMMDDB ",
        " BLDMMMMMMMMDDB ",
        " BLDMMMMMMMMDDB ",
        " BLDMMMMMMMMDDB ",
        " BLDDDDDDDDDDDB ",
        " BDDDDDDDDDDDDB ",
        "  BBBBBBBBBBBB  ",
        "                "
    ]
    
    for y in range(16):
        for x in range(16):
            char = base_layout[y][x]
            if char == 'B': pixels[x, y] = B
            elif char == 'L': pixels[x, y] = L
            elif char == 'D': pixels[x, y] = D
            elif char == 'M': pixels[x, y] = M

    # Overlay symbol at center 8x8 (x:4-11, y:4-11)
    for sy in range(8):
        for sx in range(8):
            s_char = symbol_pixels[sy][sx]
            if s_char in color_map:
                pixels[4 + sx, 4 + sy] = color_map[s_char]
                
    img.save(os.path.join(output_dir, f"{name}.png"))
    print(f"Generated {name}.png")

# RANGE (Expansion Arrows)
range_symbol = [
    ".  CC  .",
    " . CC . ",
    "  .  .  ",
    "C  ..  C",
    "C  ..  C",
    "  .  .  ",
    " . CC . ",
    ".  CC  ."
]
range_colors = {
    'C': (0, 255, 255, 255),    # Cyan
    '.': (150, 255, 255, 255)   # Light Cyan
}

# DAMAGE (Cross / Blast)
damage_symbol = [
    "C .  . C",
    " C.  .C ",
    ". C  C .",
    "  .CC.  ",
    "  .CC.  ",
    ". C  C .",
    " C.  .C ",
    "C .  . C"
]
damage_colors = {
    'C': (255, 50, 50, 255),    # Red
    '.': (255, 150, 150, 255)   # Pink/Light Red
}

# PROTECTION (Shield)
protect_symbol = [
    "CCCCCCCC",
    "C......C",
    " C....C ",
    " C....C ",
    "  C..C  ",
    "  C..C  ",
    "   CC   ",
    "   CC   "
]
protect_colors = {
    'C': (255, 180, 0, 255),    # Gold
    '.': (255, 255, 100, 255)   # Light Yellow
}

generate_chip("winter_upgrade_range", range_symbol, range_colors)
generate_chip("winter_upgrade_damage", damage_symbol, damage_colors)
generate_chip("winter_upgrade_protection", protect_symbol, protect_colors)
