# 📦 如何上传APK到GitHub Release

## 当前状态

✅ **已完成的工作**:
1. APK编译配置已就绪
2. 签名密钥配置已完成
3. 发布文档已创建
4. 自动化脚本已准备

❌ **待完成**:
- 需要重新编译APK并上传到GitHub Release

## 🚀 完整上传流程

### 步骤1: 编译Release APK

```bash
# 设置环境变量
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/home/engine/Android/Sdk

# 编译Release APK（需要约3-5分钟）
cd /home/engine/project
./gradlew assembleRelease --no-daemon
```

编译成功后，APK位置: `app/build/outputs/apk/release/app-release-unsigned.apk`

### 步骤2: 签名APK

```bash
# 如果密钥库不存在，创建新的
keytool -genkeypair -v \
  -keystore android.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias androidkey \
  -storepass android123 \
  -keypass android123 \
  -dname "CN=AutoGLM, OU=Android, O=AutoGLM, L=City, S=State, C=CN"

# 使用apksigner签名APK
$ANDROID_HOME/build-tools/34.0.0/apksigner sign \
  --ks android.jks \
  --ks-key-alias androidkey \
  --ks-pass pass:android123 \
  --key-pass pass:android123 \
  --out app/build/outputs/apk/release/app-release.apk \
  app/build/outputs/apk/release/app-release-unsigned.apk

# 验证签名
$ANDROID_HOME/build-tools/34.0.0/apksigner verify --print-certs \
  app/build/outputs/apk/release/app-release.apk
```

### 步骤3: 上传到GitHub Release

#### 方法A: 使用自动化脚本（推荐）

```bash
# 1. 设置有效的GitHub token (需要repo权限)
export GH_TOKEN="your_github_personal_access_token"

# 2. 运行发布脚本
./create_github_release.sh
```

#### 方法B: 使用GitHub CLI

```bash
# 1. 认证
export GH_TOKEN="your_github_personal_access_token"
echo $GH_TOKEN | gh auth login --with-token

# 2. 创建release并上传APK
gh release create v1.0.5 \
  app/build/outputs/apk/release/app-release.apk \
  --title "AutoGLM Android v1.0.5 - 性能优化版" \
  --notes-file RELEASE_NOTES_v1.0.5.md \
  --repo hgrghu/AndroidAutoGLM
```

#### 方法C: 使用GitHub Web界面

1. 访问: https://github.com/hgrghu/AndroidAutoGLM/releases/new
2. 填写表单:
   - **Tag version**: `v1.0.5`
   - **Release title**: `AutoGLM Android v1.0.5 - 性能优化版`
   - **Description**: 复制 `RELEASE_NOTES_v1.0.5.md` 的全部内容
3. 上传文件:
   - 点击"Attach binaries by dropping them here or selecting them"
   - 选择 `app/build/outputs/apk/release/app-release.apk`
4. 点击 **"Publish release"**

## 📝 获取GitHub Token

如需创建GitHub Personal Access Token:

1. 访问: https://github.com/settings/tokens/new
2. 设置:
   - **Note**: `AndroidAutoGLM Release`
   - **Expiration**: 选择合适的过期时间
   - **Scopes**: 勾选 `repo` (Full control of private repositories)
3. 点击 "Generate token"
4. 复制token并保存（只显示一次）

## 🔍 验证发布

发布后验证:

```bash
# 检查release是否存在
curl -s https://api.github.com/repos/hgrghu/AndroidAutoGLM/releases/tags/v1.0.5 | jq .

# 测试APK下载
wget -O test-download.apk \
  https://github.com/hgrghu/AndroidAutoGLM/releases/download/v1.0.5/app-release.apk

# 验证下载的APK
ls -lh test-download.apk
```

## 📊 预期结果

成功发布后:

- ✅ Release页面可访问: https://github.com/hgrghu/AndroidAutoGLM/releases/tag/v1.0.5
- ✅ APK可下载: https://github.com/hgrghu/AndroidAutoGLM/releases/download/v1.0.5/app-release.apk
- ✅ 发布说明完整显示
- ✅ APK大小约236 MB
- ✅ 签名验证通过

## ⚠️ 常见问题

### Q: Token无效/过期
**A**: 创建新的GitHub Personal Access Token，确保有`repo`权限

### Q: APK太大无法上传
**A**: GitHub Release支持最大2GB文件，236MB APK完全可以上传

### Q: 编译失败
**A**: 确保已安装:
- JDK 17
- Android SDK (API 34)
- 网络连接正常（用于下载依赖和模型）

### Q: 签名失败
**A**: 检查:
- 密钥库文件是否存在
- 密码是否正确
- apksigner路径是否正确

## 🎯 快速命令总结

```bash
# 一键完成编译、签名、上传（需要先设置GH_TOKEN）
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/home/engine/Android/Sdk
export GH_TOKEN="your_token"

cd /home/engine/project
./gradlew assembleRelease --no-daemon && \
$ANDROID_HOME/build-tools/34.0.0/apksigner sign \
  --ks android.jks \
  --ks-key-alias androidkey \
  --ks-pass pass:android123 \
  --key-pass pass:android123 \
  --out app/build/outputs/apk/release/app-release.apk \
  app/build/outputs/apk/release/app-release-unsigned.apk && \
./create_github_release.sh
```

---

**注意**: 由于APK文件较大（236MB），不建议提交到Git仓库。请使用GitHub Release来分发APK。
