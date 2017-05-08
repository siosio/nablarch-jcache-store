package siosio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.cache.Caching;

import nablarch.common.web.session.SessionEntry;
import nablarch.common.web.session.encoder.JavaSerializeStateEncoder;
import nablarch.fw.ExecutionContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link JCacheStore}のテスト
 */
class JCacheStoreTest {

    private JCacheStore sut;

    @BeforeEach
    void setUp() throws Exception {
        sut = new JCacheStore();
        sut.setExpires(5L);
        sut.setStateEncoder(new JavaSerializeStateEncoder());
        sut.initialize();
    }

    @AfterEach
    void tearDown() throws Exception {
        Caching.getCachingProvider().close();
    }

    @Test
    void セッションに値が格納できそれを取得出来ること() throws Exception {
        final ExecutionContext context = new ExecutionContext();

        final SessionEntry entry = new SessionEntry("キー", "あたい", sut);

        sut.save("key", Collections.singletonList(entry), context);

        final List<SessionEntry> sessionEntries = sut.load("key", context);

        assertThat(sessionEntries)
                .hasSize(1)
                .extracting(SessionEntry::getKey, SessionEntry::getValue)
                .contains(tuple("キー", "あたい"), atIndex(0));
    }

    @Test
    public void invalidateした場合全てのセッション情報が取得できなくなること() throws Exception {
        final ExecutionContext context = new ExecutionContext();

        final List<SessionEntry> entries = new ArrayList<>();
        entries.add(new SessionEntry("キー", "あたい", sut));
        entries.add(new SessionEntry("objects", new ArrayList<>(), sut));

        sut.save("key", entries, context);

        assertThat(sut.load("key", context))
                .as("invalidateする前なのでストアにデータが存在する")
                .hasSize(2);

        sut.invalidate("key", context);
        
        assertThat(sut.load("key", context))
                .as("invalidateしたのでストアからデータが削除され、ロードされない")
                .isEmpty();
    }

    @Test
    public void deleteした場合全てのセッション情報が取得できなくなること() throws Exception {
        final ExecutionContext context = new ExecutionContext();

        final List<SessionEntry> entries = new ArrayList<>();
        entries.add(new SessionEntry("キー", "あたい", sut));
        entries.add(new SessionEntry("objects", new ArrayList<>(), sut));

        sut.save("key", entries, context);

        assertThat(sut.load("key", context))
                .as("delete前はストアにデータが存在する")
                .hasSize(2);

        sut.delete("key", context);
        
        assertThat(sut.load("key", context))
                .as("deleteしたのでストアからデータが削除され、ロードされない")
                .isEmpty();
    }

    @Test
    public void 有効期限を超過した場合は取得できなくなること() throws Exception {
        final ExecutionContext context = new ExecutionContext();

        final List<SessionEntry> entries = new ArrayList<>();
        entries.add(new SessionEntry("キー", "あたい", sut));
        entries.add(new SessionEntry("objects", new ArrayList<>(), sut));

        sut.save("key", entries, context);

        assertThat(sut.load("key", context))
                .as("ストアにデータが存在していること")
                .hasSize(2);

        // 有効期限が5秒なので少し長めに待機
        TimeUnit.SECONDS.sleep(6);
        assertThat(sut.load("key", context))
                .as("timeoutしているのでストア上のデータは削除されていること")
                .isEmpty();

    }
}