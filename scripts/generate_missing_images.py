#!/usr/bin/env python3
"""生成缺少的段落圖片"""

from gradio_client import Client
import os
import time
import shutil

STORIES = [
    {
        "id": "story_001",
        "segments": [
            (3, "小兔子來到河邊，看見了一隻小鴨子。", "NARRATOR"),
            (4, "你好！我可以和你一起玩嗎？", "PROTAGONIST"),
            (6, "當然可以！我正要去游泳，一起來吧！", "ANIMAL"),
            (7, "後來，他們又遇到了松鼠和小鳥，大家都成為了好朋友。", "NARRATOR"),
        ]
    },
    {
        "id": "story_002",
        "segments": [
            (3, "我要去找那朵花！雖然路很遠，但我不怕！", "PROTAGONIST"),
            (4, "小熊開始爬山，路上遇到了很多困難。", "NARRATOR"),
            (6, "不，我不放棄！我一定要找到那朵花！", "PROTAGONIST"),
            (7, "經過努力，小熊終於爬到了山頂，找到了那朵美麗的花。", "NARRATOR"),
        ]
    },
    {
        "id": "story_003",
        "segments": [
            (3, "媽媽，我好不舒服...", "PROTAGONIST"),
            (4, "別擔心寶貝，媽媽會一直陪著你。", "PARENT"),
            (6, "媽媽，妳怎麼不睡覺？", "PROTAGONIST"),
            (7, "因為媽媽要確保你安全舒適，這就是媽媽的愛。", "PARENT"),
        ]
    },
    {
        "id": "story_004",
        "segments": [
            (3, "哇！我的身體變輕了，我飛起來了！", "PROTAGONIST"),
            (4, "小水滴變成了水蒸氣，飛到天空中。", "NARRATOR"),
            (6, "天氣變冷了，我又變重了，我要下雨了！", "PROTAGONIST"),
            (7, "小水滴從天上掉下來，變成雨水，滋潤了大地。", "NARRATOR"),
        ]
    },
    {
        "id": "story_005",
        "segments": [
            (3, "這時，小猴子的朋友們來了。", "NARRATOR"),
            (4, "小猴子，我們好餓啊，可以分我們一些香蕉嗎？", "ANIMAL"),
            (6, "來，大家一起吃吧！", "PROTAGONIST"),
            (7, "朋友們都很開心，大家一起分享美味的香蕉。", "NARRATOR"),
        ]
    }
]

OUTPUT_DIR = "/home/cw/git_project/story/app/src/main/assets/images"

def generate_image(client, prompt, output_filename, seed):
    try:
        result = client.predict(
            prompt=prompt,
            resolution="1024x1024 ( 1:1 )",
            seed=seed,
            steps=8,
            shift=3.0,
            random_seed=False,
            gallery_images=[],
            api_name="/generate"
        )
        gallery_images, used_seed, seed_value = result
        print(f"  {output_filename} 完成 (seed: {used_seed})")
        
        if gallery_images and len(gallery_images) > 0:
            image_item = gallery_images[0]
            if isinstance(image_item, dict) and 'image' in image_item:
                image_path = image_item['image']
            else:
                image_path = str(image_item)
            
            if image_path and os.path.exists(str(image_path)):
                shutil.copy2(str(image_path), os.path.join(OUTPUT_DIR, output_filename))
                return True
        return False
    except Exception as e:
        print(f"  錯誤: {e}")
        return False

def build_prompt(content, character_role):
    role_prefix = ""
    if character_role == "PROTAGONIST":
        role_prefix = "A cute little animal protagonist "
    elif character_role == "ANIMAL":
        role_prefix = "Cute animal characters "
    elif character_role == "PARENT":
        role_prefix = "A loving parent character "
    else:
        role_prefix = ""
    return f"{role_prefix}in a children's book illustration showing: {content} cute cartoon style, colorful and friendly, soft pastel colors, whimsical art style, high quality digital art, no text"

def main():
    print("連接到 Z-Image-Turbo API...")
    client = Client("http://localhost:7860")
    
    success = 0
    for story in STORIES:
        story_id = story["id"]
        print(f"\n{story_id}:")
        for seg_num, content, role in story["segments"]:
            output = f"{story_id}_seg{seg_num}.png"
            if os.path.exists(os.path.join(OUTPUT_DIR, output)):
                print(f"  [跳過] {output}")
                success += 1
                continue
            
            print(f"  生成 {output}...")
            prompt = build_prompt(content, role)
            seed = hash(story_id + str(seg_num)) % 1000000
            if generate_image(client, prompt, output, seed):
                success += 1
            time.sleep(0.5)
    
    print(f"\n完成！共 {success} 張")

if __name__ == "__main__":
    main()
