package siosio

import nablarch.common.web.session.*
import org.assertj.core.api.*
import org.assertj.core.api.Assertions.*
import org.jboss.arquillian.container.test.api.*
import org.jboss.arquillian.junit.*
import org.jboss.arquillian.test.api.*
import org.jboss.shrinkwrap.api.*
import org.jboss.shrinkwrap.api.spec.*
import org.junit.*
import org.junit.runner.*
import java.net.*
import java.util.concurrent.*
import javax.ws.rs.client.*


/**
 * [SessionStoreHandler]のテスト。
 */
@RunWith(Arquillian::class)
class SessionStoreHandlerTest {

    companion object {
        @Deployment
        @JvmStatic
        fun createDeployment(): WebArchive {
            val archive = ShrinkWrap.create(WebArchive::class.java, "web.war")
                .addPackages(true, "siosio.testapp")
            return archive
        }
    }

    @ArquillianResource
    private lateinit var deploymentUri: URI

    @Test
    @RunAsClient
    internal fun セッションストアに情報を設定した場合はクッキー情報等が設定され次のリクエストでセッションストアにアクセスできること() {
        val webTarget = ClientBuilder.newClient()
            .target(deploymentUri)
            .path("/testapp")

        // put session request
        val sid = webTarget
            .queryParam("c", "siosio.testapp.PutSession")
            .request()
            .get()
            .cookies.get("NABLARCH_SID")

        assertThat(sid)
            .hasFieldOrPropertyWithValue("path", "/")
            .hasFieldOrPropertyWithValue("maxAge", -1)
            .hasFieldOrPropertyWithValue("httpOnly", true)

        val response = webTarget.queryParam("c", "siosio.testapp.GetSession")
            .request()
            .cookie(sid)
            .get()
        assertThat(response)
            .hasFieldOrPropertyWithValue("status", 200)
        assertThat(response.cookies["NABLARCH_SID"])
            .`as`("セッションは有効であること")
            .isNotNull()
    }

    @Test
    @RunAsClient
    fun 次のリクエストまでの間にセッションが破棄された場合はセッション情報が取得出来ないこと() {
        val webTarget = ClientBuilder.newClient()
            .target(deploymentUri)
            .path("/testapp")

        // put session request
        val sid = webTarget
            .queryParam("c", "siosio.testapp.PutSession")
            .request()
            .get()
            .cookies["NABLARCH_SID"]

        assertThat(sid)
            .hasFieldOrPropertyWithValue("path", "/")
            .hasFieldOrPropertyWithValue("maxAge", -1)
            .hasFieldOrPropertyWithValue("httpOnly", true)

        TimeUnit.SECONDS.sleep(6)

        val response = webTarget.queryParam("c", "siosio.testapp.NotFoundSession")
            .request()
            .cookie(sid)
            .get()

        assertThat(response)
            .`as`("サーバ側でセッションが無効化されるのでセッション情報が取得出来ない")
            .hasFieldOrPropertyWithValue("status", 200)

        assertThat(response.cookies["NABLARCH_SID"])
            .`as`("サーバ側で無効化されるのでクッキーが削除される")
            .isNull()
    }

    @Test
    @RunAsClient
    fun セッションを無効化した後にputした場合はSIDが変わること() {
        val webTarget = ClientBuilder.newClient()
            .target(deploymentUri)
            .path("/testapp")

        // put session request
        val sid = webTarget
            .queryParam("c", "siosio.testapp.PutSession")
            .request()
            .get()
            .cookies["NABLARCH_SID"]

        assertThat(sid)
            .hasFieldOrPropertyWithValue("path", "/")
            .hasFieldOrPropertyWithValue("maxAge", -1)
            .hasFieldOrPropertyWithValue("httpOnly", true)

        val newSid = webTarget.queryParam("c", "siosio.testapp.InvalidateAndPutSession")
            .request()
            .cookie(sid)
            .get()
            .cookies["NABLARCH_SID"]
        
        assertThat(sid!!.value)
            .isNotEqualTo(newSid!!.value)
    }
}
