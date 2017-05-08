package siosio.testapp

import nablarch.common.web.session.*
import nablarch.fw.*
import nablarch.fw.web.*

class NotFoundSession : Handler<HttpRequest, HttpResponse> {
    override fun handle(data: HttpRequest, context: ExecutionContext): HttpResponse {
        return try {
            SessionUtil.get<Any>(context, "key")
            HttpResponse(200)
        } catch(e: SessionKeyNotFoundException) {
            HttpResponse(400)
        }
    }
}