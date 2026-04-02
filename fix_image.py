from PIL import Image

def remove_white(img_path):
    img = Image.open(img_path).convert("RGBA")
    datas = img.getdata()
    newData = []
    for item in datas:
        # If the pixel is close to white, make it transparent
        if item[0] > 230 and item[1] > 230 and item[2] > 230:
            newData.append((255, 255, 255, 0))
        else:
            newData.append(item)
    img.putdata(newData)
    img.save(img_path, "PNG")

remove_white("src/main/resources/assets/wintercore/textures/item/winter_energy_cell.png")
