package siosio.testapp

import nablarch.common.web.session.*
import nablarch.fw.*
import nablarch.fw.web.*

class PutSession : Handler<HttpRequest, HttpResponse> {
    override fun handle(data: HttpRequest, context: ExecutionContext): HttpResponse {
        SessionUtil.put(context, "key", "保存するデータ")
        return HttpResponse(200)
    }
}