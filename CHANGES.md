# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.5.0] - 2024-12-06
- `ValidationError` contains a new `kind` property that can be used to decide how to handle an invalid request. This is a minor breaking change for custom types and custom validations. 

## [0.4.14] - 2024-11-08
- You can now access the current request method and path in every context by accessing the `requestMethod` and `requestPath` properties. This is useful for custom logging requirements or to implement metrics.

## [0.4.13] - 2024-10-29
- Initial public release
