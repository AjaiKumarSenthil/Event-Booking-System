# Auth-Service

Issues and validates JWTs for the BookMyShow microservices. Uses an asymmetric
RSA keypair: the **private** key signs access/refresh tokens here, and every
other service (`API-Gateway`, `Booking-Service`, `Inventory-Service`,
`jwt-auth-starter`) verifies tokens with the matching **public** key.

- Port: `8083`
- Context path: `/auth/v1`
- DB: Postgres (`bookmyshow`) — shared with the other services

## JWT keypair (one-time local setup)

The repository **does not ship a private key** — you must generate one before
running `Auth-Service` for the first time. The public key is committed and
shared with every downstream service.

### Option A — keep keys at the default classpath location

```bash
cd Auth-Service/src/main/resources/keys

# 1. Generate a new 2048-bit RSA private key (PKCS#8, unencrypted)
openssl genpkey -algorithm RSA -out auth-private.pem -pkeyopt rsa_keygen_bits:2048

# 2. (Optional) regenerate the matching public key. Only needed if you also
#    want to rotate it across the other services — the committed
#    auth-public.pem already matches the previously-committed private key,
#    so if you generate a fresh private key you MUST regenerate this too
#    AND copy it to every other service's src/main/resources/keys/.
openssl rsa -in auth-private.pem -pubout -out auth-public.pem
```

If you regenerate the public key, copy it to:

- `API-Gateway/src/main/resources/keys/auth-public.pem`
- `Booking-Service/src/main/resources/keys/auth-public.pem`
- `Inventory-Service/src/main/resources/keys/auth-public.pem`

### Option B — point at keys outside the repo (recommended)

Export env vars before starting the service:

```powershell
# PowerShell
$env:JWT_PRIVATE_KEY = "file:C:/secrets/bookmyshow/auth-private.pem"
$env:JWT_PUBLIC_KEY  = "file:C:/secrets/bookmyshow/auth-public.pem"
```

```bash
# bash / zsh
export JWT_PRIVATE_KEY="file:/etc/bookmyshow/auth-private.pem"
export JWT_PUBLIC_KEY="file:/etc/bookmyshow/auth-public.pem"
```

The yaml uses Spring's `Resource` resolution, so any of `classpath:`, `file:`,
or absolute `file:///...` URIs work.

## Database

Default connection: `jdbc:postgresql://localhost:5432/bookmyshow`, user
`catalog`, password `catalog`. Override with env vars in non-dev environments:

```bash
export DB_URL="jdbc:postgresql://prod-host:5432/bookmyshow"
export DB_USERNAME="auth_app"
export DB_PASSWORD="..."   # never commit
```

Run [src/main/resources/schema.sql](src/main/resources/schema.sql) once to
create the `user`, `role`, `user_role`, and `refresh_token` tables and seed
the role rows.

## Running

```bash
mvn spring-boot:run
```
