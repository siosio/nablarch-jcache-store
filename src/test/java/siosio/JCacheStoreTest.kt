package siosio

import nablarch.common.web.session.*
import nablarch.common.web.session.encoder.*
import nablarch.fw.*
import org.assertj.core.api.Assertions.*
import org.junit.*
import java.util.*
import java.util.concurrent.*
import java.util.function.*
import javax.cache.*

/**
 * [JCacheStore]のテスト
 */
internal class JCacheStoreTest {

    private var sut: JCacheStore = JCacheStore()

    @Before
    fun setUp() {
        sut.setExpires(5L)
        sut.stateEncoder = JavaSerializeStateEncoder()
        sut.initialize()
    }

    @After
    fun tearDown() {
        Caching.getCachingProvider().close()
    }

    @Test
    fun セッションに値が格納できそれを取得出来ること() {
        val context = ExecutionContext()

        val entry = SessionEntry("キー", "あたい", sut)

        sut.save("key", listOf(entry), context)

        assertThat(sut.load("key", context))
            .hasSize(1)
            .extracting(Function<SessionEntry, String> { it.key }, Function<SessionEntry, Any> { it.value })
            .contains(tuple("キー", "あたい"), atIndex(0))
    }

    @Test
    fun invalidateした場合全てのセッション情報が取得できなくなること() {
        val context = ExecutionContext()

        val entries = ArrayList<SessionEntry>()
        entries.add(SessionEntry("キー", "あたい", sut))
        entries.add(SessionEntry("objects", ArrayList<Any>(), sut))

        sut.save("key", entries, context)

        assertThat(sut.load("key", context))
            .`as`("invalidateする前なのでストアにデータが存在する")
            .hasSize(2)

        sut.invalidate("key", context)

        assertThat(sut.load("key", context))
            .`as`("invalidateしたのでストアからデータが削除され、ロードされない")
            .isEmpty()
    }

    @Test
    fun deleteした場合全てのセッション情報が取得できなくなること() {
        val context = ExecutionContext()

        val entries = ArrayList<SessionEntry>()
        entries.add(SessionEntry("キー", "あたい", sut))
        entries.add(SessionEntry("objects", ArrayList<Any>(), sut))

        sut.save("key", entries, context)

        assertThat(sut.load("key", context))
            .`as`("delete前はストアにデータが存在する")
            .hasSize(2)

        sut.delete("key", context)

        assertThat(sut.load("key", context))
            .`as`("deleteしたのでストアからデータが削除され、ロードされない")
            .isEmpty()
    }

    @Test
    fun 有効期限を超過した場合は取得できなくなること() {
        val context = ExecutionContext()

        val entries = ArrayList<SessionEntry>()
        entries.add(SessionEntry("キー", "あたい", sut))
        entries.add(SessionEntry("objects", ArrayList<Any>(), sut))

        sut.save("key", entries, context)

        assertThat(sut.load("key", context))
            .`as`("ストアにデータが存在していること")
            .hasSize(2)

        // 有効期限が5秒なので少し長めに待機
        TimeUnit.SECONDS.sleep(6)
        assertThat(sut.load("key", context))
            .`as`("timeoutしているのでストア上のデータは削除されていること")
            .isEmpty()

    }

    @Test
    fun 複数のセッション情報をもてること() {
        val context = ExecutionContext()

        val entries = ArrayList<SessionEntry>()
        entries.add(SessionEntry("キー", "あたい", sut))
        entries.add(SessionEntry("objects", ArrayList<Any>(), sut))

        sut.save("key", entries, context)
        
        TimeUnit.SECONDS.sleep(4)
        sut.save("key2", entries, context)


        TimeUnit.SECONDS.sleep(2)
        assertThat(sut.load("key", context))
            .`as`("timeoutしているのでストア上のデータは削除されていること")
            .isEmpty()

        assertThat(sut.load("key2", context))
            .`as`("key2はタイムアウトしていないので取得できる")
            .hasSize(2)
    }
}