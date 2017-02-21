package mqttextender.auth_demo.hooks;

import com.lightstreamer.mqtt_extender.hooks.HookException;
import com.lightstreamer.mqtt_extender.hooks.IMqttBrokerConfig;
import com.lightstreamer.mqtt_extender.hooks.IMqttConnectOptions;
import com.lightstreamer.mqtt_extender.hooks.IMqttExtenderHook;
import com.lightstreamer.mqtt_extender.hooks.IMqttMessage;
import com.lightstreamer.mqtt_extender.hooks.IMqttSubscription;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AuthHookWithAuthCache implements IMqttExtenderHook {

    // List of connection ID-user pairs.
    private final ConcurrentHashMap<String, String> connectionIdToUser= new ConcurrentHashMap<String,String>();

    // Authorization cache for the user.
    private final Map<String,UserAuthorizations> authCache= new ConcurrentHashMap<String, UserAuthorizations>();

//    @Override
//    @SuppressWarnings("rawtypes")
//    public boolean onConnectionRequest(String connectionId, String user, String password, Map clientContext, String clientPrincipal) throws HookException {
//
//        /*
//         * A user is connecting. We suppose the password works as an authentication token,
//         * generated by the webserver in response to a user/password login made by the client.
//         * Thus, we have to ask the same server (or a common backend like a memcached or a DB) if
//         * the received token is (still) valid.
//         * This demo does not actually perform the request, user/token pairs are hardcoded in the
//         * AuthorizationRequest class
//         */
//        AuthorizationResult result= AuthorizationRequest.validateToken(user, password);
//        if (result != AuthorizationResult.OK)
//            throw new HookException("Unauthorized access: token invalid for user '" + user + "'", result.toString());
//
//        /*
//         * NOTE: as documented in the Lightstreamer JMS Extender Documentation PDF, the onConnectionRequest
//         * call is made during the notifyNewSession call of the JMS Extender Metadata Adapter. For this
//         * reason, if we have to block in order to perform the lookup for the client, a specific "SET"
//         * thread pool may be configured in the adapters.xml configuration file for the JMS Extender
//         * Adapter Set. We could also speed up things using a local cache.
//         */
//
//        /*
//         * NOTE 2: it is common practice for a webserver to place its session token inside a cookie;
//         * if the cookie, the JS client library, and the Lightstreamer server are properly configured,
//         * such cookie is available in the HTTP headers map, which can be obtained from the
//         * clientContext map with the "HTTP_HEADERS" key; you might be tempted to use it to authenticate
//         * the user: this approach is discouraged, please check the Server configuration for the
//         * <use_protected_js> and <forward_cookies> documentation for further info about the topic.
//         */
//
//        /*
//         * Since subsequent hook calls will rely only on the connection ID, we store the user
//         * associated with this connection ID on an internal map.
//         */
//        connectionIdToUser.put(connectionId, user);
//
//        /*
//         * We now verify if a cache containing his authorizations is already available,
//         * and, if not, query the external service to create one.
//         */
//        UserAuthorizations userCache= null;
//        synchronized (authCache) {
//            userCache= authCache.get(user);
//            if (userCache == null) {
//                userCache= new UserAuthorizations();
//                authCache.put(user, userCache);
//            }
//        }
//
//        /*
//         * The cache object also counts the connections associated to the related user, so we inform it
//         * to count a new connection.
//         */
//        boolean isFirstConnection= userCache.newConnection();
//        if (isFirstConnection) {
//
//            /*
//             * If this is the first connection we have to query the service to retrieve the list of authorizations.
//             * We don't need it right away, thus it would be a pity to block the thread. So we will make the request
//             * to the service on a spearated thread.
//             */
//            final String retrievedUser= user;
//            final UserAuthorizations retrievedUserCache= userCache;
//            AuthorizationsThreads.execute(new Runnable() {
//                public void run() {
//
//                    /*
//                     * In a real case, here we would call the service with a blocking call. In this demo the
//                     * authorization list is hard-coded int the AuthorizationRequest class, the call will not
//                     * block and will always work; in a real case you will probably need a fallback mechanism to
//                     * release the CountDownLatch in UserAuthorization if the authorization mechanism fails
//                     * or a timeout expires.
//                     */
//                    retrievedUserCache.cacheAuthorizations(AuthorizationRequest.getUserAuthorizations(retrievedUser));
//                }
//            });
//        }
//
//        return true;
//    }
//
//    @Override
//    public void onConnectionClose(String connectionId) {
//
//        /*
//         * Once all the connections for a certain user are closed we have to clean the cache,
//         * thus we have to keep count of how many connections a user has. We now first recover
//         * the user associated with the connection ID from our internal map.
//         */
//        String user= connectionIdToUser.get(connectionId);
//        if (user == null)
//            return; // Should never happen
//
//        /*
//         * Then we check his cache object to verify the number of active connections.
//         * If this is the last one we simply destroy the cache.
//         */
//        synchronized (authCache) {
//            UserAuthorizations userCache= authCache.get(user);
//            if (userCache == null)
//                return; // Should never happen
//
//            boolean isLastConnection= userCache.endConnection();
//            if (isLastConnection)
//                authCache.remove(user);
//        }
//    }
//
//    @Override
//    public boolean onMessageConsumerRequest(String connectionId, String dataAdapterName, String sessionGuid, String destinationName, boolean destinationIsTopic) throws HookException {
//
//        /*
//         * A user is trying to create a message consumer on a destination, we have to verify if
//         * he is authorized to see what he's asking for. To do this we first recover the user associated
//         * with the connection ID from our internal map. Then, since we already have a local cache or such cache is
//         * being filled, so we just check it.
//         */
//        String user= connectionIdToUser.get(connectionId);
//        if (user == null)
//            return false; // Should never happen
//
//        synchronized (authCache) {
//            UserAuthorizations userCache= authCache.get(user);
//            if (userCache == null)
//                return false; // Should never happen
//
//            Map<String, AuthorizationResult> authorizations= userCache.getAuthorizations();
//            if (authorizations == null)
//                return false; // May happen if the authorization cache is taking too long to fill
//
//            AuthorizationResult result= authorizations.get(destinationName);
//            if (result == null) {
//
//                // Here we allow unknown destination names
//                // as they may be temporary queues or topics
//                return true;
//            }
//
//            if (result != AuthorizationResult.OK)
//                throw new HookException("Unauthorized access: user '" + user + "' can't receive messages from destination '" + destinationName + "'", result.toString());
//
//            return true;
//        }
//    }
//
//    @Override
//    public boolean onDurableSubscriptionRequest(String connectionId, String dataAdapterName, String clientId, String sessionGuid, String subscriptionName, String topicName) throws HookException {
//
//        /*
//         * A user is trying to create a durable subscription on a destination, we have to verify if
//         * he is authorized to see what he's asking for. To do this we first recover the user associated
//         * with the connection ID from our internal map. Then, since we already have a local cache or such cache is
//         * being filled, so we just check it.
//         */
//        String user= connectionIdToUser.get(connectionId);
//        if (user == null)
//            return false; // Should never happen
//
//        synchronized (authCache) {
//            UserAuthorizations userCache= authCache.get(user);
//            if (userCache == null)
//                return false; // Should never happen
//
//            Map<String, AuthorizationResult> authorizations= userCache.getAuthorizations();
//            if (authorizations == null)
//                return false; // May happen if user has no authorizations at all
//
//            AuthorizationResult result= authorizations.get(topicName);
//            if (result == null) {
//
//                // Here we allow unknown destination names
//                // as they may be temporary queues or topics
//                return true;
//            }
//
//            if (result != AuthorizationResult.OK)
//                throw new HookException("Unauthorized access: user '" + user + "' can't subscribe to topic '" + topicName + "'", result.toString());
//
//            return true;
//        }
//    }
//
//    @Override
//    public boolean onMessageProducerRequest(String connectionId, String dataAdapterName, String sessionGuid, String destinationName, boolean destinationIsTopic) throws HookException {
//
//        /*
//         * A user is trying to create a message producer on a destination, we have to verify if
//         * he is authorized to see what he's asking for. To do this we first recover the user associated
//         * with the connection ID from our internal map. Then, since we already have a local cache or such cache is
//         * being filled, so we just check it.
//         */
//        String user= connectionIdToUser.get(connectionId);
//        if (user == null)
//            return false; // Should never happen
//
//        synchronized (authCache) {
//            UserAuthorizations userCache= authCache.get(user);
//            if (userCache == null)
//                return false; // Should never happen
//
//            Map<String, AuthorizationResult> authorizations= userCache.getAuthorizations();
//            if (authorizations == null)
//                return false; // May happen if user has no authorizations at all
//
//            AuthorizationResult result= authorizations.get(destinationName);
//            if (result == null) {
//
//                // Here we allow unknown destination names
//                // as they may be temporary queues or topics
//                return true;
//            }
//
//            if (result != AuthorizationResult.OK)
//                throw new HookException("Unauthorized access: user '" + user + "' can't send messages to destination '" + destinationName + "'", result.toString());
//
//            return true;
//        }
//    }

    // Dedicate thread pool to retrieve authorizations lists. You might want to limit its size.
    private ExecutorService AuthorizationsThreads= Executors.newCachedThreadPool();

    // Authorization cache object
    private class UserAuthorizations {

        int connectionCount= 0;
        private Map<String, AuthorizationResult> authorizations;

        // If we check for authorizations before the authorizations
        // are filled, the reqeust will be kept waiting by this CountDownLatch.
        private CountDownLatch cacheWait = new CountDownLatch(1);

        // Returns true if is the first connection
        public synchronized boolean newConnection() {
            connectionCount++;
            return (connectionCount == 1);
        }

        // Returns true if it was the last connection
        public synchronized boolean endConnection() {
            connectionCount--;
            return (connectionCount == 0);
        }

        // Saves the authorization map and releases the CountDownLatch:
        // any request, waiting for this authorizations list, can
        // now continue.
        // We expect this method to be called only once (it is),
        // safety controls are out of scope here.
        public void cacheAuthorizations(Map<String, AuthorizationResult> authorizations) {
           this.authorizations = authorizations;
           cacheWait.countDown();
        }

        // Retrieves the authorizations map if already available,
        // otherwise awaits.
        public Map<String, AuthorizationResult> getAuthorizations() {
           try {

               // We do not wait forever, we have to release the thread.
               cacheWait.await(3, TimeUnit.SECONDS);

           } catch (InterruptedException e) { /* Exception ignored */ }

           return this.authorizations;
        }
    }

    @Override
    public boolean canConnect(String sessionId, String clientId, String serverAddress,
        IMqttConnectOptions connectOptions) throws HookException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canOpenSession(String sessionId, String userId, String password,
        Map clientContext, String clientPrincipal) throws HookException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canPublish(String sessionId, String clientId, String serverAddress,
        IMqttMessage message) throws HookException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canSubscribe(String sessionId, String clientId, String serverAddress,
        IMqttSubscription subscription) throws HookException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void init(File configDir) throws HookException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onDisconnection(String sessionId, String clientId, String serverAddress) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onSessionClose(String sessionId) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onUnsubscribe(String sessionId, String clientId, String serverAddress,
        String topicFilter) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public IMqttBrokerConfig resolveAlias(String alias) throws HookException {
        // TODO Auto-generated method stub
        return null;
    }
}
