## Distribution to Google Cloud Run via GitHub Actions


### Cloud Run

To make your life easier, export these environment variables so that you can copy and paste the commands used here. Choose whatever name you want, but the $PROJECT_ID has to be a unique name, because project IDs can't be reused in Google Cloud.

```
export PROJECT_ID=
export ACCOUNT_NAME=
```

For example, your commands should look something like this:

```
export PROJECT_ID=foobar
export ACCOUNT_NAME=foobar
```

Log in with your Google account:

```
gcloud auth login
```

```
gcloud projects create $PROJECT_ID
gcloud config set project $PROJECT_ID
```

Enable billing for your project, and create a billing profile if you don’t have one:

```
open "https://console.cloud.google.com/billing/linkedaccount?project=$PROJECT_ID"
```

Enable the necessary services:

```
gcloud services enable cloudbuild.googleapis.com run.googleapis.com containerregistry.googleapis.com
```

Create a service account:

```
gcloud iam service-accounts create $ACCOUNT_NAME --description="Cloud Run deploy account" --display-name="Cloud-Run-Deploy"
```

Give the service account Cloud Run Admin, Storage Admin, and Service Account User roles. You can’t set all of them at once, so you have to run separate commands:

```
gcloud projects add-iam-policy-binding $PROJECT_ID --member=serviceAccount:$ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com --role=roles/run.admin

gcloud projects add-iam-policy-binding $PROJECT_ID --member=serviceAccount:$ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com --role=roles/storage.admin

gcloud projects add-iam-policy-binding $PROJECT_ID --member=serviceAccount:$ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com --role=roles/iam.serviceAccountUser

gcloud projects add-iam-policy-binding $PROJECT_ID --member=serviceAccount:$ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com --role=roles/artifactregistry.writer
```

Generate a key.json file with your credentials, so your GitHub workflow can authenticate with Google Cloud:

```
gcloud iam service-accounts keys create key.json --iam-account $ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com
```

### GitHub Actions

GitHub, you need to set up a secrets environment in your repository, with the following values:

- `GCP_PROJECT_ID` is your `$PROJECT_ID`.
- `GCP_APP_NAME` is your app name.
- `GCP_CREDENTIALS` is the content from the key.json file that you just created.

For example, your settings should look something like this:

`GCP_PROJECT_ID = foobar`  
`GCP_APP_NAME = foobar`

Cat the `key.json` content and paste it into the `GCP_CREDENTIALS` secret value.

Now you just need to create a YAML file telling which commands your workflow should run. In your project directory, create a folder called `.github` and create another one inside it called `workflows`.

See the workflows in this project for examples.

## Cloud SQL Setup

![Cloud SQL Admin API Enable Screenshot](docs/enable-cloud-sql-admin-api.png)

### Cloud SQL Instance

Create an instance of Cloud SQL:

![](docs/cloud_sql_create_intance.png)

Choose PostgreSQL as your database engine:

![](docs/choose-postgresql.png)

![Enable Compute Engine API](docs/enable-compute-engine-api.png)

Configure it with the following options:

![](docs/dbinstance-options.png)

### Databases

Create two databases `grailsforge-production` and `grailsforge-snapshot`

![Cloud SQL Databases Screenshot](docs/create-two-databases.png)

## Cloud Run environment variables

| Name                           | Value                                          | 
|:-------------------------------|:-----------------------------------------------|
| `MICRONAUT_ENV_DEDUCTION`      | `false`                                        |
| `MICRONAUT_ENVIRONMENTS`       | `gcp`                                          |
| `API_KEY`                      | `Allowed API Key`                              |
| `CLOUD_SQL_CONNECTION_NAME`    | `GC_PROJECT_ID:REGION:CLOUD_SQL_INSTANCE_NAME` |
| `DATASOURCES_DEFAULT_PASSWORD` | Database password                              | 
| `DATASOURCES_DEFAULT_USERNAME` | Database username                              |
| `DB_NAME`                      | Database name                                  |
| `DATASOURCES_DEFAULT_URL`      | Database JDBC URL                              |