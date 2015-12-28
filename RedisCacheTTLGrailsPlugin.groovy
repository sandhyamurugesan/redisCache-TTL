/* Copyright 2012 SpringSource.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import grails.plugin.cache.redis.GrailsRedisCache
import grails.plugin.cache.redis.GrailsRedisCacheManager
import grails.plugin.cache.web.filter.redis.*

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.cache.DefaultRedisCachePrefix
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.JedisShardInfo
import redis.clients.jedis.Protocol

import java.util.List;
import java.util.Map;
/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 * @extendedBy <a href='mailto:dhya.san@gmail.com'>Sandhya</a>
 */
class RedisCacheTTLGrailsPlugin {
    // the plugin version
    def version = "1.0.0"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.4 > *"
     def loadAfter = ['cache']
    def pluginExcludes = [
            'grails-app/conf/*CacheConfig.groovy',
            'scripts/CreateCacheRedisTestApps.groovy',
            'docs/**',
            'src/docs/**'
    ]


    // TODO Fill in these fields
    def title = "Redis Cache TTL Plugin" // Headline display name of the plugin
    def author = "Sandhya"
    def authorEmail = "dhya.san@gmail.com"
    def description = 'A Redis-based implementation of the Cache plugin with cahce name specific TTL'

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/redis-cache-ttl"

    
	String license = 'APACHE'
	def developers = [
			[name: "Burt Beckwith", email: 'burt@burtbeckwith.com'],
			[name: "Sandhya", email: 'dhya.san@gmail.com'],
			[name: 'Costin Leau']
	]
	def organization = [name: 'Pivotal', url: 'http://www.gopivotal.com/oss']
	def issueManagement = [system: 'JIRA', url: 'http://jira.grails.org/browse/GPCACHEREDIS']
	def scm = [url: 'https://github.com/grails-plugins/grails-redisCache-TTL']

	def doWithSpring = {
		if (!isEnabled(application)) {
			log.warn 'Redis Cache TTL plugin is disabled'
			return
		}
		List<Map> cachesExpiry =null
		def cacheConfig = application.config.grails.cache
		def redisCacheConfig = cacheConfig.redis
		if(cacheConfig.cachesExpiry)
		 cachesExpiry = cacheConfig.cachesExpiry as List<Map>
		 println cachesExpiry
		int configDatabase = redisCacheConfig.database ?: 0
		boolean configUsePool = (redisCacheConfig.usePool instanceof Boolean) ? redisCacheConfig.usePool : true
		String configHostName = redisCacheConfig.hostName ?: 'localhost'
		int configPort = redisCacheConfig.port ?: Protocol.DEFAULT_PORT
		int configTimeout = redisCacheConfig.timeout ?: Protocol.DEFAULT_TIMEOUT
		String configPassword = redisCacheConfig.password ?: null
		Long ttlInSeconds = cacheConfig.ttl ?: GrailsRedisCache.NEVER_EXPIRE
		//print ttlInSeconds
		Boolean isUsePrefix = (redisCacheConfig.usePrefix instanceof Boolean) ? redisCacheConfig.usePrefix : false
		String keySerializerBean = redisCacheConfig.keySerializer instanceof String ?
				redisCacheConfig.keySerializer : null
		String hashKeySerializerBean = redisCacheConfig.hashKeySerializer instanceof String ?
				redisCacheConfig.hashKeySerializer : null

		grailsCacheJedisPoolConfig(JedisPoolConfig)

		grailsCacheJedisShardInfo(JedisShardInfo, configHostName, configPort) {
			password = configPassword
			connectionTimeout = configTimeout
		}

		redisConnectionFactory(JedisConnectionFactory) {
			usePool = configUsePool
			database = configDatabase
			hostName = configHostName
			port = configPort
			timeout = configTimeout
			password = configPassword
			poolConfig = ref('grailsCacheJedisPoolConfig')
			shardInfo = ref('grailsCacheJedisShardInfo')
		}

		grailsRedisCacheSerializer(GrailsSerializer)

		grailsRedisCacheDeserializer(GrailsDeserializer)

		grailsRedisCacheDeserializingConverter(GrailsDeserializingConverter) {
			deserializer = ref('grailsRedisCacheDeserializer')
		}

		grailsRedisCacheSerializingConverter(GrailsSerializingConverter) {
			serializer = ref('grailsRedisCacheSerializer')
		}

		grailsCacheRedisSerializer(GrailsRedisSerializer) {
			serializer = ref('grailsRedisCacheSerializingConverter')
			deserializer = ref('grailsRedisCacheDeserializingConverter')
		}

		grailsCacheRedisKeySerializer(GrailsRedisKeySerializer, ref('grailsCacheRedisSerializer'))

		grailsCacheRedisTemplate(RedisTemplate) {
			connectionFactory = ref('redisConnectionFactory')
			defaultSerializer = ref('grailsCacheRedisSerializer')
			if (keySerializerBean)
				keySerializer = ref(keySerializerBean)
			if (hashKeySerializerBean)
				hashKeySerializer = ref(hashKeySerializerBean)
		}

		String delimiter = redisCacheConfig.cachePrefixDelimiter ?: ':'
		redisCachePrefix(DefaultRedisCachePrefix, delimiter)

		grailsCacheManager(GrailsRedisCacheManager, ref('grailsCacheRedisTemplate'),cachesExpiry) {
			cachePrefix = ref('redisCachePrefix')
			timeToLive = ttlInSeconds
			usePrefix = isUsePrefix
		}

		grailsCacheFilter(RedisPageFragmentCachingFilter) {
			cacheManager = ref('grailsCacheManager')
			nativeCacheManager = ref('grailsCacheRedisTemplate')
			// TODO this name might be brittle - perhaps do by type?
			cacheOperationSource = ref('org.springframework.cache.annotation.AnnotationCacheOperationSource#0')
			keyGenerator = ref('webCacheKeyGenerator')
			expressionEvaluator = ref('webExpressionEvaluator')
		}
	}

	private boolean isEnabled(GrailsApplication application) {
		// TODO
		true
	}
   
}
