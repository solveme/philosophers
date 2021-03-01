.PHONY: help

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

install: ## Install with running unit tests
	mvn clean install -U

install-only: ## Install without running tests
	mvn clean install -DskipTests

package: ## Package
	mvn clean package -DskipTests

build-app: ## Builds projects as distributable bundle (via appassembler-plugin)
	mvn clean package appassembler:assemble

build-uberjar: ## Builds projects as single "fat" jar (via shade-plugin)
	mvn clean package shade:shade
