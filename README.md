
Cache Redis with TTL Plugin
===========================

#Summary:
This plugin is an extension of [redis-cache-plugin] with TTL for grails version >2.4. Since cache name specific TTL wasn't available for grails 2.4 , we have made some code pull from higher version plugins and added code for this functionality.


#Description:
This plugin can be configured for expiration based on the cahce name .
Version 3.0.0 and above require Grails 2.4 or higher:

	plugins {
		 compile "org.grails.plugins:redisCache-TTL:1.0.0"
	}

#Configuration: 
 Apart from the usual redis cache config,we have the ability for specifying the name of the cache and TTL in a Map List as below(cachesExpiry)
 
	grails {
	 	cache {
		 cachesExpiry=[[ name:"subArrayIds", ttl:"90"],
		 		[name:"fromId",ttl:"60"]]   // cache 'subArrayIds' will expire in 90 sec and 'fromId' cache will expire in 60 secs as per config 
		   	redis {
			 hostName = 'localhost'
			 port = 6379
			 timeout = 2000
		   	}
	    }
	 }

This is based on spring @Cacheable and @CacheEvict annotations

Thanks to Burt.

For the base plugin information see [the plugin page](http://grails.org/plugin/cache-redis)
