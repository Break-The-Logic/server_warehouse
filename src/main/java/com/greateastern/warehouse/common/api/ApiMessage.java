package com.greateastern.warehouse.common.api;

public sealed interface ApiMessage permits TextMessage, BugFailureMessage, ExternalApiFailureMessage, MissingFieldsFailureMessage { }
