#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT"

for report in */target/cppbridge/native-build-report.txt; do
  if [[ -f "$report" ]]; then
    echo "==== $report ===="
    cat "$report"
    echo
  fi
done
