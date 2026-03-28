output "ec2" {
  description = "EC2 instance (app)"
  value = {
    id                   = aws_instance.app.id
    arn                  = aws_instance.app.arn
    instance_type        = aws_instance.app.instance_type
    ami                  = aws_instance.app.ami
    availability_zone    = aws_instance.app.availability_zone
    subnet_id            = aws_instance.app.subnet_id
    vpc_security_groups  = aws_instance.app.vpc_security_group_ids
    iam_instance_profile = aws_instance.app.iam_instance_profile
    key_name             = aws_instance.app.key_name
    public_ip            = aws_instance.app.public_ip
    public_dns           = aws_instance.app.public_dns
    private_ip           = aws_instance.app.private_ip
    private_dns          = aws_instance.app.private_dns
  }
}

output "rds" {
  description = "RDS PostgreSQL instance"
  value = {
    id             = aws_db_instance.postgres.id
    arn            = aws_db_instance.postgres.arn
    identifier     = aws_db_instance.postgres.identifier
    address        = aws_db_instance.postgres.address
    port           = aws_db_instance.postgres.port
    endpoint       = aws_db_instance.postgres.endpoint
    engine         = aws_db_instance.postgres.engine
    engine_version = aws_db_instance.postgres.engine_version
    db_name        = aws_db_instance.postgres.db_name
    username       = aws_db_instance.postgres.username
    hosted_zone_id = aws_db_instance.postgres.hosted_zone_id
    jdbc_url       = "jdbc:postgresql://${aws_db_instance.postgres.address}:${aws_db_instance.postgres.port}/${aws_db_instance.postgres.db_name}"
  }
}

output "amqp" {
  description = "Amazon MQ (RabbitMQ) broker — AMQPS endpoints, console, broker metadata (password is not exposed)"
  value = {
    id                 = aws_mq_broker.rabbitmq.id
    arn                = aws_mq_broker.rabbitmq.arn
    broker_name        = aws_mq_broker.rabbitmq.broker_name
    engine_type        = aws_mq_broker.rabbitmq.engine_type
    engine_version     = aws_mq_broker.rabbitmq.engine_version
    host_instance_type = aws_mq_broker.rabbitmq.host_instance_type
    deployment_mode    = aws_mq_broker.rabbitmq.deployment_mode
    subnet_ids         = aws_mq_broker.rabbitmq.subnet_ids
    security_groups    = aws_mq_broker.rabbitmq.security_groups
    instances          = aws_mq_broker.rabbitmq.instances
    console_url        = aws_mq_broker.rabbitmq.instances[0].console_url
    amqp_endpoints     = aws_mq_broker.rabbitmq.instances[0].endpoints
    username           = var.mq_username
  }
}

# Convenience aliases (same values as nested fields above)
output "ec2_public_ip" {
  description = "EC2 public IP"
  value       = aws_instance.app.public_ip
}

output "ec2_public_dns" {
  description = "EC2 public DNS"
  value       = aws_instance.app.public_dns
}

output "ec2_instance_id" {
  description = "EC2 instance ID"
  value       = aws_instance.app.id
}

output "ec2_deploy_target" {
  description = "SSM tag value used by CI to target the production instance"
  value       = aws_instance.app.tags["DeployTarget"]
}

output "rds_endpoint" {
  description = "RDS hostname only (use rds.endpoint for host:port)"
  value       = aws_db_instance.postgres.address
}

output "mq_endpoint" {
  description = "Primary AmazonMQ AMQP endpoint URL (see amqp.amqp_endpoints for all)"
  value       = aws_mq_broker.rabbitmq.instances[0].endpoints[0]
}

output "mq_console_url" {
  description = "RabbitMQ management console URL"
  value       = aws_mq_broker.rabbitmq.instances[0].console_url
}

output "secrets_manager_arn" {
  description = "ARN of the Secrets Manager secret"
  value       = aws_secretsmanager_secret.app.arn
}

output "secrets_manager_name" {
  description = "Secrets Manager name consumed by the production app"
  value       = aws_secretsmanager_secret.app.name
}

output "s3_assets" {
  description = "S3 bucket for app assets"
  value = {
    id  = aws_s3_bucket.assets.id
    arn = aws_s3_bucket.assets.arn
  }
}
