#!/bin/bash

# GitHub Release Creation Script for AutoGLM Android v1.0.5
# This script automates the creation of a GitHub release and uploads the APK

set -e

# Configuration
REPO_OWNER="hgrghu"
REPO_NAME="AndroidAutoGLM"
TAG_NAME="v1.0.5"
RELEASE_NAME="AutoGLM Android v1.0.5 - 性能优化版"
APK_PATH="app/build/outputs/apk/release/app-release.apk"
RELEASE_NOTES="RELEASE_NOTES_v1.0.5.md"

# Colors for output
RED='\033[0:31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}GitHub Release Creation Script${NC}"
echo -e "${GREEN}================================${NC}"
echo ""

# Check if GitHub token is set
if [ -z "$GH_TOKEN" ] && [ -z "$GITHUB_TOKEN" ]; then
    echo -e "${RED}Error: GitHub token not found!${NC}"
    echo ""
    echo "Please set one of the following environment variables:"
    echo "  export GH_TOKEN='your_github_token'"
    echo "  export GITHUB_TOKEN='your_github_token'"
    echo ""
    echo "You can create a token at: https://github.com/settings/tokens"
    echo "Required scopes: repo"
    exit 1
fi

TOKEN="${GH_TOKEN:-$GITHUB_TOKEN}"

# Check if APK exists
if [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}Error: APK not found at $APK_PATH${NC}"
    echo "Please run: ./gradlew assembleRelease"
    exit 1
fi

# Check if release notes exist
if [ ! -f "$RELEASE_NOTES" ]; then
    echo -e "${RED}Error: Release notes not found at $RELEASE_NOTES${NC}"
    exit 1
fi

echo -e "${YELLOW}Checking existing releases...${NC}"
EXISTING_RELEASE=$(curl -s -H "Authorization: Bearer $TOKEN" \
    "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/tags/$TAG_NAME" \
    | jq -r '.id // empty')

if [ -n "$EXISTING_RELEASE" ]; then
    echo -e "${YELLOW}Release $TAG_NAME already exists (ID: $EXISTING_RELEASE)${NC}"
    echo "Do you want to delete it and create a new one? (y/N)"
    # Non-interactive mode for automation
    if [[ "${NON_INTERACTIVE:-}" == "true" ]]; then
        response="y"
    else
        read -r response
    fi
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        echo -e "${YELLOW}Deleting existing release...${NC}"
        curl -X DELETE -H "Authorization: Bearer $TOKEN" \
            "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/$EXISTING_RELEASE"
        echo -e "${GREEN}Existing release deleted${NC}"
    else
        echo "Aborting"
        exit 1
    fi
fi

echo -e "${YELLOW}Creating git tag...${NC}"
git tag -f "$TAG_NAME"
# Attempt to push tag, but don't fail if it doesn't work (might not have push access but have release access)
git push -f origin "$TAG_NAME" || echo "Warning: Could not push tag to origin"

echo -e "${YELLOW}Creating GitHub release...${NC}"
RELEASE_BODY=$(cat "$RELEASE_NOTES" | jq -Rs .)
RELEASE_RESPONSE=$(curl -X POST \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases" \
    -d "{
        \"tag_name\": \"$TAG_NAME\",
        \"name\": \"$RELEASE_NAME\",
        \"body\": $RELEASE_BODY,
        \"draft\": false,
        \"prerelease\": false
    }")

RELEASE_ID=$(echo "$RELEASE_RESPONSE" | jq -r '.id // empty')

if [ -z "$RELEASE_ID" ]; then
    echo -e "${RED}Error creating release:${NC}"
    echo "$RELEASE_RESPONSE" | jq .
    exit 1
fi

echo -e "${GREEN}Release created (ID: $RELEASE_ID)${NC}"

echo -e "${YELLOW}Uploading APK...${NC}"
APK_SIZE=$(stat -c%s "$APK_PATH")
APK_SIZE_MB=$((APK_SIZE / 1024 / 1024))
echo "APK size: ${APK_SIZE_MB} MB"

UPLOAD_RESPONSE=$(curl -X POST \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/vnd.android.package-archive" \
    --data-binary @"$APK_PATH" \
    "https://uploads.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/$RELEASE_ID/assets?name=app-release.apk")

ASSET_ID=$(echo "$UPLOAD_RESPONSE" | jq -r '.id // empty')

if [ -z "$ASSET_ID" ]; then
    echo -e "${RED}Error uploading APK:${NC}"
    echo "$UPLOAD_RESPONSE" | jq .
    exit 1
fi

echo -e "${GREEN}APK uploaded successfully (Asset ID: $ASSET_ID)${NC}"

echo ""
echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}Release created successfully!${NC}"
echo -e "${GREEN}================================${NC}"
echo ""
echo "Release URL: https://github.com/$REPO_OWNER/$REPO_NAME/releases/tag/$TAG_NAME"
echo "APK Download: https://github.com/$REPO_OWNER/$REPO_NAME/releases/download/$TAG_NAME/app-release.apk"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo "1. Visit the release page to verify"
echo "2. Test the APK download"
echo "3. Share the release link with users"
