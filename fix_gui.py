from PIL import Image, ImageDraw

img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Main background
draw.rectangle([0, 0, 175, 165], fill=(40, 40, 45, 255), outline=(20, 20, 25, 255))

# Title background
draw.rectangle([0, 0, 175, 15], fill=(25, 25, 30, 255))

# Player inventory slots
start_x, start_y = 7, 83
for row in range(3):
    for col in range(9):
        x = start_x + col * 18
        y = start_y + row * 18
        draw.rectangle([x, y, x+17, y+17], fill=(20, 20, 25, 255), outline=(60, 60, 70, 255))

# Player hotbar
for col in range(9):
    x = start_x + col * 18
    y = start_y + 58
    draw.rectangle([x, y, x+17, y+17], fill=(20, 20, 25, 255), outline=(60, 60, 70, 255))

# Center slot (cyan)
x, y = 79, 34
draw.rectangle([x, y, x+17, y+17], fill=(20, 20, 25, 255), outline=(0, 255, 255, 255))
draw.rectangle([x-2, y-2, x+19, y+19], outline=(0, 150, 150, 255))

# Energy bar slot (empty)
draw.rectangle([19, 19, 30, 70], fill=(20, 20, 25, 255), outline=(60, 60, 70, 255))

img.save("src/main/resources/assets/wintercore/textures/gui/winter_core_screen.png")
