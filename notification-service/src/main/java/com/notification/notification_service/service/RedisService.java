package com.notification.notification_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    
    // STEP 1: Create logger for debugging (will be auto-created by @Slf4j annotation)
    
    private static final Logger log = LoggerFactory.getLogger(RedisService.class);
    
    // STEP 2: Declare RedisTemplate dependency (injected by Spring)
    private final RedisTemplate<String,String> redisTemplate;

    
    // STEP 3: Constructor injection (Spring will inject RedisTemplate automatically)
    public RedisService(RedisTemplate<String,String>redisTemplate){
        this.redisTemplate=redisTemplate;
    }

    //       Initialize the redisTemplate field
    
    
    //  METHOD 1: set() 
    // Purpose: Store template priority in Redis cache
    // Parameters: templateName (String), templatePriority (int)
    // Returns: void

   
    
    // STEP 4: Inside set() method - Use try-catch block
         public void set(String templateName,int templatePriority){
        try{
            String priorityStr=Integer.toString(templatePriority);
            redisTemplate.opsForValue().set(templateName,priorityStr,1, TimeUnit.DAYS);
        }
        catch (Exception e){
            log.error("Exception setting value to redis. Exception: "+e);
        }
    }
    
    
    //  METHOD 2: get() 
    // Purpose: Retrieve template priority from Redis cache
    // Parameters: templateName (String)
    // Returns: int (priority 1/2/3, or -1 if not found/error)
    
    public int get(String templateName) {
        try {
            // Call Redis to get the cached priority value
            // opsForValue() gives access to simple key-value operations
            String o = redisTemplate.opsForValue().get(templateName);
            
            // Check if template exists in cache
            if (o == null) {
                // Template not found in Redis cache (cache miss)
                log.info("{} template not available in Redis", templateName);
                return -1; // Return -1 to indicate cache miss
            }
            
            // Convert String from Redis to integer priority
            // Redis stores everything as strings, parse back to int
            int priority = Integer.parseInt(o);
            log.info("Retrieved priority {} for template {} from Redis", priority, templateName);
            
            return priority; // Return the cached priority
            
        } catch (Exception e) {
            // Log any errors (connection issues, parsing errors, etc.)
            log.error("Exception getting value from redis. Exception: " + e);
            return -1; // Return -1 on error, caller will fallback to database
        }
    }
    
}