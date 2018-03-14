import com.pubnub.api.*;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import java.util.*;

/**
 * Created by Abhi on 2018-03-11.
 */

public class PubNubAPI {
    PubNub pubNub;
    Connection connection = null;
    List<String> subscriptions;
    User user;
    boolean client = true;
    String root = "";
    Map<String, LinkedList<String>> history = new HashMap<>();


    public PubNubAPI(boolean client) {
        this.client = client;
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey(Permissions.SUBSCRIBE_KEY);
        // pnConfiguration.setFilterExpression("uuid != '" + pnConfiguration.getUuid()+"'");

        pnConfiguration.setPublishKey(Permissions.PUBLISH_KEY);
        this.pubNub = new PubNub(pnConfiguration);
        this.pubNub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory) {
                    // This event happens when radio / connectivity is lost
                } else if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                    // Connect event. You can do stuff like publish, and know you'll get it.
                    // Or just use the connected event to confirm you are subscribed for
                    // UI / internal notifications, etc
                    if (status.getCategory() == PNStatusCategory.PNConnectedCategory){
                        pubnub.publish().channel("test").message("hello!!").async(new PNCallback<PNPublishResult>() {
                            @Override
                            public void onResponse(PNPublishResult result, PNStatus status) {
                                // Check whether request successfully completed or not.
                                if (!status.isError()) {
                                    // Message successfully published to specified channel.
                                }
                                // Request processing failed.
                                else {
                                    // Handle message publish error. Check 'category' property to find out possible issue
                                    // because of which request did fail.
                                    //
                                    // Request can be resent using: [status retry];
                                }
                            }
                        });
                    }
                } else if (status.getCategory() == PNStatusCategory.PNReconnectedCategory) {
                    // Happens as part of our regular operation. This event happens when
                    // radio / connectivity is lost, then regained.
                } else if (status.getCategory() == PNStatusCategory.PNDecryptionErrorCategory) {
                    // Handle messsage decryption error. Probably client configured to
                    // encrypt messages and on live data feed it received plain text.
                }
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                // Handle new message stored in message.message
                if (message.getChannel() != null) {
                    // Message has been received on channel group stored in
                    // message.getChannel()
                    if (client) {
                        handleNewMessage(message);
                    } else {
                        handleCommand(message);
                    }

                } else {
                    // Message has been received on channel stored in
                    // message.getSubscription()
                }

            /*
                log the following items with your favorite logger
                    - message.getMessage()
                    - message.getSubscription()
                    - message.getTimetoken()
            */
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            }
        });
        this.subscriptions = new ArrayList<>();
    }

    public boolean connect(User user, Connection connection, Database database) {
        boolean success = false;
        try {
            success = database.connect(user, connection);
            if (success) {
                this.user = user;
                this.root = connection.getUser() + ": ~Shell$  ";
                this.subscriptions.add(connection.getID());
                this.connection = connection;
                this.pubNub.subscribe()
                        .channels(this.subscriptions)   // subscribe to channel of the form from.to.channel
                        .withPresence()                 // also subscribe to related presence information
                        .execute();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return success;
    }

    public boolean connect(User user) {
        this.user = user;
        this.subscriptions.add(user.getUsername()+".*");    // wildcard
        this.pubNub.subscribe()
                .channels(this.subscriptions) // subscribe to channel of the form from.to.channel
                .execute();
        // System.out.println("subscribed with wildcard");
        return true;
    }

    public void unsubscribeToAllChannels() {
        this.pubNub.unsubscribe()
                .channels(this.subscriptions)
                .execute();
    }

    public void disconnect(boolean client) {
        if (client) {
            if (this.connection != null) {
                this.user = null;
                this.unsubscribeToAllChannels();
                this.subscriptions = new ArrayList<>();
                this.connection = null;
            }
        } else {
            this.user = null;
            this.unsubscribeToAllChannels();
            this.subscriptions = new ArrayList<>();
        }
    }

    public boolean sendMessage(String command, User user) {
        boolean success = false;
        if (connection.getID().split("\\.")[1].equals(user.getUsername())) {
            success = true;
            // Map<String, Object> meta = new HashMap<>();
            // meta.put("uuid", pnConfiguration.getUuid());
            this.pubNub.publish()
                    // .meta(meta)
                    .message(Arrays.asList(new PubNubMessage(user.getUsername(), command).getJSON()))
                    .channel(this.connection.getID())
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            // handle publish result, status always present, result if successful
                            // status.isError to see if error happened
                        }
                    });
        } else {
            System.out.println("Must connect to the server before sending a command.");
        } return success;
    }


    public boolean sendMessage(Connection connection, String message) {
        boolean success = false;
            success = true;
            // Map<String, Object> meta = new HashMap<>();
            // meta.put("uuid", pnConfiguration.getUuid());
            this.pubNub.publish()
                    // .meta(meta)
                    .message(Arrays.asList(new PubNubMessage(user.getUsername(), message).getJSON()))
                    .channel(connection.getID())
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            // handle publish result, status always present, result if successful
                            // status.isError to see if error happened
                        }
                    });
        return success;
    }

    public void handleNewMessage(PNMessageResult message) {
        if (this.connection != null && message.getChannel().equals(this.connection.getID())) {
            PubNubMessage parsedMessage = new PubNubMessage(message.getMessage().getAsJsonArray());
            if (!this.user.getUsername().equals(parsedMessage.getPublisher())) {
                if (!parsedMessage.getMessage().equals("")) {
                    System.out.println();
                    for (String line : parsedMessage.getMessage().split("///")) {
                        System.out.println(line);
                    }
                    System.out.print(this.root);
                }
            }
        }
    }

    public void handleCommand(PNMessageResult message) {
        PubNubMessage parsedMessage = new PubNubMessage(message.getMessage().getAsJsonArray());
        if (!user.getUsername().equals(parsedMessage.getPublisher())) {
            String channel = message.getChannel();
            if (history.get(channel) == null) {
                LinkedList<String> localHistory = new LinkedList<>();
                localHistory.add(parsedMessage.getMessage());
                history.put(channel, localHistory);
            } else {
                history.get(channel).add(parsedMessage.getMessage());
            }
            Connection connection = new Connection(user, parsedMessage.getPublisher(), message.getChannel(), "", false);
            sendMessage(connection, Bash.executeBashCommands(history.get(channel)));
        }
    }
}
