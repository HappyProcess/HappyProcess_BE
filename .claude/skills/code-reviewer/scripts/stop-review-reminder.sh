#!/usr/bin/env bash
# Stop 훅: 현재 브랜치에 main 대비(또는 커밋 안 된) 변경분이 있는데
# 아직 리뷰되지 않았으면 Claude가 code-reviewer 스킬로 리뷰하도록 알린다.
#
# 무한 루프 방지:
#   1) stop_hook_active=true 면 즉시 종료 (직전 블록으로 인한 재진입)
#   2) 변경분 해시를 .git/.code-review-done 에 기록 → 같은 변경분은 한 번만 알림

input=$(cat)

# 1) 직전 Stop 훅 블록으로 다시 들어온 경우 → 그냥 종료
[ "$(printf '%s' "$input" | jq -r '.stop_hook_active // false')" = "true" ] && exit 0

# git 저장소가 아니면 종료
root=$(git rev-parse --show-toplevel 2>/dev/null) || exit 0

# 비교 기준 브랜치(main 없으면 master, 둘 다 없으면 커밋 안 된 변경만)
base=""
for b in main master; do
  if git rev-parse --verify --quiet "$b" >/dev/null; then base="$b"; break; fi
done

if [ -n "$base" ]; then
  diff=$(git diff "$base"...HEAD; git diff; git diff --staged)
else
  diff=$(git diff; git diff --staged)
fi

# 변경분 없으면 종료
[ -z "$diff" ] && exit 0

# 이미 같은 변경분을 리뷰 알림했으면 종료
hash=$(printf '%s' "$diff" | shasum | cut -d' ' -f1)
marker="$root/.git/.code-review-done"
[ "$(cat "$marker" 2>/dev/null)" = "$hash" ] && exit 0
printf '%s' "$hash" > "$marker"

# 변경분 있음 → 리뷰 유도 (Stop을 차단하고 reason을 Claude에 전달)
printf '%s' '{"decision":"block","reason":"현재 브랜치에 아직 리뷰하지 않은 코드 변경분이 있습니다. code-reviewer 스킬을 사용해 변경분(버그/로직, 성능, 스타일 관점)을 리뷰하고 핵심 지적을 한국어로 정리하세요. 방금 구현을 마친 직후라면 특히 유용합니다. 이미 이번 작업의 변경분을 리뷰했거나 사용자가 리뷰를 원치 않는다고 했다면, 추가 작업 없이 그대로 종료하세요."}'
