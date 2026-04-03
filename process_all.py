import sys
from rembg import remove
from PIL import Image

def process(file_path):
    with open(file_path, 'rb') as f:
        img_data = f.read()
    print(f"Removing BG for {file_path}")
    no_bg = remove(img_data)
    
    import io
    img = Image.open(io.BytesIO(no_bg)).convert("RGBA")
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)
        
    max_d = max(img.width, img.height)
    sq = Image.new("RGBA", (max_d, max_d), (0,0,0,0))
    sq.paste(img, ((max_d - img.width)//2, (max_d - img.height)//2))
    
    # Resize to 16x16
    final = sq.resize((16, 16), Image.Resampling.LANCZOS)
    
    # Sharp threshold for alpha
    datas = final.getdata()
    newData = []
    for item in datas:
        if item[3] < 128:
            newData.append((0, 0, 0, 0))
        else:
            newData.append(item)
    final.putdata(newData)
    final.save(file_path)

if __name__ == "__main__":
    process(r"E:\spore\WinterCoreAddon\src\main\resources\assets\wintercore\textures\item\winter_upgrade_range.png")
    process(r"E:\spore\WinterCoreAddon\src\main\resources\assets\wintercore\textures\item\winter_upgrade_damage.png")
    process(r"E:\spore\WinterCoreAddon\src\main\resources\assets\wintercore\textures\item\winter_upgrade_protection.png")
    print("Done")
