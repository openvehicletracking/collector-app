package com.openvehicletracking.collector.helper;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;

/**
 * Created by oksuz on 13/05/2017.
 *
 */
public class HttpHelper {


    public static HttpServerResponse getUnauthorized(HttpServerResponse response) {
        String error = new JsonObject().put("error", "unauthorized").toString();
        return response.setStatusCode(HttpResponseStatus.UNAUTHORIZED.code())
                .setStatusMessage(HttpResponseStatus.UNAUTHORIZED.reasonPhrase())
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(error.getBytes().length))
                .write(error);
    }

    public static HttpServerResponse getBadRequest(HttpServerResponse response, String error) {
        error = (null == error) ? HttpResponseStatus.BAD_REQUEST.reasonPhrase() : error;
        String out = new JsonObject().put("error", error).toString();

        return response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .setStatusMessage(HttpResponseStatus.BAD_REQUEST.reasonPhrase())
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(out.getBytes().length))
                .write(out);
    }

    public static HttpServerResponse getInternalServerError(HttpServerResponse response, String error) {
        String out = new JsonObject().put("error", error).toString();

        return response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                .setStatusMessage(HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase())
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(out.getBytes().length))
                .write(out);
    }

    public static HttpServerResponse getOK(HttpServerResponse response, String out) {
        return response.setStatusCode(HttpResponseStatus.OK.code())
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(out.getBytes().length))
                .setStatusMessage(HttpResponseStatus.OK.reasonPhrase())
                .write(out);
    }

    public static HttpServerResponse getOK(HttpServerResponse response, JsonObject out) {
        return getOK(response, out.toString());
    }

    public static HttpServerResponse getOKNoContent(HttpServerResponse response) {
        return response.setStatusCode(HttpResponseStatus.NO_CONTENT.code())
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .setStatusMessage(HttpResponseStatus.NO_CONTENT.reasonPhrase());
    }


    public static HttpServerResponse getNotFound(HttpServerResponse response, String error) {
        String out = new JsonObject().put("error", error).toString();

        return response.setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .setStatusMessage(HttpResponseStatus.NOT_FOUND.reasonPhrase())
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(out.getBytes().length))
                .write(out);
    }

    public static HttpServerResponse getGone(HttpServerResponse response, String error) {
        String out = new JsonObject().put("gone", error).toString();

        return response.setStatusCode(HttpResponseStatus.GONE.code())
                .setStatusMessage(HttpResponseStatus.GONE.reasonPhrase())
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(out.getBytes().length))
                .write(out);
    }
}
