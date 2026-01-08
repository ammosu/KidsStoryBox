#!/usr/bin/env python3
"""測試 API 返回格式"""

from gradio_client import Client

client = Client("http://localhost:7860")

print("測試生成圖片...")
result = client.predict(
    prompt="A cute rabbit in a forest, children's book illustration",
    resolution="1024x1024 ( 1:1 )",
    seed=42,
    steps=4,  # 快速測試
    shift=3.0,
    random_seed=False,
    gallery_images=[],
    api_name="/generate"
)

print(f"結果類型: {type(result)}")
print(f"結果內容: {result}")

if isinstance(result, tuple):
    gallery, seed_str, seed_int = result
    print(f"\n gallery: {gallery}")
    print(f" gallery 類型: {type(gallery)}")
    if gallery:
        print(f" gallery[0]: {gallery[0]}")
        print(f" gallery[0] 類型: {type(gallery[0])}")
