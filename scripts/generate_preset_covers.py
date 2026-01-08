#!/usr/bin/env python3
"""
為預設故事生成封面圖片
使用本地 Z-Image-Turbo API
"""

import json
import os
import shutil
from pathlib import Path
from gradio_client import Client

# 連接到圖片生成服務
client = Client("http://localhost:7860")

# 預設故事配置
PRESET_STORIES = [
    {
        "id": "story_001",
        "title": "小兔子找朋友",
        "category": "友誼",
        "protagonist": "小白兔",
        "prompt": "A cute white rabbit looking for friends in a colorful meadow, "
                 "children's book illustration, warm and friendly atmosphere, "
                 "happy animals playing together, soft pastel colors, whimsical art style, "
                 "suitable for kids ages 3-6, book cover design, no text"
    },
    {
        "id": "story_002",
        "title": "勇敢的小熊",
        "category": "冒險",
        "protagonist": "小熊",
        "prompt": "A brave little bear on an adventure in a magical forest, "
                 "children's book illustration, exciting journey scene, "
                 "colorful landscape with sparkles, soft pastel colors, whimsical art style, "
                 "suitable for kids ages 3-6, book cover design, no text"
    },
    {
        "id": "story_003",
        "title": "媽媽的愛",
        "category": "家庭",
        "protagonist": "小女孩和媽媽",
        "prompt": "A loving mother hugging her child, warm family scene, "
                 "children's book illustration, cozy home atmosphere, "
                 "heartwarming and tender, soft pastel colors, whimsical art style, "
                 "suitable for kids ages 3-6, book cover design, no text"
    },
    {
        "id": "story_004",
        "title": "小水滴的旅行",
        "category": "科普",
        "protagonist": "小水滴",
        "prompt": "A cute water droplet character traveling through nature, "
                 "children's book illustration, water cycle journey, "
                 "clouds, rain, river, ocean scenes, educational and fun, "
                 "soft pastel colors, whimsical art style, "
                 "suitable for kids ages 3-6, book cover design, no text"
    },
    {
        "id": "story_005",
        "title": "分享的快樂",
        "category": "品德教育",
        "protagonist": "小朋友們",
        "prompt": "Happy children sharing toys and treats together, "
                 "children's book illustration, joyful and caring scene, "
                 "positive message about kindness, soft pastel colors, whimsical art style, "
                 "suitable for kids ages 3-6, book cover design, no text"
    }
]

def generate_cover(story_config):
    """為一個故事生成封面圖"""
    print(f"\n📖 正在生成: {story_config['title']}")
    print(f"   提示詞: {story_config['prompt'][:80]}...")

    try:
        result = client.predict(
            prompt=story_config['prompt'],
            resolution="1024x1024 ( 1:1 )",
            seed=42,
            steps=8,
            shift=3.0,
            random_seed=False,
            gallery_images=[],
            api_name="/generate"
        )

        gallery_images, used_seed, seed_value = result

        if gallery_images and len(gallery_images) > 0:
            # 獲取生成的圖片路徑
            image_data = gallery_images[0]
            if isinstance(image_data, dict):
                image_path = image_data.get('image')
            else:
                image_path = image_data

            print(f"   ✅ 生成成功！種子值: {used_seed}")
            print(f"   📍 圖片路徑: {image_path}")
            return image_path
        else:
            print(f"   ❌ 生成失敗：沒有返回圖片")
            return None

    except Exception as e:
        print(f"   ❌ 錯誤: {e}")
        return None

def copy_to_assets(image_path, story_id, output_dir):
    """複製圖片到 assets 目錄"""
    if not image_path or not os.path.exists(image_path):
        print(f"   ⚠️  圖片路徑無效: {image_path}")
        return None

    try:
        # 創建輸出目錄
        os.makedirs(output_dir, exist_ok=True)

        # 目標檔案名
        target_filename = f"{story_id}_cover.png"
        target_path = os.path.join(output_dir, target_filename)

        # 複製檔案
        shutil.copy2(image_path, target_path)
        print(f"   💾 已保存: {target_path}")

        return target_filename

    except Exception as e:
        print(f"   ❌ 複製失敗: {e}")
        return None

def main():
    print("=" * 70)
    print("🎨 預設故事封面生成器")
    print("=" * 70)

    # 確定輸出目錄
    script_dir = Path(__file__).parent
    project_root = script_dir.parent
    assets_dir = project_root / "app" / "src" / "main" / "assets" / "images"

    print(f"\n📁 輸出目錄: {assets_dir}")

    # 生成所有封面
    results = []
    for i, story in enumerate(PRESET_STORIES, 1):
        print(f"\n[{i}/{len(PRESET_STORIES)}] 處理: {story['title']}")

        # 生成圖片
        image_path = generate_cover(story)

        if image_path:
            # 複製到 assets
            filename = copy_to_assets(image_path, story['id'], str(assets_dir))
            if filename:
                results.append({
                    "story_id": story['id'],
                    "title": story['title'],
                    "filename": filename,
                    "status": "success"
                })
            else:
                results.append({
                    "story_id": story['id'],
                    "title": story['title'],
                    "status": "copy_failed"
                })
        else:
            results.append({
                "story_id": story['id'],
                "title": story['title'],
                "status": "generation_failed"
            })

    # 輸出總結
    print("\n" + "=" * 70)
    print("📊 生成總結")
    print("=" * 70)

    success_count = sum(1 for r in results if r['status'] == 'success')
    print(f"\n✅ 成功: {success_count}/{len(PRESET_STORIES)}")

    for result in results:
        status_icon = "✅" if result['status'] == 'success' else "❌"
        print(f"   {status_icon} {result['story_id']}: {result['title']}")
        if result['status'] == 'success':
            print(f"      → {result['filename']}")

    print(f"\n📁 所有圖片已保存到: {assets_dir}")
    print("\n下一步：")
    print("1. 檢查生成的圖片質量")
    print("2. 如果滿意，運行 Android Studio 的 Gradle Sync")
    print("3. 更新 PresetStoryDataSource 來讀取這些圖片")
    print("=" * 70)

if __name__ == "__main__":
    main()
