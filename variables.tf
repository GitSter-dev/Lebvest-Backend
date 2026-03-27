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
  description = "Name of your existing EC2 key pair for SSH access"
  type        = string
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
