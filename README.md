# Lebvest

## Production flow

Production is now split into two clean phases:

1. `terraform apply` provisions AWS resources and prepares the EC2 instance for deployments.
2. A push to `main` runs the production workflow, builds the image, and deploys it to the EC2 instance through AWS Systems Manager.

After the first infrastructure apply, the deploy path no longer depends on the instance public IP, SSH connectivity, or manual Docker Compose installation.

## What Terraform provisions

Terraform creates and configures:

- VPC, subnets, routes, and security groups
- EC2, RDS, Amazon MQ, S3, and Secrets Manager
- EC2 IAM permissions for Secrets Manager, S3, MQ, and SSM
- Docker, Docker Compose, `curl`, and the SSM agent on the app instance
- `/opt/lebvest/.env.infrastructure` on the instance with infra-owned runtime values:
  - `APP_SECRETS_NAME`
  - `APP_S3_BUCKET_NAME`
  - `AWS_REGION`
  - `FRONTEND_BASE_URL`
  - `SPRING_PROFILES_ACTIVE`

## CI/CD expectations

The production workflow in `.github/workflows/prod-ci.yml` expects these GitHub secrets:

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_REGION`
- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`

The workflow:

1. runs `./mvnw verify`
2. builds and pushes the production image
3. finds the running prod instance by the `DeployTarget=prod` tag
4. pushes the latest compose file and deploy script through SSM
5. runs the deploy script on the instance and waits for actuator health

## First production move

Use `terraform.tfvars.example` as the template for your production values, especially:

- database and RabbitMQ credentials
- mail credentials
- `frontend_base_url`
- optional legacy SSH settings if you still want SSH access

Then run:

```bash
terraform init
terraform apply
```

Once the EC2 instance is up and visible in SSM, push to `main` to trigger the first application rollout.

## What you no longer need to do manually

- install Docker Compose on the EC2 instance
- keep `EC2_HOST`, `EC2_USER`, or `EC2_SSH_KEY` in GitHub secrets
- SSH into the host just to copy `compose.prod.yaml`
- patch the prod S3 bucket name after provisioning
- hardcode the Secrets Manager name or AWS region inside the prod profile
