#!/bin/bash
set -e

echo "[1/6] SHUTDOWN IMMEDIATE..."
sqlplus -s / as sysdba <<EOF
SHUTDOWN IMMEDIATE;
EXIT;
EOF

echo "[2/6] STARTUP UPGRADE..."
sqlplus -s / as sysdba <<EOF
STARTUP UPGRADE;
EXIT;
EOF

echo "[3/6] CDB용 utl32k.sql 실행..."
sqlplus -s / as sysdba <<EOF
@?/rdbms/admin/utl32k.sql
EXIT;
EOF

echo "[4/6] 모든 PDB UPGRADE 모드로 열기..."
sqlplus -s / as sysdba <<EOF
ALTER PLUGGABLE DATABASE ALL OPEN UPGRADE;
EXIT;
EOF

echo "[5/6] FREEPDB1용 utl32k.sql 실행..."
sqlplus -s / as sysdba <<EOF
ALTER SESSION SET CONTAINER=FREEPDB1;
@?/rdbms/admin/utl32k.sql
ALTER SESSION SET CONTAINER=CDB\$ROOT;
EXIT;
EOF

echo "[6/6] SHUTDOWN 후 정상 시작..."
sqlplus -s / as sysdba <<EOF
SHUTDOWN IMMEDIATE;
EXIT;
EOF

sqlplus -s / as sysdba <<EOF
STARTUP;
ALTER PLUGGABLE DATABASE ALL OPEN;
EXIT;
EOF

echo "=== 완료: MAX_STRING_SIZE=EXTENDED 전체 마이그레이션 성공 ==="
