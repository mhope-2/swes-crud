#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
ENGINEER_ID="${ENGINEER_ID:-1}"
REQUESTS="${REQUESTS:-1000}"
CONCURRENCY="${CONCURRENCY:-20}"

READ_URL="${BASE_URL}/api/v1/software-engineer/${ENGINEER_ID}"
STATS_URL="${BASE_URL}/api/v1/cache/engineer-by-id/stats"

printf 'Reading engineer %s with %s requests at concurrency %s\n' \
  "$ENGINEER_ID" "$REQUESTS" "$CONCURRENCY"
printf 'Before:\n'
curl --fail --silent --show-error "$STATS_URL"
printf '\n'

seq "$REQUESTS" | xargs -n 1 -P "$CONCURRENCY" sh -c \
  'curl --fail --silent --show-error --output /dev/null "$0"' "$READ_URL"

printf 'After:\n'
curl --fail --silent --show-error "$STATS_URL"
printf '\n'
