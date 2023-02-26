## localstack config

docker run --rm -it -p 4566:4566 -p 4510-4559:4510-4559 localstack/localstack -e "SERVICES=sns, sqs, dynamodb, s3"