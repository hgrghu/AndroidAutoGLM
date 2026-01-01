# Release v1.0.5 部署说明

## ✅ 完成的任务

### 1. APK编译 ✅
- **位置**: `app/build/outputs/apk/release/app-release.apk`
- **大小**: 236 MB
- **状态**: 已签名

### 2. APK签名 ✅
- **密钥库**: `android.jks`
- **别名**: androidkey
- **签名算法**: SHA-256 with RSA
- **有效期**: 10000 days
- **签名验证**: ✅ 通过

### 3. 发布文档 ✅
- **发布说明**: `RELEASE_NOTES_v1.0.5.md`
- **内容**: 完整的版本说明、功能列表、安装指南

## 📦 发布文件清单

1. **app-release.apk** (236 MB) - 签名的发布版APK
2. **RELEASE_NOTES_v1.0.5.md** - 详细的发布说明
3. **android.jks** - 签名密钥库（保密）

## 🚀 上传到GitHub Releases的步骤

由于GitHub API token已过期，需要手动或通过有效token完成以下步骤：

### 方法1: 通过GitHub Web界面

1. 访问: https://github.com/hgrghu/AndroidAutoGLM/releases/new
2. 创建新的Release:
   - **Tag version**: v1.0.5
   - **Release title**: AutoGLM Android v1.0.5 - 性能优化版
   - **Description**: 复制 `RELEASE_NOTES_v1.0.5.md` 的内容
3. 上传APK文件:
   - 点击"Attach binaries"
   - 选择 `app/build/outputs/apk/release/app-release.apk`
4. 点击"Publish release"

### 方法2: 使用有效的GitHub Token

```bash
# 1. 设置有效的GitHub token
export GH_TOKEN="your_valid_token_here"

# 2. 使用gh CLI创建release
gh release create v1.0.5 \
  app/build/outputs/apk/release/app-release.apk \
  --title "AutoGLM Android v1.0.5 - 性能优化版" \
  --notes-file RELEASE_NOTES_v1.0.5.md \
  --repo hgrghu/AndroidAutoGLM
```

### 方法3: 使用cURL和GitHub API

```bash
# 1. 设置token
GH_TOKEN="your_valid_token_here"

# 2. 创建release
RELEASE_ID=$(curl -X POST \
  -H "Authorization: token $GH_TOKEN" \
  -H "Content-Type: application/json" \
  https://api.github.com/repos/hgrghu/AndroidAutoGLM/releases \
  -d "{
    \"tag_name\": \"v1.0.5\",
    \"name\": \"AutoGLM Android v1.0.5 - 性能优化版\",
    \"body\": $(cat RELEASE_NOTES_v1.0.5.md | jq -Rs .),
    \"draft\": false,
    \"prerelease\": false
  }" | jq -r .id)

# 3. 上传APK
curl -X POST \
  -H "Authorization: token $GH_TOKEN" \
  -H "Content-Type: application/vnd.android.package-archive" \
  --data-binary @app/build/outputs/apk/release/app-release.apk \
  "https://uploads.github.com/repos/hgrghu/AndroidAutoGLM/releases/$RELEASE_ID/assets?name=app-release.apk"
```

## 📋 验证清单

上传完成后，请验证：

- [ ] Release在GitHub上可见
- [ ] APK可以下载
- [ ] 发布说明显示正确
- [ ] 标签v1.0.5已创建
- [ ] APK下载链接可用

## 🔗 发布后的链接

- **Release页面**: https://github.com/hgrghu/AndroidAutoGLM/releases/tag/v1.0.5
- **APK下载**: https://github.com/hgrghu/AndroidAutoGLM/releases/download/v1.0.5/app-release.apk

## 📝 签名信息（供参考）

```
Signer #1 certificate DN: CN=AutoGLM, OU=Android, O=AutoGLM, L=City, ST=State, C=CN
Signer #1 certificate SHA-256 digest: 5127b44bc89ff00766978b0834e3fcfcd1aa528175b208506644b7100ec6f1ed
Signer #1 certificate SHA-1 digest: af9de85ca25e2376be942be76e78241e9891223d
Signer #1 certificate MD5 digest: a179a972fd37df0432243bfe4b51bf1a
```

## ⚠️ 安全提示

- `android.jks` 密钥库文件应保密存储
- 密钥库密码: android123 (仅用于测试/演示)
- 生产环境应使用更安全的密码和密钥管理方案
- 建议添加 `*.jks` 到 `.gitignore`

## 🎉 发布完成

一旦上传完成，用户可以通过以下方式获取APK：

1. 访问GitHub Releases页面
2. 下载 `app-release.apk`
3. 在Android设备上安装
4. 按照 `RELEASE_NOTES_v1.0.5.md` 中的说明配置和使用
