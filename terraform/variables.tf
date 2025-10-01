variable "instance_type" {
  type = string                     # The type of the variable, in this case a string
  default = "t3.micro"                 # Default value for the variable

  description = "The type of EC2 instance" # Description of what this variable represents
}

variable "region" {
  description = "region"
  default     = "ap-northeast-2"
}

variable "prefix" {
  description = "Prefix for all resources"
  default     = "team2"
}

variable "app_1_domain" {
  description = "app_1 domain"
  default     = "api.ssoul.life"
}

variable "s3_bucket_name" {
  description = "S3 bucket name for file storage"
  default     = "app-s3-bucket"
}

variable "s3_public_read" {
  description = "Enable public read access for S3 bucket"
  type        = bool
  default     = true
}