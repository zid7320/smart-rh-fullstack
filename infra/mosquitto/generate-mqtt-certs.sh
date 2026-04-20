#!/bin/bash

# ============================================================
# MQTT TLS Certificate & Device Credentials Generator
# Generates SSL certificates and device credentials for Mosquitto
# ============================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CERTS_DIR="${SCRIPT_DIR}/certs"
PASSWD_FILE="${SCRIPT_DIR}/passwd"

# Configuration
COUNTRY="MA"
STATE="Casablanca"
CITY="Casablanca"
ORG="SMART RH 4.0"
COMMON_NAME="mosquitto.smartrh.local"
VALIDITY_DAYS=365

echo "🔒 MQTT TLS Certificate & Credentials Generator"
echo "=================================================="
echo ""

# Create certs directory
mkdir -p "${CERTS_DIR}"
chmod 700 "${CERTS_DIR}"

echo "[1/3] Generating CA Certificate..."
# Generate CA private key (4096-bit RSA)
openssl genrsa -out "${CERTS_DIR}/ca.key" 4096 2>/dev/null
echo "     ✓ CA private key generated"

# Generate CA certificate (self-signed)
openssl req -new -x509 -days ${VALIDITY_DAYS} -key "${CERTS_DIR}/ca.key" \
  -out "${CERTS_DIR}/ca.crt" \
  -subj "/C=${COUNTRY}/ST=${STATE}/L=${CITY}/O=${ORG}/CN=SMART-RH-CA" \
  2>/dev/null
echo "     ✓ CA certificate generated"

echo ""
echo "[2/3] Generating Server Certificate..."
# Generate server private key
openssl genrsa -out "${CERTS_DIR}/server.key" 2048 2>/dev/null
echo "     ✓ Server private key generated"

# Generate server certificate signing request (CSR)
openssl req -new \
  -key "${CERTS_DIR}/server.key" \
  -out "${CERTS_DIR}/server.csr" \
  -subj "/C=${COUNTRY}/ST=${STATE}/L=${CITY}/O=${ORG}/CN=${COMMON_NAME}" \
  2>/dev/null
echo "     ✓ Server CSR generated"

# Sign server certificate with CA
openssl x509 -req -in "${CERTS_DIR}/server.csr" \
  -CA "${CERTS_DIR}/ca.crt" \
  -CAkey "${CERTS_DIR}/ca.key" \
  -CAcreateserial \
  -out "${CERTS_DIR}/server.crt" \
  -days ${VALIDITY_DAYS} \
  -extensions v3_req \
  -extfile <(printf "subjectAltName=DNS:${COMMON_NAME},DNS:mosquitto,DNS:localhost,IP:127.0.0.1") \
  2>/dev/null
echo "     ✓ Server certificate signed by CA"

# Remove CSR (no longer needed)
rm -f "${CERTS_DIR}/server.csr"

echo ""
echo "[3/3] Creating Device Credentials File..."

# Create/reset password file
> "${PASSWD_FILE}"
chmod 600 "${PASSWD_FILE}"

# Function to add device credential
add_device_credential() {
  local device_name=$1
  local device_id=$2
  local password=${3:-$(openssl rand -base64 16 | sed 's/=//g')}

  # Use mosquitto_passwd to hash the password (bcrypt)
  mosquitto_passwd -b "${PASSWD_FILE}" "device_${device_id}" "${password}" 2>/dev/null

  echo "     ✓ Device: device_${device_id}"
}

# Add backend admin credentials
ADMIN_PASS=$(openssl rand -base64 16 | sed 's/=//g')
mosquitto_passwd -b "${PASSWD_FILE}" "backend_admin" "${ADMIN_PASS}" 2>/dev/null
echo "     ✓ Backend admin: backend_admin"

# Add facial recognition cameras
add_device_credential "Camera 1" "camera_01"
add_device_credential "Camera 2" "camera_02"
add_device_credential "Camera 3" "camera_03"

# Add access control readers
add_device_credential "Access Reader 1" "access_reader_01"
add_device_credential "Access Reader 2" "access_reader_02"

# Add motion sensors
add_device_credential "Motion Sensor 1" "motion_01"
add_device_credential "Motion Sensor 2" "motion_02"

# Add door locks
add_device_credential "Door Lock 1" "lock_01"
add_device_credential "Door Lock 2" "lock_02"

# Add temperature sensors
add_device_credential "Temperature Sensor 1" "temperature_01"
add_device_credential "Temperature Sensor 2" "temperature_02"

echo ""
echo "✅ Certificate & Credentials Generation Complete!"
echo ""
echo "📁 Generated Files:"
echo "   - ${CERTS_DIR}/ca.crt (CA Certificate - share with clients)"
echo "   - ${CERTS_DIR}/server.crt (Server Certificate)"
echo "   - ${CERTS_DIR}/server.key (Server Key - keep secret!)"
echo "   - ${PASSWD_FILE} (Device Credentials - keep secret!)"
echo ""
echo "📝 Certificate Details:"
echo "   - Validity: ${VALIDITY_DAYS} days"
echo "   - Algorithms: RSA-4096 (CA), RSA-2048 (Server)"
echo "   - Common Name: ${COMMON_NAME}"
echo ""
echo "🚀 Next Steps:"
echo "   1. Mount certs/ directory in Docker container:"
echo "      -v \$(pwd)/certs:/etc/mosquitto/certs:ro"
echo "   2. Mount passwd file in Docker container:"
echo "      -v \$(pwd)/passwd:/etc/mosquitto/passwd:ro"
echo "   3. Mount acl file in Docker container:"
echo "      -v \$(pwd)/acl:/etc/mosquitto/acl:ro"
echo "   4. Expose ports: 1883 (unencrypted), 8883 (TLS)"
echo ""
echo "🔐 Device Connection Example:"
echo "   mqtt://device_camera_01:{PASSWORD}@mosquitto.smartrh.local:8883"
echo "   (Use ca.crt for TLS verification)"
echo ""
echo "⚠️  Security Reminders:"
echo "   - Keep server.key and passwd file confidential!"
echo "   - Rotate credentials regularly"
echo "   - Restrict file permissions: chmod 600"
echo "   - Use strong passwords for new devices"
echo ""
