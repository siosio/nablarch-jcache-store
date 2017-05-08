package siosio.testapp

import nablarch.common.web.session.*
import nablarch.fw.*
import nablarch.fw.web.*

class GetSession : Handler<HttpRequest, HttpResponse> {
    override fun handle(data: HttpRequest, context: ExecutionContext): HttpResponse {
        val session: String = SessionUtil.get(context, "key")
        return if (session == "保存するデータ") {
            HttpResponse(200)
        } else {
            HttpResponse(400)
        }
    }
}