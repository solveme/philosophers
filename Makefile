.PHONY: help

REGISTRY = docker-registry.cardpay-test.com
IMAGE = cardpay/keycloak-service

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

install: ## Install with running unit tests
	mvn clean install -U

install-only: ## Install without running tests
	mvn clean install -U -DskipTests

run: ## Run
	mvn clean package -DskipTests
	./run.sh