package com.example.core;

import java.util.Map;

public record RxRequest(String methodName, Map<String, String> params)
{
}
