package siosio.testapp

import nablarch.common.web.session.*
import nablarch.core.date.*
import nablarch.core.repository.*
import nablarch.fw.*
import nablarch.fw.web.*
import nablarch.fw.web.servlet.*
import siosio.*
import siosio.SessionStoreHandler
import java.util.concurrent.*
import javax.cache.*
import javax.servlet.*
import javax.servlet.annotation.*

@WebFilter("/testapp")
class LogicBasedWebFrontController : WebFrontController() {

    override fun init(config: FilterConfig) {
        super.init(config)
        initializeRepository()
        val sessionStoreHandler = SessionStoreHandler()
        sessionStoreHandler.setSessionManager(SystemRepository.get("sessionManager"))

        setHandlerQueue(listOf(
            sessionStoreHandler,
            Handler<HttpRequest, Any> { data, context ->
                data.getParam("c").firstOrNull()?.let {
                    val clazz = Class.forName(it)
                    context.addHandler(clazz.newInstance())
                    context.handleNext<HttpRequest, Any>(data)
                } ?: HttpResponse(404)
            }
        ))
    }

    private fun initializeRepository() {
        SystemRepository.load({
            mapOf(
                "systemTimeProvider" to BasicSystemTimeProvider(),
                "sessionManager" to run {
                    val sessionManager = SessionManager()
                    val jCacheStore = JCacheStore()
                    jCacheStore.setExpires(5, TimeUnit.SECONDS)
                    jCacheStore.initialize()
                    sessionManager.setDefaultStoreName("mem")
                    sessionManager.availableStores = listOf(jCacheStore)
                    sessionManager
                }
            )
        })
    }

    override fun destroy() {
        Caching.getCachingProvider().cacheManager.close()
    }
}