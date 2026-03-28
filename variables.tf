variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "app_name" {
  description = "Application name used for resource naming"
  type        = string
  default     = "lebvest"
}

variable "ec2_ami" {
  description = "AMI ID for EC2 (Amazon Linux 2023 us-east-1)"
  type        = string
  default     = "ami-0c02fb55956c7d316"
}

variable "ec2_instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.small"
}

variable "ec2_key_pair_name" {
  description = "Optional EC2 key pair name for legacy SSH access"
  type        = string
  default     = ""
}

variable "enable_ssh_access" {
  description = "Whether to open SSH ingress and attach an EC2 key pair"
  type        = bool
  default     = false
}

variable "ssh_ingress_cidr" {
  description = "CIDR allowed to reach SSH when enable_ssh_access is true"
  type        = string
  default     = "0.0.0.0/0"
}

variable "docker_compose_version" {
  description = "Docker Compose v2 release tag (git tag) installed on EC2 for deploy scripts"
  type        = string
  default     = "v2.29.7"
}

variable "rds_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "db_name" {
  description = "PostgreSQL database name"
  type        = string
  default     = "lebvest"
}

variable "db_username" {
  description = "PostgreSQL master username"
  type        = string
  default     = "lebvest_admin"
}

variable "db_password" {
  description = "PostgreSQL master password"
  type        = string
  sensitive   = true
}

variable "mq_instance_type" {
  description = "AmazonMQ instance type"
  type        = string
  default     = "mq.t3.micro"
}

variable "mq_username" {
  description = "RabbitMQ username"
  type        = string
  default     = "lebvest_mq"
}

variable "mq_password" {
  description = "RabbitMQ password"
  type        = string
  sensitive   = true
}

variable "s3_bucket_name" {
  description = "S3 bucket name for app assets (globally unique)"
  type        = string
  default     = "lebvest-assets"
}

variable "mail_host" {
  description = "SMTP host"
  type        = string
  default     = "smtp.gmail.com"
}

variable "mail_port" {
  description = "SMTP port"
  type        = number
  default     = 587
}

variable "mail_username" {
  description = "SMTP username"
  type        = string
}

variable "mail_password" {
  description = "SMTP password"
  type        = string
  sensitive   = true
}

variable "mail_from" {
  description = "From address for outbound mail"
  type        = string
}

variable "admin_email" {
  description = "Admin notification recipient"
  type        = string
}

variable "frontend_base_url" {
  description = "Frontend base URL injected into the production app runtime"
  type        = string
  default     = "http://localhost:4200"
}
