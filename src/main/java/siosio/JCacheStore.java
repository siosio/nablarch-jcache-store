package siosio;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.TouchedExpiryPolicy;

import nablarch.common.web.session.SessionEntry;
import nablarch.common.web.session.SessionStore;
import nablarch.core.repository.initialization.Initializable;
import nablarch.fw.ExecutionContext;

/**
 * セッションの内容をJCacheに格納する{@link SessionStore}の実装クラス。
 *
 * @author siosio
 */
public class JCacheStore extends SessionStore implements Initializable {

    /** ストア先 */
    private Cache<String, byte[]> cache;

    /**
     * コンストラクタ。
     */
    public JCacheStore() {
        super("mem");
    }

    @Override
    public List<SessionEntry> load(final String sessionId, final ExecutionContext executionContext) {
        final byte[] sessionData = cache.get(sessionId);
        if (sessionData != null) {
            return decode(sessionData);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void save(final String sessionId, final List<SessionEntry> entries,
            final ExecutionContext executionContext) {
        cache.put(sessionId, encode(entries));
    }

    @Override
    public void delete(final String sessionId, final ExecutionContext executionContext) {
        invalidate(sessionId, executionContext);
    }

    @Override
    public void invalidate(final String sessionId, final ExecutionContext executionContext) {
        cache.remove(sessionId);
    }

    @Override
    public void initialize() {
        final CacheManager cacheManager = Caching.getCachingProvider()
                                                 .getCacheManager();

        final MutableConfiguration<String, byte[]> configuration = new MutableConfiguration<String, byte[]>()
                .setExpiryPolicyFactory(
                        TouchedExpiryPolicy.factoryOf(
                                new Duration(TimeUnit.MILLISECONDS, getExpiresMilliSeconds())));

        cache = cacheManager.createCache("sessionStore", configuration);
    }
}
