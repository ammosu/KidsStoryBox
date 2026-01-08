#!/usr/bin/env python3
"""
測試單個圖片生成並查看返回數據結構
"""

from gradio_client import Client
import json

client = Client("http://localhost:7860")

print("📖 測試生成一張圖片...")

result = client.predict(
    prompt="A cute white rabbit in a colorful meadow, children's book illustration",
    resolution="1024x1024 ( 1:1 )",
    seed=42,
    steps=8,
    shift=3.0,
    random_seed=False,
    gallery_images=[],
    api_name="/generate"
)

print(f"\n✅ 生成完成！")
print(f"\n返回值類型: {type(result)}")
print(f"返回值長度: {len(result) if isinstance(result, (list, tuple)) else 'N/A'}")

if isinstance(result, (list, tuple)):
    for i, item in enumerate(result):
        print(f"\n--- 元素 {i} ---")
        print(f"類型: {type(item)}")
        print(f"內容: {item if not isinstance(item, (list, dict)) else json.dumps(item, indent=2, ensure_ascii=False)[:500]}")

    # 詳細查看第一個元素（gallery_images）
    if len(result) > 0:
        gallery_images = result[0]
        print(f"\n=== Gallery Images 詳細信息 ===")
        print(f"類型: {type(gallery_images)}")
        if isinstance(gallery_images, list) and len(gallery_images) > 0:
            first_image = gallery_images[0]
            print(f"\n第一張圖片:")
            print(json.dumps(first_image, indent=2, ensure_ascii=False))
