package com.onirun.api

import org.json.JSONObject

data class ErrorResponse(val httpCode: Int, val errorData: JSONObject?)