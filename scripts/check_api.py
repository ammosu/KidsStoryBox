#!/usr/bin/env python3
"""
檢查 Z-Image-Turbo API 的可用端點
"""

from gradio_client import Client

try:
    client = Client("http://localhost:7860")

    print("✅ 成功連接到服務！")
    print("\n可用的 API 端點：")
    print("=" * 50)

    # 查看客戶端的所有端點
    if hasattr(client, 'endpoints'):
        for endpoint in client.endpoints:
            print(f"  - {endpoint}")

    # 或者查看 view_api
    if hasattr(client, 'view_api'):
        print("\nAPI 資訊：")
        print(client.view_api())

    # 嘗試直接查看可用的方法
    print("\n客戶端屬性：")
    for attr in dir(client):
        if not attr.startswith('_'):
            print(f"  - {attr}")

except Exception as e:
    print(f"❌ 錯誤: {e}")
