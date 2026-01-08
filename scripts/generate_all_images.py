#!/usr/bin/env python3
"""
生成所有預設故事的段落圖片
"""

from gradio_client import Client
import os
import time
import shutil

STORIES = [
    {
        "id": "story_001",
        "title": "小兔子找朋友",
        "segments": [
            ("從前，有一隻可愛的小兔子，住在森林裡。", "NARRATOR"),
            ("小兔子很孤單，它想要找一些朋友。", "NARRATOR"),
            ("我要去找朋友！我想和大家一起玩！", "PROTAGONIST"),
            ("小兔子來到河邊，看見了一隻小鴨子。", "NARRATOR"),
            ("你好！我可以和你一起玩嗎？", "PROTAGONIST"),
            ("當然可以！我正要去游泳，一起來吧！", "ANIMAL"),
            ("後來，他們又遇到了松鼠和小鳥，大家都成為了好朋友。", "NARRATOR"),
            ("從那天起，小兔子每天都和朋友們快樂地玩耍。", "NARRATOR"),
        ]
    },
    {
        "id": "story_002",
        "title": "勇敢的小熊",
        "segments": [
            ("小熊住在一個美麗的山谷裡。", "NARRATOR"),
            ("有一天，小熊聽說山頂上有一朵神奇的花。", "NARRATOR"),
            ("我要去找那朵花！雖然路很遠，但我不怕！", "PROTAGONIST"),
            ("小熊開始爬山，路上遇到了很多困難。", "NARRATOR"),
            ("小熊，山路太難走了，你還是回家吧！", "ANIMAL"),
            ("不，我不放棄！我一定要找到那朵花！", "PROTAGONIST"),
            ("經過努力，小熊終於爬到了山頂，找到了那朵美麗的花。", "NARRATOR"),
            ("小熊學到了：只要勇敢堅持，就能實現夢想。", "NARRATOR"),
        ]
    },
    {
        "id": "story_003",
        "title": "媽媽的愛",
        "segments": [
            ("小貓咪和媽媽一起住在溫暖的家裡。", "NARRATOR"),
            ("有一天，小貓咪生病了，發燒不舒服。", "NARRATOR"),
            ("媽媽，我好不舒服...", "PROTAGONIST"),
            ("別擔心寶貝，媽媽會一直陪著你。", "PARENT"),
            ("媽媽整夜照顧小貓咪，給它喝水，量體溫。", "NARRATOR"),
            ("媽媽，妳怎麼不睡覺？", "PROTAGONIST"),
            ("因為媽媽要確保你安全舒適，這就是媽媽的愛。", "PARENT"),
            ("第二天早上，小貓咪好多了，它給了媽媽一個大大的擁抱。", "NARRATOR"),
        ]
    },
    {
        "id": "story_004",
        "title": "小水滴的旅行",
        "segments": [
            ("我是一滴小水滴，住在大海裡。", "PROTAGONIST"),
            ("有一天，太陽出來了，把小水滴曬得暖暖的。", "NARRATOR"),
            ("哇！我的身體變輕了，我飛起來了！", "PROTAGONIST"),
            ("小水滴變成了水蒸氣，飛到天空中。", "NARRATOR"),
            ("在天空中，小水滴和其他水滴們聚在一起，變成了雲。", "NARRATOR"),
            ("天氣變冷了，我又變重了，我要下雨了！", "PROTAGONIST"),
            ("小水滴從天上掉下來，變成雨水，滋潤了大地。", "NARRATOR"),
            ("最後，小水滴順著小溪，又回到了大海，準備下一次旅行。", "NARRATOR"),
        ]
    },
    {
        "id": "story_005",
        "title": "分享的快樂",
        "segments": [
            ("小猴子有一籃子香蕉，都是它最愛吃的。", "NARRATOR"),
            ("這些都是我的香蕉！我要自己吃光光！", "PROTAGONIST"),
            ("這時，小猴子的朋友們來了。", "NARRATOR"),
            ("小猴子，我們好餓啊，可以分我們一些香蕉嗎？", "ANIMAL"),
            ("小猴子想了想，決定把香蕉分給朋友們。", "NARRATOR"),
            ("來，大家一起吃吧！", "PROTAGONIST"),
            ("朋友們都很開心，大家一起分享美味的香蕉。", "NARRATOR"),
            ("小猴子發現，和朋友一起分享，比自己吃更快樂。", "NARRATOR"),
        ]
    }
]

OUTPUT_DIR = "/home/cw/git_project/story/app/src/main/assets/images"

def generate_image(client, prompt, output_filename, seed):
    """生成單張圖片"""
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
        print(f"  生成完成: {output_filename}, seed: {used_seed}")

        if gallery_images and len(gallery_images) > 0:
            image_item = gallery_images[0]
            if isinstance(image_item, dict) and 'image' in image_item:
                image_path = image_item['image']
            else:
                image_path = str(image_item)
            
            print(f"  圖片路徑: {image_path}")
            
            if image_path and os.path.exists(str(image_path)):
                dest_path = os.path.join(OUTPUT_DIR, output_filename)
                shutil.copy2(str(image_path), dest_path)
                print(f"  保存到: {dest_path}")
                return True

        return False
    except Exception as e:
        print(f"  錯誤: {e}")
        import traceback
        traceback.print_exc()
        return False

def build_prompt(content, character_role):
    """根據段落內容和角色构建 Prompt"""
    role_prefix = ""
    if character_role == "PROTAGONIST":
        role_prefix = "A cute little animal protagonist "
    elif character_role == "ANIMAL":
        role_prefix = "Cute animal characters "
    elif character_role == "PARENT":
        role_prefix = "A loving parent character "
    else:
        role_prefix = ""

    return f"{role_prefix}in a children's book illustration showing: {content} " \
           f"cute cartoon style, colorful and friendly, " \
           f"soft pastel colors, whimsical art style, " \
           f"high quality digital art, no text"

def main():
    print("連接到 Z-Image-Turbo API...")
    client = Client("http://localhost:7860")

    total_images = 0
    success_images = 0

    for story in STORIES:
        story_id = story["id"]
        print(f"\n{'='*50}")
        print(f"故事: {story['title']} ({story_id})")
        print(f"{'='*50}")

        for i, (content, role) in enumerate(story["segments"]):
            segment_num = i + 1
            output_filename = f"{story_id}_seg{segment_num}.png"

            output_path = os.path.join(OUTPUT_DIR, output_filename)
            if os.path.exists(output_path):
                file_size = os.path.getsize(output_path)
                if file_size > 100000:
                    print(f"  [跳過] {output_filename} 已存在")
                    success_images += 1
                    total_images += 1
                    continue

            print(f"\n生成段落 {segment_num}/8:")
            print(f"  內容: {content[:30]}...")

            prompt = build_prompt(content, role)
            # 使用 story_id 和 segment_num 生成穩定的 seed
            seed = hash(story_id + str(segment_num)) % 1000000
            success = generate_image(client, prompt, output_filename, seed)

            total_images += 1
            if success:
                success_images += 1

            time.sleep(0.5)

    print(f"\n{'='*50}")
    print(f"完成！共 {total_images} 張圖片，成功 {success_images} 張")
    print(f"{'='*50}")

if __name__ == "__main__":
    main()
