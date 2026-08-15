# 10 — Deployment

**Purpose:** Documents the actual CI/CD pipeline, Azure deployment, authentication, and the required GitHub configuration for the DevLaunch project.

---

# 1. Deployment Overview

The application is **already deployed and working** on Microsoft Azure Container Apps. The GitHub Actions workflows in `.github/workflows/` implement CI (validation) and CD (build → push → deploy). The existing Azure resources are the source of truth for infrastructure — the pipeline **creates nothing** and only updates the container image on the existing apps.

```
GitHub
  ↓
GitHub Actions
  ├── CI  (ci.yml)   — tests + builds + docker validation (never deploys)
  └── CD  (cd.yml)   — Azure OIDC login → ACR push → containerapp update
                        ↓
Azure Container Registry (devlaunchacr)
                        ↓
Azure Container Apps (devlaunch-backend, devlaunch-frontend)
                        ↓
Live application
```

# 2. Existing Azure Resources

| Resource | Name |
|---|---|
| Resource group | `devlaunch-rg` |
| Container Registry | `devlaunchacr` |
| Container Apps | `devlaunch-backend`, `devlaunch-frontend` |
| Container Apps environment | `devlaunch-env` |

The MySQL database, Redis, and RabbitMQ used by the deployed backend are part of the existing environment configuration and are **not** created or modified by the pipeline.

# 3. CI — `ci.yml` (validation only)

**Triggers:** pull requests targeting `develop` or `main`; pushes to `develop` or `main`.

**Jobs (Ubuntu GitHub-hosted runners):**

1. **Backend tests** — Java 21 (Temurin, Maven cache); `chmod +x ./mvnw && ./mvnw test` (208 tests).
2. **Frontend build** — Node 20; `npm ci` → `npm run build`.
3. **Docker build** — `docker build` of both Dockerfiles (tagged `:ci` locally only).

**Explicitly out of scope for CI:** pushing images to ACR, touching Azure, deploying. CI only validates that tests, the production build, and the Dockerfiles are healthy.

# 4. CD — `cd.yml` (deploys to existing Azure resources)

**Triggers:** push to `main`; manual `workflow_dispatch`. Gated by the GitHub **`production`** environment; a `concurrency` guard ensures only one production deployment runs at a time.

**Steps:**

1. **Checkout** the repository.
2. **Azure login (OIDC)** — `azure/login@v2` with `AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_SUBSCRIPTION_ID` (federated credentials — no long-lived Azure client secret is stored in GitHub).
3. **Backend image** — `az acr login --name devlaunchacr`, then build + push `devlaunchacr.azurecr.io/devlaunch-backend:${{ github.sha }}` from `backend/`.
4. **Frontend image** — same pattern from `frontend/`, built with `--build-arg VITE_API_URL="$VITE_API_URL"`. The build **fails loudly** if the `VITE_API_URL` repository variable is unset (Vite bakes the API URL into the bundle at build time, so a missing value would produce a broken frontend).
5. **Deploy backend** — `az containerapp update --name devlaunch-backend --resource-group devlaunch-rg --image devlaunchacr.azurecr.io/devlaunch-backend:${{ github.sha }}`.
6. **Deploy frontend** — same for `devlaunch-frontend`.

`az containerapp update --image …` changes **only the image reference** on the existing Container App; Azure Container Apps creates a normal new revision. The environment variables and secrets already configured on the apps are **preserved**.

**Why immutable `github.sha` tags?** Each deployed revision maps to exactly one commit, so any revision can be rolled back by re-deploying its previous image tag. No `latest` tag is used for deployments.

# 5. GitHub Configuration (required before CD works)

**Secrets** — configured on the GitHub `production` environment:

| Secret | Value |
|---|---|
| `AZURE_CLIENT_ID` | Client ID of the Azure app registration with the OIDC federated credential (issuer `https://token.actions.githubusercontent.com`) |
| `AZURE_TENANT_ID` | Azure tenant ID |
| `AZURE_SUBSCRIPTION_ID` | Azure subscription ID |

**Variables** — repository-level:

| Variable | Value |
|---|---|
| `VITE_API_URL` | Public URL of the **existing** `devlaunch-backend` Container App (e.g. `https://devlaunch-backend.<env>.azurecontainerapps.io`) |

**Azure permissions required for the federated identity:**

- `AcrPush` on the existing `devlaunchacr` registry (push images).
- `Contributor` (or a scoped role allowing `Microsoft.App/containerApps/write`) on the existing `devlaunch-backend` and `devlaunch-frontend` Container Apps in `devlaunch-rg` (update image/revision).

# 6. What the Pipeline Does NOT Do

- Creates no resource group, ACR, Container App, MySQL, Redis, RabbitMQ, or Log Analytics.
- Does not modify Container App environment variables or secrets.
- Does not change `application.yml`, `SecurityConfig`, CORS, or JWT configuration.
- Does not use Azure admin credentials or store long-lived Azure secrets in the repository.

# 7. Manual Rollback

To roll back a revision, re-run the CD workflow steps with the previously deployed tag, or use the Azure CLI/portal to point the Container App at an older `devlaunchacr.azurecr.io/devlaunch-*:<sha>` image.

# 8. Local Development Alternative

For local development the same services run via `docker/docker-compose.yml` (MySQL, Redis, RabbitMQ, backend, frontend). See `README.md` → Docker for ports and the required `devlaunch-network` external network.
