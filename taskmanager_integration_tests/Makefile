.PHONY: test allure zip

test:
	mvn -q test

allure-report:
	mvn -q test
	# generate allure results if using the plugin (requires configuration)

zip:
	rm -f taskmanager_integration_tests.zip
	zip -r taskmanager_integration_tests.zip .
