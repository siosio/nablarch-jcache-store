package siosio.testapp

import nablarch.common.web.session.*
import nablarch.fw.*
import nablarch.fw.web.*

class InvalidateAndPutSession : Handler<HttpRequest, HttpResponse> {
    override fun handle(data: HttpRequest, context: ExecutionContext): HttpResponse {
        SessionUtil.invalidate(context)
        SessionUtil.put(context, "キー", "あたい")
        return HttpResponse(200)
    }
}