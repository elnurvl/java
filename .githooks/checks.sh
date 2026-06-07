#!/usr/bin/env bash
set -e

echo "[1/4] Spotless: auto-fixing formatting..."
./gradlew spotlessApply --quiet

echo "[2/4] Checkstyle: checking code style..."
./gradlew checkstyleMain checkstyleTest --quiet

echo "[3/4] Tests: running test suite..."
./gradlew test --quiet

echo "[4/4] Coverage: checking >= 80%..."
./gradlew testCodeCoverageReport --quiet

CSV="build/reports/jacoco/test/jacocoTestReport.csv"
if [ ! -f "$CSV" ]; then
  echo "ERROR: JaCoCo CSV report not found at $CSV"
  exit 1
fi

# Sum instruction missed/covered from CSV (skip header)
read -r missed covered <<< "$(awk -F',' 'NR>1 {m+=$4; c+=$5} END {print m, c}' "$CSV")"
total=$((missed + covered))
if [ "$total" -eq 0 ]; then
  echo "ERROR: No coverage data found"
  exit 1
fi
pct=$((covered * 100 / total))
echo "Coverage: ${pct}%"
if [ "$pct" -lt 80 ]; then
  echo "ERROR: Coverage ${pct}% is below 80% threshold"
  exit 1
fi

echo "All checks passed."
